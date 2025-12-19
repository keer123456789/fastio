package com.keer.fastio.common.utils;

/**
 * @Author: 张经伦
 * @Date: 2025/12/19  10:59
 * @Description:
 */
public class ByteUtils {
    // 工具方法：byte[] 转 hex（用于日志）
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
