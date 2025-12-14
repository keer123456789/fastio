package com.keer.fastio;

import com.keer.fastio.entity.BucketMeta;
import com.keer.fastio.entity.MultipartUploadMeta;
import com.keer.fastio.entity.ObjectMeta;
import com.keer.fastio.enums.ExceptionErrorMsg;
import com.keer.fastio.exception.ServiceException;
import com.keer.fastio.handler.ObjectReadHandle;
import com.keer.fastio.manager.RocksDbManager;
import com.keer.fastio.request.*;
import com.keer.fastio.utils.FileUtils;
import com.keer.fastio.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author 张经伦
 * @date 2025/12/14 17:42
 * @description:
 */
public class LocalFileStorage implements StorageFacade {
    private static final Logger logger = LoggerFactory.getLogger(LocalFileStorage.class);
    private static final String CACHE_BUCKET_PREFIX = "bucket_";
    private static final String CACHE_OBJECT_PREFIX = "object_";
    /**
     * 系统根目录
     */
    private String rootPath;
    private String cachePath;
    private String objectPath;
    private RocksDbManager dbManager;

    public LocalFileStorage(String rootPath) {
        this.rootPath = rootPath;
        this.cachePath = rootPath + "/cache";
        FileUtils.mkdirs(cachePath);
        this.objectPath = rootPath + "/object";
        FileUtils.mkdirs(objectPath);
        this.dbManager = RocksDbManager.getInstance(this.cachePath);
    }

    @Override
    public void createBucket(String bucket) {
        if (!bucketExists(bucket)) {
            logger.warn("bucket has alread exist! {}", bucket);
            throw new ServiceException(ExceptionErrorMsg.BucketExists);
        }
        String path = objectPath + "/" + bucket;
        if (!FileUtils.mkdirs(path)) {
            throw new ServiceException(ExceptionErrorMsg.FileCreatFail);
        }
        BucketMeta bucketMeta = new BucketMeta();
        bucketMeta.setName(bucket);
        bucketMeta.setCreateTime(System.currentTimeMillis());
        String jsonValue = JsonUtil.toJson(bucketMeta);
        dbManager.put(CACHE_BUCKET_PREFIX + bucket, jsonValue);
    }

    @Override
    public void deleteBucket(String bucket) {
        if (bucketExists(bucket)) {
            dbManager.delete(CACHE_BUCKET_PREFIX + bucket);
        }

    }

    /**
     * 删除文件系统中桶
     * 同时删除桶下的所有文件和文件夹
     * 异步删除
     * 将文件夹名称添加 DEL_ 前缀
     * 通过一个异步线程 循环 便利 找到DEL_前缀文件夹进行删除
     *
     * @param bucket
     */
    private void asyncDeleteBucketLocal(String bucket) {

    }

    @Override
    public boolean bucketExists(String bucket) {
        return dbManager.exists(bucket);
    }

    @Override
    public List<BucketMeta> listBuckets() {
        return Collections.emptyList();
    }

    @Override
    public void putObject(PutObjectRequest request) {

    }

    @Override
    public ObjectReadHandle getObject(GetObjectRequest request) {
        return null;
    }

    @Override
    public ObjectMeta headObject(String bucket, String key) {
        return null;
    }

    @Override
    public void deleteObject(String bucket, String key) {

    }

    @Override
    public List<ObjectMeta> listObjects(ListObjectsRequest request) {
        return Collections.emptyList();
    }

    @Override
    public String initiateMultipartUpload(String bucket, String key) {
        return "";
    }

    @Override
    public void uploadPart(UploadPartRequest request) {

    }

    @Override
    public void completeMultipartUpload(CompleteMultipartRequest request) {

    }

    @Override
    public void abortMultipartUpload(String uploadId) {

    }

    @Override
    public MultipartUploadMeta getMultipartUpload(String uploadId) {
        return null;
    }
}
