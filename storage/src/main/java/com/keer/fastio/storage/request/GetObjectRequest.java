package com.keer.fastio.storage.request;

import com.keer.fastio.common.enums.ReadSource;

/**
 * @author 张经伦
 * @date 2025/12/14 16:57
 * @description:
 */
public class GetObjectRequest {
    // ===== 对象标识 =====
    private String bucket;
    private String key;

    // ===== Range =====
    private Long rangeStart; // nullable
    private Long rangeEnd;   // nullable (inclusive)

    // ===== 条件请求 =====
    private String ifMatch;            // ETag
    private String ifNoneMatch;
    private Long ifModifiedSince;
    private Long ifUnmodifiedSince;

    // ===== 行为控制 =====
    private boolean headOnly;           // HEAD Object
    private boolean strongConsistency;  // 是否强一致读

    // ===== 来源 =====
    private ReadSource source;

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

    public Long getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(Long rangeStart) {
        this.rangeStart = rangeStart;
    }

    public Long getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(Long rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    public String getIfMatch() {
        return ifMatch;
    }

    public void setIfMatch(String ifMatch) {
        this.ifMatch = ifMatch;
    }

    public String getIfNoneMatch() {
        return ifNoneMatch;
    }

    public void setIfNoneMatch(String ifNoneMatch) {
        this.ifNoneMatch = ifNoneMatch;
    }

    public Long getIfModifiedSince() {
        return ifModifiedSince;
    }

    public void setIfModifiedSince(Long ifModifiedSince) {
        this.ifModifiedSince = ifModifiedSince;
    }

    public Long getIfUnmodifiedSince() {
        return ifUnmodifiedSince;
    }

    public void setIfUnmodifiedSince(Long ifUnmodifiedSince) {
        this.ifUnmodifiedSince = ifUnmodifiedSince;
    }

    public boolean isHeadOnly() {
        return headOnly;
    }

    public void setHeadOnly(boolean headOnly) {
        this.headOnly = headOnly;
    }

    public boolean isStrongConsistency() {
        return strongConsistency;
    }

    public void setStrongConsistency(boolean strongConsistency) {
        this.strongConsistency = strongConsistency;
    }

    public ReadSource getSource() {
        return source;
    }

    public void setSource(ReadSource source) {
        this.source = source;
    }
}
