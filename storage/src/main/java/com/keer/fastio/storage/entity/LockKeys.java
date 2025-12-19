package com.keer.fastio.storage.entity;

/**
 * @Author: 张经伦
 * @Date: 2025/12/19  16:21
 * @Description:
 */
public final class LockKeys {

    public static String object(String bucket, String key) {
        return "obj:" + bucket + "/" + key;
    }

    public static String multipart(String bucket, String uploadId) {
        return "mpu:" + bucket + "/" + uploadId;
    }

    public static String bucket(String bucket) {
        return "bkt:" + bucket;
    }
}
