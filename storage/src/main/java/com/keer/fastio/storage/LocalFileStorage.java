package com.keer.fastio.storage;

import com.keer.fastio.common.entity.BucketMeta;
import com.keer.fastio.common.entity.MultipartUploadMeta;
import com.keer.fastio.common.entity.ObjectMeta;
import com.keer.fastio.common.enums.ExceptionErrorMsg;
import com.keer.fastio.common.exception.ServiceException;
import com.keer.fastio.common.manager.RootResourceManager;
import com.keer.fastio.storage.handler.ObjectReadHandle;
import com.keer.fastio.storage.manager.LocalDiskManager;
import com.keer.fastio.storage.manager.RocksDbManager;
import com.keer.fastio.common.utils.FileUtils;
import com.keer.fastio.common.utils.JsonUtil;
import com.keer.fastio.storage.request.*;
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

    private LocalDiskManager localDiskManager;
    private RocksDbManager dbManager;

    public LocalFileStorage() {
        this.localDiskManager = RootResourceManager.getInstance().getManager(LocalDiskManager.class);
        this.dbManager = RootResourceManager.getInstance().getManager(RocksDbManager.class);
    }

    @Override
    public void createBucket(String bucket) {
        if (!bucketExists(bucket)) {
            logger.warn("bucket has alread exist! {}", bucket);
            throw new ServiceException(ExceptionErrorMsg.BucketExists);
        }
        //只是逻辑建桶
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
            //TODO 修改本地文件夹 bucket 名称，添加 DEL前缀 异步删除 通过localDiskManager
        }

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
