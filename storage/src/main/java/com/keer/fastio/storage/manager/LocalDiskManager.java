package com.keer.fastio.storage.manager;

import com.keer.fastio.common.hash.HashRing;
import com.keer.fastio.common.manager.AbstractResourceManager;
import com.keer.fastio.common.utils.HashRingUtils;
import com.keer.fastio.storage.entity.LocalStorageUnit;
import com.keer.fastio.storage.enums.LocalStorageStatus;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @Author: 张经伦
 * @Date: 2025/12/18  16:38
 * @Description: 本地磁盘管理器
 */
public class LocalDiskManager extends AbstractResourceManager {
    private List<LocalStorageUnit> disks;
    private final ReentrantLock lockDisk = new ReentrantLock();
    /**
     * 读操作hash环
     */
    private volatile NavigableMap<Long, LocalStorageUnit> readRing = new TreeMap<>();
    /**
     * 写操作hash环
     */
    private volatile NavigableMap<Long, LocalStorageUnit> writeRing = new TreeMap<>();
    private final ReentrantLock lockRing = new ReentrantLock();
    int baseVNodes = 200; // 每块盘基准 vnode

    /**
     * 异步刷新线程
     */
    private ExecutorService executorService = Executors.newFixedThreadPool(2, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "LocalDiskManagerThread");
            return t;
        }
    });
    private AtomicBoolean running = new AtomicBoolean(false);
    /**
     * 读模式
     */
    public static final int READ_MODEL = 0;
    /**
     * 写模式
     */
    public static final int WRITE_MODEL = 1;

    public LocalDiskManager(List<LocalStorageUnit> units) {
        disks = units;
        refreshDiskStatus();
        refreshRing(READ_MODEL);
        refreshRing(WRITE_MODEL);
    }


    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    protected void doInit() throws Exception {
        if (running.compareAndSet(false, true)) {
            executorService.submit(() -> {
                while (running.get() && !Thread.interrupted()) {
                    refreshRing(READ_MODEL);
                    refreshRing(WRITE_MODEL);
                    try {
                        Thread.sleep(1000 * 300L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            executorService.submit(() -> {
                while (running.get() && !Thread.interrupted()) {
                    refreshDiskStatus();
                    try {
                        Thread.sleep(1000 * 30L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    @Override
    protected void doClose() throws Exception {
        running.set(false);
    }


    /**
     * 刷新hash环
     *
     * @param model
     */
    private void refreshRing(int model) {
        //检查磁盘健康程度
        List<LocalStorageUnit> disks = new ArrayList<>();
        if (model == READ_MODEL) {
            disks = this.disks.stream().filter(d -> d.getStatus() != LocalStorageStatus.OFFLINE).collect(Collectors.toList());
        } else {
            disks = this.disks.stream().filter(d -> d.getStatus() == LocalStorageStatus.ONLINE).collect(Collectors.toList());
        }
        //根据健康磁盘生成虚拟节点并更新hash ring
        NavigableMap<Long, LocalStorageUnit> map = new TreeMap<>();
        for (LocalStorageUnit unit : disks) {

            double weight = Float.valueOf(unit.getUsableCapacity()) / Float.valueOf(unit.getTotalCapacity());
            int vnodeNum = Math.max(1, (int) (baseVNodes * weight));

            for (int i = 0; i < vnodeNum; i++) {
                map.put(HashRingUtils.hash(unit.getId() + "#" + i), unit);
            }
        }
        lockRing.lock();
        try {
            if (model == 0) {
                this.readRing = Collections.unmodifiableNavigableMap(map);
            } else {
                this.writeRing = Collections.unmodifiableNavigableMap(map);
            }

        } finally {
            lockRing.unlock();
        }

    }

    /**
     * 刷新磁盘状态
     */
    private void refreshDiskStatus() {
        lockDisk.lock();
        try {
            this.disks = this.disks.stream().map(d -> {
                d.refreshCapacity();
                return d;
            }).collect(Collectors.toList());
        } finally {
            lockDisk.unlock();
        }
    }

    /**
     * 选择节点
     *
     * @param key
     * @param model 操作模式 0-读 1-写
     * @return
     */
    public LocalStorageUnit selectUnit(String key, int model) {
        if (key == null) {
            return null;
        }
        long hash = HashRingUtils.hash(key);
        Map.Entry<Long, LocalStorageUnit> entry = null;
        if (model == READ_MODEL) {
            entry = readRing.ceilingEntry(hash);
        } else {
            entry = writeRing.ceilingEntry(hash);
        }


        if (entry != null) {
            return entry.getValue();
        } else {
            if (model == READ_MODEL) {
                return readRing.firstEntry().getValue();
            } else {
                return writeRing.firstEntry().getValue();
            }
        }
    }


}
