package com.keer.fastio.storage.config;

import java.util.List;

/**
 * @author 张经伦
 * @date 2025/12/15 22:11
 * @description:
 */
public class ExecutorManagerConfig {
    /**
     * 删除bucket单线程 间隔时间
     */
    private long deleteBucketTaskInterval = 5000;
    /**
     * 数据目录
     */
    private List<String> dataPaths;

    public long getDeleteBucketTaskInterval() {
        return deleteBucketTaskInterval;
    }

    public void setDeleteBucketTaskInterval(long deleteBucketTaskInterval) {
        this.deleteBucketTaskInterval = deleteBucketTaskInterval;
    }

    public List<String> getDataPaths() {
        return dataPaths;
    }

    public void setDataPaths(List<String> dataPaths) {
        this.dataPaths = dataPaths;
    }
}
