package com.keer.fastio.common.entity;

/**
 * @author 张经伦
 * @date 2025/12/14 15:46
 * @description: 桶的元数据
 */
public class BucketMeta {
    /**
     * 桶的名称
     */
    private String name;
    /**
     * 桶的创建时间
     */
    private long createTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
