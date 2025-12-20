package com.keer.fastio.storage.manager;

import com.keer.fastio.common.config.StorageUnitConfig;
import com.keer.fastio.common.enums.ExceptionErrorMsg;
import com.keer.fastio.common.exception.ServiceException;
import com.keer.fastio.common.manager.AbstractResourceManager;
import com.keer.fastio.common.utils.ByteUtils;
import com.keer.fastio.common.utils.HashUtils;
import com.keer.fastio.storage.entity.LocalStorageUnit;
import com.keer.fastio.storage.enums.LocalStorageStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author: 张经伦
 * @Date: 2025/12/18  16:38
 * @Description: 本地磁盘管理器
 */
public class LocalDiskManager extends AbstractResourceManager {
    private static final Logger logger = LoggerFactory.getLogger(LocalDiskManager.class);
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
    private ExecutorService executorService = Executors.newFixedThreadPool(3, new ThreadFactory() {
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

    public LocalDiskManager(List<StorageUnitConfig> units) {
        disks = units.stream().map(u ->
             new LocalStorageUnit(u.getId(), u.getPath())
        ).collect(Collectors.toList());
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
            executorService.submit(() -> {
                while (running.get() && !Thread.interrupted()) {
                    deletePath();
                    try {
                        Thread.sleep(1000 * 300L);
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
                map.put(HashUtils.unsignedHash(unit.getId() + "#" + i), unit);
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
     * 删除桶
     */
    private void deletePath() {
        for (LocalStorageUnit unit : disks) {
            Path p = Paths.get(unit.getPath());
            if (!Files.exists(p) || !Files.isDirectory(p)) {
                continue;
            }
            try (Stream<Path> stream = Files.list(p)) {
                stream.filter(Files::isDirectory)
                        .filter(ps -> {
                            String folderName = ps.getFileName().toString();
                            return folderName.startsWith("DEL_");
                        })
                        .forEach(ps -> {
                            try {
                                Files.walkFileTree(ps, new SimpleFileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                        // 删除文件
                                        Files.delete(file);
                                        return FileVisitResult.CONTINUE;
                                    }

                                    @Override
                                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                        Files.delete(dir);
                                        return FileVisitResult.CONTINUE;
                                    }
                                });
                            } catch (IOException e) {
                                logger.warn("递归删除桶（{}）失败,错误信息：{}", ps.getFileName().getFileName(), e.getMessage());
                            }
                        });
            } catch (IOException e) {
                logger.warn("查找路径（{}）下的文件夹失败，导致本次删除桶任务失败,错误信息：{}", unit.getPath(), e.getMessage());
            }
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
        long hash = HashUtils.unsignedHash(key);
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


    public List<LocalStorageUnit> getDisks() {
        return disks;
    }

    /**
     * 写入文件
     *
     * @param channel
     * @param destPath
     * @param bufferSize
     * @param openOptions
     * @return
     */
    public WriteResult writeFile(ReadableByteChannel channel, Path destPath, int bufferSize, StandardOpenOption... openOptions) {


        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        int totalWriteSize = 0;
        try (FileChannel destChannel = FileChannel.open(destPath, openOptions)) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize); // 8KB 缓冲区
            ReadableByteChannel source = channel;

            while (source.read(buffer) != -1) {
                buffer.flip(); // 切换到读模式
                totalWriteSize += buffer.remaining();
                // 👇 关键：先更新 MD5（需要 byte[] 或 ByteBuffer）
                // MessageDigest 支持直接传 ByteBuffer！
                md5.update(buffer);

                // 写入文件
                destChannel.write(buffer);

                buffer.clear(); // 清空，准备下一轮读
            }
            destChannel.force(true);

        } catch (Exception e) {
            throw new ServiceException(ExceptionErrorMsg.FileWriteFail);
        }
        byte[] digest = md5.digest();
        String etag = ByteUtils.bytesToHex(digest);
        return new WriteResult(etag, totalWriteSize);
    }

    /**
     * 合并分片文件
     *
     * @param partPaths
     * @param destPath
     * @param bufferSize
     * @return
     */
    public WriteResult mergerPart(List<Path> partPaths, Path destPath, int bufferSize) {
        // 使用 Direct Buffer 减少 GC 压力
        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        int totalWriteSize = 0;
        // 打开目标文件（追加写入）
        try (FileChannel destChannel = FileChannel.open(destPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) { // 先清空（可选）

            for (Path partFile : partPaths) {
                // 逐个打开分片文件
                try (FileChannel partChannel = FileChannel.open(partFile, StandardOpenOption.READ)) {
                    while (partChannel.read(buffer) != -1) {
                        buffer.flip();
                        totalWriteSize += buffer.remaining();
                        destChannel.write(buffer);
                        buffer.clear();
                    }
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
        byte[] digest = md5.digest();
        String etag = ByteUtils.bytesToHex(digest);
        return new WriteResult(etag, totalWriteSize);
    }

    public static class WriteResult {
        private String etag;
        private long totalSize;

        public WriteResult(String etag, long totalSize) {
            this.etag = etag;
            this.totalSize = totalSize;
        }

        public String getEtag() {
            return etag;
        }

        public void setEtag(String etag) {
            this.etag = etag;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public void setTotalSize(long totalSize) {
            this.totalSize = totalSize;
        }
    }

}
