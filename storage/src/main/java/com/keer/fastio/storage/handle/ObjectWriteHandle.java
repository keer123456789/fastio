package com.keer.fastio.storage.handle;

import com.keer.fastio.common.entity.ObjectMeta;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

/**
 * @author 张经伦
 * @date 2025/12/20 17:26
 * @description:
 */
public interface  ObjectWriteHandle<T> extends AutoCloseable {
    /* push 模式 */
    WritableByteChannel openWriteChannel();

    T commit(long total,String eTag) throws IOException;

    void abort();

    void close() throws Exception;
}
