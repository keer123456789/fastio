package com.keer.fastio.storage.handle.read;

import com.keer.fastio.common.entity.ObjectMeta;
import com.keer.fastio.common.lock.LockLease;
import com.keer.fastio.storage.handle.ObjectReadHandle;

import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.Lock;

/**
 * @Author: 张经伦
 * @Date: 2025/12/19  11:44
 * @Description:
 */
public class DefaultObjectReadHandle implements ObjectReadHandle {
    private ObjectMeta meta;
    private boolean isOnlyMeta;

    private ReadableByteChannel channel;
    private LockLease readLock;

    public DefaultObjectReadHandle(ObjectMeta meta, boolean isOnlyMeta, LockLease readLock) {
        this.meta = meta;
        this.isOnlyMeta = isOnlyMeta;
        this.readLock = readLock;
    }

    @Override
    public ObjectMeta meta() {
        return this.meta;
    }

    @Override
    public long contentLength() {
        return 0;
    }

    @Override
    public long totalObjectSize() {
        return meta.getSize();
    }

    @Override
    public ReadableByteChannel openChannel() {
        if (isOnlyMeta) {
            return null;
        }
        try {
            this.channel = FileChannel.open(Paths.get(meta.getPhysicalPath()), StandardOpenOption.READ);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.channel;
    }


    @Override
    public void close() {
        try {
            this.channel.close();
        } catch (Exception e) {
        } finally {
            readLock.unlock();
            readLock.release();
        }
    }
}
