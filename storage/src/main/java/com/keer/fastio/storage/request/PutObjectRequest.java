package com.keer.fastio.storage.request;

import com.keer.fastio.common.enums.StorageWriteMode;
import com.keer.fastio.common.enums.WriteSource;

import java.nio.channels.ReadableByteChannel;
import java.util.Map;

/**
 * @author 张经伦
 * @date 2025/12/14 16:43
 * @description:
 */
public class PutObjectRequest {
    // ===== 对象标识 =====
    private String bucket;
    private String key;

    // ===== 数据输入 =====
    private ReadableByteChannel dataChannel;
    private Long contentLength; // 可选（HTTP 会有）

    // ===== 校验 =====
    private String contentMD5;       // 可选
    private String sha256;            // 可选

    // ===== 元数据 =====
    private Map<String, String> userMetadata;
    private Map<String, String> systemMetadata;

    // ===== 行为控制 =====
    private boolean overwrite;        // PUT / If-None-Match
    private StorageWriteMode writeMode;

    // ===== 来源标识（为未来准备）=====
    private WriteSource source;

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

    public ReadableByteChannel getDataChannel() {
        return dataChannel;
    }

    public void setDataChannel(ReadableByteChannel dataChannel) {
        this.dataChannel = dataChannel;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentMD5() {
        return contentMD5;
    }

    public void setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public Map<String, String> getUserMetadata() {
        return userMetadata;
    }

    public void setUserMetadata(Map<String, String> userMetadata) {
        this.userMetadata = userMetadata;
    }

    public Map<String, String> getSystemMetadata() {
        return systemMetadata;
    }

    public void setSystemMetadata(Map<String, String> systemMetadata) {
        this.systemMetadata = systemMetadata;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public StorageWriteMode getWriteMode() {
        return writeMode;
    }

    public void setWriteMode(StorageWriteMode writeMode) {
        this.writeMode = writeMode;
    }

    public WriteSource getSource() {
        return source;
    }

    public void setSource(WriteSource source) {
        this.source = source;
    }
}
