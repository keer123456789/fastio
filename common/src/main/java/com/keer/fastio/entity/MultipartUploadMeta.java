package com.keer.fastio.entity;

import java.util.Map;

/**
 * @author 张经伦
 * @date 2025/12/14 17:22
 * @description: 断点续传的元数据
 */
public class MultipartUploadMeta {
    private String uploadId;
    private String bucket;
    private String key;
    private Map<Integer, PartMeta> parts;
    private long createTime;

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Map<Integer, PartMeta> getParts() {
        return parts;
    }

    public void setParts(Map<Integer, PartMeta> parts) {
        this.parts = parts;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
