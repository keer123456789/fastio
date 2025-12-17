package com.keer.fastio.common.hash;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * @author 张经伦
 * @date 2025/12/16 21:20
 * @description:
 */
public class MurmurHash3_128 {

    // 64位常量
    private static final long C1_128 = 0x87c37b91114253d5L;
    private static final long C2_128 = 0x4cf5ad432745937fL;

    /**
     * 128位哈希结果容器
     */
    public static class Hash128 {
        public final long h1;
        public final long h2;

        public Hash128(long h1, long h2) {
            this.h1 = h1;
            this.h2 = h2;
        }

        @Override
        public String toString() {
            return String.format("%016x%016x", h1, h2);
        }

        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(16)
                    .order(ByteOrder.LITTLE_ENDIAN);
            buffer.putLong(h1);
            buffer.putLong(h2);
            return buffer.array();
        }
    }

    /**
     * 计算128位MurmurHash3
     */
    public static Hash128 hash128(byte[] data, int offset, int length, long seed) {
        long h1 = seed;
        long h2 = seed;

        int roundedEnd = offset + (length & 0xfffffff0); // 16字节对齐

        // 处理16字节块
        for (int i = offset; i < roundedEnd; i += 16) {
            long k1 = ByteBuffer.wrap(data, i, 8)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getLong();
            long k2 = ByteBuffer.wrap(data, i + 8, 8)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getLong();

            // 混合k1
            k1 *= C1_128;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= C2_128;
            h1 ^= k1;

            h1 = Long.rotateLeft(h1, 27);
            h1 += h2;
            h1 = h1 * 5 + 0x52dce729;

            // 混合k2
            k2 *= C2_128;
            k2 = Long.rotateLeft(k2, 33);
            k2 *= C1_128;
            h2 ^= k2;

            h2 = Long.rotateLeft(h2, 31);
            h2 += h1;
            h2 = h2 * 5 + 0x38495ab5;
        }

        // 处理剩余字节 (0-15字节)
        long k1 = 0;
        long k2 = 0;
        int remaining = length - (roundedEnd - offset);

        if (remaining > 0) {
            for (int i = roundedEnd + remaining - 1, shift = 0; i >= roundedEnd; i--, shift += 8) {
                if (shift < 64) {
                    k2 ^= ((long) (data[i] & 0xFF)) << shift;
                } else {
                    k1 ^= ((long) (data[i] & 0xFF)) << (shift - 64);
                }
            }

            if (remaining >= 8) {
                k1 *= C1_128;
                k1 = Long.rotateLeft(k1, 31);
                k1 *= C2_128;
                h1 ^= k1;
            }

            if (remaining >= 16) {
                k2 *= C2_128;
                k2 = Long.rotateLeft(k2, 33);
                k2 *= C1_128;
                h2 ^= k2;
            }
        }

        // 最终混合
        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        h2 += h1;

        return new Hash128(h1, h2);
    }

    /**
     * 64位最终混合函数
     */
    private static long fmix64(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;
        return k;
    }

    /**
     * 字符串哈希
     */
    public static Hash128 hash128(String str, long seed) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        return hash128(bytes, 0, bytes.length, seed);
    }
}
