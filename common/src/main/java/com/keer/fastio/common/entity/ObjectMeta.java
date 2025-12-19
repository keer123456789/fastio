package com.keer.fastio.common.entity;

import com.keer.fastio.common.enums.ObjectStatus;

/**
 * @author 张经伦
 * @date 2025/12/14 15:47
 * @description: 对象元信息
 */
public class ObjectMeta {
    /**
     * 桶名称
     */
    private String bucketName;
    /**
     * 对象名称
     */
    private String key;
    /**
     * 物理存储路径
     */
    private String physicalPath;
    /**
     * 对象大小
     */
    private long size;
    /**
     * 对象md5
     */
    private String etag;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 最后一次修改时间
     */
    private Long modifiedTime;
    /**
     * 对象状态
     */
    private ObjectStatus status;

    public ObjectStatus getStatus() {
        return status;
    }

    public void setStatus(ObjectStatus status) {
        this.status = status;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPhysicalPath() {
        return physicalPath;
    }

    public void setPhysicalPath(String physicalPath) {
        this.physicalPath = physicalPath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
