package com.keer.fastio.storage.request;

import java.nio.channels.ReadableByteChannel;

/**
 * @author 张经伦
 * @date 2025/12/14 17:06
 * @description:
 */
public class UploadPartRequest {
    private String uploadId;
    private String bucket;
    private int index;

    private ReadableByteChannel dataChannel;

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public ReadableByteChannel getDataChannel() {
        return dataChannel;
    }

    public void setDataChannel(ReadableByteChannel dataChannel) {
        this.dataChannel = dataChannel;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
}
