package com.keer.fastio.common.hash;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * @author 张经伦
 * @date 2025/12/16 21:19
 * @description:
 */
public class MurmurHash3_32 {


    // 常量定义
    private static final int C1_32 = 0xcc9e2d51;
    private static final int C2_32 = 0x1b873593;
    private static final int R1_32 = 15;
    private static final int R2_32 = 13;
    private static final int M_32 = 5;
    private static final int N_32 = 0xe6546b64;

    /**
     * 计算32位MurmurHash3
     *
     * @param data   输入数据
     * @param offset 起始偏移量
     * @param length 数据长度
     * @param seed   种子值
     * @return 32位哈希值
     */
    public static int hash32(byte[] data, int offset, int length, int seed) {
        int hash = seed;
        int roundedEnd = offset + (length & 0xfffffffc); //4字节对齐

        //处理4字节块
        for (int i = offset; i < roundedEnd; i += 4) {
            // 小端序读取4字节
            int k = ByteBuffer.wrap(data, i, 4)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getInt();

            // 混合步骤
            k *= C1_32;
            k = Integer.rotateLeft(k, R1_32);
            k *= C2_32;

            hash ^= k;
            hash = Integer.rotateLeft(hash, R2_32);
            hash = hash * M_32 + N_32;
        }

        // 处理剩余字节 (0-3字节)
        int k = 0;
        int shift = 0;
        for (int i = roundedEnd; i < offset + length; i++) {
            k ^= (data[i] & 0xFF) << shift;
            shift += 8;
        }

        if (shift > 0) {
            k *= C1_32;
            k = Integer.rotateLeft(k, R1_32);
            k *= C2_32;
            hash ^= k;
        }

        // 最终混合
        hash ^= length;
        hash ^= hash >>> 16;
        hash *= 0x85ebca6b;
        hash ^= hash >>> 13;
        hash *= 0xc2b2ae35;
        hash ^= hash >>> 16;

        return hash;
    }

    /**
     * 字符串哈希 (UTF-8编码)
     */
    public static int hash32(String str, int seed) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        return hash32(bytes, 0, bytes.length, seed);
    }

    /**
     * 默认种子为0的版本
     */
    public static int hash32(byte[] data) {
        return hash32(data, 0, data.length, 0);
    }


    public static void main(String[] args) {
        System.out.println(hash32("/data1" ,1));
    }

}
