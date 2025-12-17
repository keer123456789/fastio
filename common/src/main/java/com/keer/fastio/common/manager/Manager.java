package com.keer.fastio.common.manager;

/**
 * 管理接口
 * 一般为资源类管理接口，需要初始化 和关闭的服务接口
 */
public interface Manager extends AutoCloseable {
    /**
     * 初始化资源
     * @throws Exception
     */
    void init() throws Exception;
}
