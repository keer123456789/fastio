package com.keer.fastio.storage.manager;

import com.keer.fastio.common.hash.HashRing;
import com.keer.fastio.common.utils.HashRingUtils;
import com.keer.fastio.storage.entity.LocalStorageUnit;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @Author: 张经伦
 * @Date: 2025/12/18  16:38
 * @Description: 本地磁盘管理器
 */
public class LocalDiskManager implements HashRing<LocalStorageUnit> {
    private List<LocalStorageUnit> disks;
    /**
     * hash环
     */
    private volatile NavigableMap<Long, LocalStorageUnit> ring = new TreeMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public LocalDiskManager(List<String> paths) {
        disks = new LinkedList<>();
        paths.forEach(p -> {
            disks.add(new LocalStorageUnit(p));
        });
        refresh();
    }

    public void init() {
        while (!Thread.interrupted()) {
            refresh();
            try {
                Thread.sleep(1000 * 600L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 刷新hash环
     */
    private void refresh() {
        //检查磁盘健康程度
        List<LocalStorageUnit> healthDisks = this.disks.stream().map(d -> {
            d.refreshCapacity();
            return d;
        }).filter(d -> d.isHealth()).collect(Collectors.toList());
        //根据健康磁盘生成虚拟节点并更新hash ring
        NavigableMap<Long, LocalStorageUnit> map = new TreeMap<>();
        for (LocalStorageUnit unit : healthDisks) {
            int baseVNodes = 200; // 每块盘基准 vnode
            double weight = Float.valueOf(unit.getUsableCapacity()) / Float.valueOf(unit.getTotalCapacity());
            int vnodeNum =Math.max(1, (int)(baseVNodes * weight));

            for (int i = 0; i < vnodeNum; i++) {
                map.put(HashRingUtils.hash(unit.getPath() + "#" + i), unit);
            }
        }
        lock.lock();
        try {
            this.ring = Collections.unmodifiableNavigableMap(map);;
        } finally {
            lock.unlock();
        }

    }


    @Override
    public LocalStorageUnit selectUnit(String key) {
        if (key == null) {
            return null;
        }
        long hash = HashRingUtils.hash(key);
        Map.Entry<Long, LocalStorageUnit> entry = ring.ceilingEntry(hash);

        if (entry != null) {
            return entry.getValue();
        } else {
            // 环回：返回第一个节点
            return ring.firstEntry().getValue();
        }
    }

    public void addLocalStorageUnit(LocalStorageUnit unit) {
        this.disks.add(unit);
    }

}
