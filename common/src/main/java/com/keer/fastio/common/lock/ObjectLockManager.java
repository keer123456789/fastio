package com.keer.fastio.common.lock;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.keer.fastio.common.manager.AbstractResourceManager;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * @Author: 张经伦
 * @Date: 2025/12/19  15:30
 * @Description: 对象锁管理器
 */
public class ObjectLockManager extends AbstractResourceManager {
    private final Cache<String, RefCountedLock> lockCache = Caffeine.newBuilder().maximumSize(10_000)                          // 最多缓存 1 万个锁
            .expireAfterAccess(java.time.Duration.ofMinutes(10)) // 10 分钟未使用自动移除
            .removalListener((key, lock, cause) -> {
                //TODO
            }).build();

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    protected void doInit() {
    }

    @Override
    protected void doClose() {
        lockCache.invalidateAll();
    }

    // ================== 公共 API ==================

    /**
     * 获取读锁租约（必须调用 lease.release() 或 close()）
     */
    public LockLease acquireReadLock(String objectId) {
        if (objectId == null || objectId.isEmpty()) {
            throw new IllegalArgumentException("objectId must not be null or empty");
        }
        RefCountedLock wrapper = lockCache.get(objectId, k -> new RefCountedLock(k, lockCache::invalidate));
        Lock lock = wrapper.readLock(); // 增加引用计数
        return new LockLease(lock, wrapper::release);
    }

    /**
     * 获取写锁租约（必须调用 lease.release() 或 close()）
     */
    public LockLease acquireWriteLock(String objectId) {
        if (objectId == null || objectId.isEmpty()) {
            throw new IllegalArgumentException("objectId must not be null or empty");
        }
        RefCountedLock wrapper = lockCache.get(objectId, k -> new RefCountedLock(k, lockCache::invalidate));
        Lock lock = wrapper.writeLock(); // 增加引用计数
        return new LockLease(lock, wrapper::release);
    }

    // =============================================
    // 内部类：带引用计数的锁包装器
    // =============================================
    static class RefCountedLock {
        private final String key;
        private final Consumer<String> onZeroRefCallback; // 回调：当 refCount==0 时触发
        private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final AtomicInteger refCount = new AtomicInteger(0);

        RefCountedLock(String key, Consumer<String> onZeroRefCallback) {
            this.key = key;
            this.onZeroRefCallback = onZeroRefCallback;
        }

        public Lock readLock() {
            refCount.incrementAndGet();
            return rwLock.readLock();
        }

        public Lock writeLock() {
            refCount.incrementAndGet();
            return rwLock.writeLock();
        }

        public void release() {
            int count = refCount.decrementAndGet();
            if (count < 0) {
                throw new IllegalStateException("Reference count underflow");
            }
            // ✅ 关键：当引用归零，立即通知缓存删除
            if (count == 0) {
                onZeroRefCallback.accept(key);
            }
        }

        public int getRefCount() {
            return refCount.get();
        }
    }

}
