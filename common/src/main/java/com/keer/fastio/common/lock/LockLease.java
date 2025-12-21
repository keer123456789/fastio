package com.keer.fastio.common.lock;

import java.io.Closeable;
import java.util.concurrent.locks.Lock;

/**
 * @author 张经伦
 * @date 2025/12/21 14:20
 * @description:
 */
public class LockLease implements Closeable {
    private final Lock lock;
    private final Runnable releaseCallback;
    private volatile boolean released = false;

    public LockLease(Lock lock, Runnable releaseCallback) {
        this.lock = lock;
        this.releaseCallback = releaseCallback;
    }

    public void lock() {
        ensureNotReleased();
        lock.lock();
    }

    public void lockInterruptibly() throws InterruptedException {
        ensureNotReleased();
        lock.lockInterruptibly();
    }

    public boolean tryLock() {
        ensureNotReleased();
        return lock.tryLock();
    }

    public boolean tryLock(long time, java.util.concurrent.TimeUnit unit) throws InterruptedException {
        ensureNotReleased();
        return lock.tryLock(time, unit);
    }

    public void unlock() {
        ensureNotReleased();
        lock.unlock();
    }

    public java.util.concurrent.locks.Condition newCondition() {
        ensureNotReleased();
        return lock.newCondition();
    }

    /**
     * 释放租约（通知管理器：该锁可被回收）
     * 可多次调用，但只有第一次生效
     */
    public void release() {
        if (!released) {
            released = true;
            releaseCallback.run();
        }
    }

    @Override
    public void close() {
        release(); // 支持 try-with-resources
    }

    private void ensureNotReleased() {
        if (released) {
            throw new IllegalStateException("LockLease has been released");
        }
    }
}
