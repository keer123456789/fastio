package com.keer.fastio.common.hash;

/**
 * @author 张经伦
 * @date 2025/12/18 09:20
 * @description: 哈希环接口
 */
public interface HashRing<T> {
    /**
     * 根据给定的key进行挑选存储单元
     *
     * @param key
     * @return
     */
    T selectUnit(String key);
}
