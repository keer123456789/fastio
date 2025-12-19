package com.keer.fastio.common.utils;

import com.keer.fastio.common.hash.MurmurHash3_32;

/**
 * @author 张经伦
 * @date 2025/12/16 21:50
 * @description:
 */
public class HashUtils {
    public static final int seed = 9593;

    /**
     * 获取无符号hash值
     *
     * @param key
     * @return
     */
    public static long unsignedHash(String key) {
        return MurmurHash3_32.hash32(key, seed) & 0xFFFFFFFFL;
    }

    /**
     * 获取16进制hash值
     *
     * @param key
     * @return
     */
    public static String hexHash(String key) {
        return Long.toHexString(MurmurHash3_32.hash32(key, seed));
    }
}
