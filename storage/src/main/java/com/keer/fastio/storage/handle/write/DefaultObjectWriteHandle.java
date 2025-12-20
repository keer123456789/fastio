package com.keer.fastio.storage.handle.write;

import com.keer.fastio.common.constant.Constants;
import com.keer.fastio.common.entity.ObjectMeta;
import com.keer.fastio.common.enums.ObjectStatus;
import com.keer.fastio.common.manager.RootResourceManager;
import com.keer.fastio.common.utils.JsonUtil;
import com.keer.fastio.storage.handle.ObjectWriteHandle;
import com.keer.fastio.storage.manager.RocksDbManager;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.file.*;
import java.util.concurrent.locks.Lock;

/**
 * @author 张经伦
 * @date 2025/12/20 17:28
 * @description:
 */
public class DefaultObjectWriteHandle implements ObjectWriteHandle<ObjectMeta> {

    private WritableByteChannel channel;
    //对象锁
    private Lock lock;
    private ObjectMeta meta;
    private Path tempPath;
    private RocksDbManager rocksDbManager;
    private boolean commitOrAbort=false;

    public DefaultObjectWriteHandle(Path tempPath, Lock lock, ObjectMeta meta) throws IOException {
        this.tempPath = tempPath;
        this.lock = lock;
        this.meta = meta;
        this.rocksDbManager = RootResourceManager.getInstance().getManager(RocksDbManager.class);
        this.channel = Files.newByteChannel(this.tempPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public WritableByteChannel openWriteChannel() {
        return channel;
    }


    @Override
    public ObjectMeta commit() {
        if (commitOrAbort) {
            throw new IllegalStateException("ObjectWriteHandle has already been committed or aborted");
        }
        Path finalPath = Paths.get(meta.getPhysicalPath());
        try {
            Files.createDirectories(finalPath.getParent());
            Files.move(tempPath, finalPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

            meta.setSize(Files.size(finalPath));
            //Etag 这个问题需要考虑大文件计算时间久的问题
//            meta.setEtag(eTag);
            meta.setPhysicalPath(finalPath.toString());
            meta.setStatus(ObjectStatus.VISIBLE);
            String key = Constants.CACHE_OBJECT_PREFIX + meta.getBucketName() + "/" + meta.getKey();
            rocksDbManager.put(key, JsonUtil.toJson(meta));
            return meta;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            this.lock.unlock();
        }
    }

    @Override
    public void abort() {
        if(commitOrAbort){return;}
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            Files.deleteIfExists(tempPath);
            this.rocksDbManager.delete(Constants.CACHE_OBJECT_PREFIX + meta.getBucketName() + "/" + meta.getKey());
        } catch (IOException ignored) {
        }
        this.lock.unlock();
    }

    @Override
    public void close() throws Exception {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        channel=null;
    }
}
