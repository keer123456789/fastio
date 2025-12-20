package com.keer.fastio.common.config;

/**
 * @author 张经伦
 * @date 2025/12/20 14:25
 * @description: 全局配置类
 */
public class FastIoConfig {
    private ApiConfig api;
    private StorageConfig storage;

    public ApiConfig getApi() {
        return api;
    }

    public void setApi(ApiConfig api) {
        this.api = api;
    }

    public StorageConfig getStorage() {
        return storage;
    }

    public void setStorage(StorageConfig storage) {
        this.storage = storage;
    }
}

