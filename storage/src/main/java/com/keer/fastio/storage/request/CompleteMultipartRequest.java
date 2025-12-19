package com.keer.fastio.storage.request;

/**
 * @author 张经伦
 * @date 2025/12/14 17:06
 * @description:
 */
public class CompleteMultipartRequest {
    private String uploadId;
    private String bucket;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }
}
