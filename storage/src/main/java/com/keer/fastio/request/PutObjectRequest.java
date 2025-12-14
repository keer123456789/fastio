package com.keer.fastio.request;

import com.keer.fastio.enums.StorageWriteMode;
import com.keer.fastio.enums.WriteSource;

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
}
