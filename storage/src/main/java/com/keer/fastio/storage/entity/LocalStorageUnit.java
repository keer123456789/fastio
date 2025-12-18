package com.keer.fastio.storage.entity;

import com.keer.fastio.storage.enums.LocalStorageStatus;

import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @Author: 张经伦
 * @Date: 2025/12/18  14:10
 * @Description:
 */
public class LocalStorageUnit {
    private String id;
    //物理路径
    private String path;
    //可用容量 GB
    private long usableCapacity;
    //总容量
    private long totalCapacity;
    //false 磁盘使用率小于10%，磁盘标记不健康，此时不在写入
    private LocalStorageStatus status;

    public LocalStorageUnit(String id, String path) {
        this.id = id;
        this.path = path;
        refreshCapacity();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
        refreshCapacity();
    }

    public long getUsableCapacity() {
        return usableCapacity;
    }

    public long getTotalCapacity() {
        return totalCapacity;
    }

    public void refreshCapacity() {
        try {
            // 获取文件夹所在的文件存储（磁盘分区）
            FileStore store = Files.getFileStore(Paths.get(this.path));

            long usableSpace = store.getUsableSpace();    // 可用空间
            long totalSpace = store.getTotalSpace();
            this.usableCapacity = usableSpace / 1024 / 1024 / 1024;
            this.totalCapacity = totalSpace / 1024 / 1024 / 1024;
            float percent = Float.valueOf(usableSpace) / Float.valueOf(totalSpace);
            this.status = percent < 0.1 ? LocalStorageStatus.READONLY : LocalStorageStatus.ONLINE;

        } catch (Exception e) {
            e.printStackTrace();
            status = LocalStorageStatus.OFFLINE;
        }
    }

    public String getId() {
        return id;
    }

    public LocalStorageStatus getStatus() {
        return status;
    }
}
