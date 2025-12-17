package com.keer.fastio.common.entity;

import java.nio.file.Path;

/**
 * @author 张经伦
 * @date 2025/12/14 17:22
 * @description: 断点续传分片 元数据
 */
public class PartMeta {
    private int partNumber;
    private String etag;
    private long size;
    private Path path;

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
