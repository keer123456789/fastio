package com.keer.fastio.common.utils;

import com.keer.fastio.common.hash.MurmurHash3_32;

/**
 * @author 张经伦
 * @date 2025/12/16 21:50
 * @description:
 */
public class HashRingUtils {
    public static final int seed = 9593;

    public static long hash(String key) {
        return MurmurHash3_32.hash32(key, seed) & 0xFFFFFFFFL;
    }
}
