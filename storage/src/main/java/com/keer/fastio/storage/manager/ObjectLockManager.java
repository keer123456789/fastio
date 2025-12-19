package com.keer.fastio.storage.manager;

import com.keer.fastio.common.manager.AbstractResourceManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author: 张经伦
 * @Date: 2025/12/19  15:30
 * @Description: 对象锁管理器
 */
public class ObjectLockManager extends AbstractResourceManager {
    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    protected void doInit() throws Exception {

    }

    @Override
    protected void doClose() throws Exception {

    }

    private final ConcurrentHashMap<String, ReentrantReadWriteLock> lockMap =
            new ConcurrentHashMap<>();

    private ReentrantReadWriteLock getLock(String objectId) {

        return lockMap.computeIfAbsent(objectId, k -> new ReentrantReadWriteLock());
    }

    public Lock readLock(String objectId) {
        return getLock(objectId).readLock();
    }

    public Lock writeLock(String objectId) {
        return getLock(objectId).writeLock();
    }

}
