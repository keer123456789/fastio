package com.keer.fastio.storage.request;

/**
 * @author 张经伦
 * @date 2025/12/14 17:02
 * @description:
 */
public class ListObjectsRequest {
    private String bucket;
    private String prefix;
    private int size = 10;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
