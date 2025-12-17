package com.keer.fastio.storage.handler;

import com.keer.fastio.common.entity.ObjectMeta;

import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;

/**
 * @author 张经伦
 * @date 2025/12/14 16:58
 * @description:
 */
public interface ObjectReadHandle extends AutoCloseable {

    // ===== 基本信息 =====
    ObjectMeta meta();

    long contentLength();      // 实际返回长度

    long totalObjectSize();    // 原始对象大小

    // ===== 读取方式 =====

    /**
     * 方式 1：Channel（推荐）
     */
    ReadableByteChannel openChannel();

    /**
     * 方式 2：InputStream（兼容）
     */
    InputStream openStream();

    /**
     * 方式 3：零拷贝（Netty / sendfile）
     * TODO 之后做
     */
    boolean supportsZeroCopy();
//    ZeroCopyRegion zeroCopyRegion();

    // ===== 生命周期 =====
    @Override
    void close();
}
