package com.keer.fastio.storage;

import com.keer.fastio.common.entity.BucketMeta;
import com.keer.fastio.common.entity.MultipartUploadMeta;
import com.keer.fastio.common.entity.ObjectMeta;
import com.keer.fastio.storage.handle.ObjectReadHandle;
import com.keer.fastio.storage.handle.ObjectWriteHandle;
import com.keer.fastio.storage.request.*;

import java.nio.channels.ReadableByteChannel;
import java.util.List;

/**
 * @author 张经伦
 * @date 2025/12/14 16:58
 * @description: 存储层对外接口
 */
public interface StorageFacade {
    // Bucket

    /**
     * 创建桶
     *
     * @param bucket
     */
    void createBucket(String bucket);

    /**
     * 删除桶
     *
     * @param bucket
     */
    void deleteBucket(String bucket);

    /**
     * 判断桶是否存在
     *
     * @param bucket
     * @return
     */
    boolean bucketExists(String bucket);

    /**
     * 获取所有桶
     *
     * @return
     */
    List<BucketMeta> listBuckets();

    // Object
    ObjectWriteHandle putObject(PutObjectRequest req);
    /**
     * 获取对象
     *
     * @param request
     * @return
     */
    ObjectReadHandle getObject(GetObjectRequest request);

    /**
     * 获取对象元数据
     *
     * @param bucket
     * @param key
     * @return
     */
    ObjectMeta headObject(String bucket, String key);

    /**
     * 删除对象
     *
     * @param bucket
     * @param key
     */
    void deleteObject(String bucket, String key);

    // Listing
    List<ObjectMeta> listObjects(ListObjectsRequest request);

    // Multipart

    /**
     * 初始化断点续传
     *
     * @param bucket
     * @param key
     * @return 断点续传ID
     */
    String initiateMultipartUpload(String bucket, String key);

    /**
     * 上传分片
     *
     * @param request
     */
    ObjectWriteHandle uploadPart(UploadPartRequest request);

    /**
     * 完成断点续传
     * 合并入库
     *
     * @param request
     */
    void completeMultipartUpload(CompleteMultipartRequest request);

    /**
     * 终止断点续传
     *
     * @param bucket
     * @param uploadId
     */
    void abortMultipartUpload(String bucket,String uploadId);

    /**
     * 获取断点续传元数据
     *
     * @param uploadId
     * @return
     */
    MultipartUploadMeta getMultipartUpload(String bucket, String uploadId);
}
