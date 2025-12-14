package com.keer.fastio.request;

import com.keer.fastio.enums.ReadSource;

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
}
