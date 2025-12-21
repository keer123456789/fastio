package com.keer.fastio.storage.handle.write;

import com.keer.fastio.common.constant.Constants;
import com.keer.fastio.common.entity.MultipartUploadMeta;
import com.keer.fastio.common.entity.PartMeta;
import com.keer.fastio.common.lock.LockLease;
import com.keer.fastio.common.manager.RootResourceManager;
import com.keer.fastio.common.utils.JsonUtil;
import com.keer.fastio.storage.handle.ObjectWriteHandle;
import com.keer.fastio.storage.manager.RocksDbManager;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author 张经伦
 * @date 2025/12/20 20:29
 * @description:
 */
public class DefaultPartWriteHandle implements ObjectWriteHandle<MultipartUploadMeta> {
    private WritableByteChannel channel;
    //对象锁
    private LockLease lock;
    private MultipartUploadMeta meta;
    private Path partPath;
    private RocksDbManager rocksDbManager;
    private boolean commitOrAbort = false;
    private int index;

    public DefaultPartWriteHandle(Path partPath, int index, LockLease lock, MultipartUploadMeta meta) throws IOException {
        this.partPath = partPath;
        this.index = index;
        this.lock = lock;
        this.meta = meta;
        this.rocksDbManager = RootResourceManager.getInstance().getManager(RocksDbManager.class);
        this.channel = Files.newByteChannel(this.partPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }


    @Override
    public WritableByteChannel openWriteChannel() {
        return channel;
    }

    @Override
    public MultipartUploadMeta commit(long total, String eTag) {
        if (commitOrAbort) {
            throw new IllegalStateException("ObjectWriteHandle has already been committed or aborted");
        }
        try {
            PartMeta partMeta = new PartMeta();
            partMeta.setPartNumber(index);
            partMeta.setPath(partPath.toString());
            partMeta.setEtag(eTag);
            partMeta.setSize(total);
            String key = Constants.CACHE_Multi_PREFIX + meta.getBucket() + "/" + meta.getUploadId();
            //重新获取元数据，防止元数据异常
            meta = JsonUtil.fromJson(rocksDbManager.get(key), MultipartUploadMeta.class);
            meta.putPart(index, partMeta);
            rocksDbManager.put(key, JsonUtil.toJson(meta));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            this.lock.unlock();
            this.lock.release();
        }
        return meta;
    }

    @Override
    public void abort() {
        if (commitOrAbort) {
            return;
        }
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            Files.deleteIfExists(partPath);
        } catch (IOException ignored) {
        }
        this.lock.unlock();
        this.lock.release();
    }

    @Override
    public void close() throws Exception {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        channel = null;
    }
}
