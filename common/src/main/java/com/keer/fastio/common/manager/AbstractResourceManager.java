package com.keer.fastio.common.manager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 张经伦
 * @date 2025/12/17 21:42
 * @description:
 */
public abstract class AbstractResourceManager implements Manager {
    // 初始化和关闭状态
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * 获取初始化顺序
     *
     * @return 顺序值，值越小优先级越高
     */
    public abstract int getOrder();

    /**
     * 子类实现具体的初始化逻辑
     */
    protected abstract void doInit() throws Exception;

    /**
     * 子类实现具体的关闭逻辑
     */
    protected abstract void doClose() throws Exception;

    @Override
    public final void init() throws Exception {
        if (initialized.get()) {
            return;
        }
        if (closed.get()) {
            throw new IllegalStateException("Cannot initialize after close");
        }

        doInit();
        initialized.set(true);
    }

    @Override
    public final void close() throws Exception {
        if (closed.get()) {
            return;
        }

        if (initialized.get()) {
            doClose();
        }

        initialized.set(false);
        closed.set(true);
    }

    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * 检查是否已关闭
     */
    public boolean isClosed() {
        return closed.get();
    }

    /**
     * 自动注册到RootResourceManager
     * 可以在子类构造函数中调用此方法
     */
    protected void autoRegister() {
        RootResourceManager.getInstance().register(this);
    }
}
