package com.keer.fastio.storage;

import com.keer.fastio.common.constant.Constants;
import com.keer.fastio.common.entity.BucketMeta;
import com.keer.fastio.common.entity.MultipartUploadMeta;
import com.keer.fastio.common.entity.ObjectMeta;
import com.keer.fastio.common.entity.PartMeta;
import com.keer.fastio.common.enums.ExceptionErrorMsg;
import com.keer.fastio.common.enums.ObjectStatus;
import com.keer.fastio.common.exception.ServiceException;
import com.keer.fastio.common.lock.LockLease;
import com.keer.fastio.common.manager.RootResourceManager;
import com.keer.fastio.common.utils.HashUtils;
import com.keer.fastio.common.utils.JsonUtil;
import com.keer.fastio.storage.entity.LocalStorageUnit;
import com.keer.fastio.storage.entity.LockKeys;
import com.keer.fastio.storage.handle.ObjectWriteHandle;
import com.keer.fastio.storage.handle.read.DefaultObjectReadHandle;
import com.keer.fastio.storage.handle.ObjectReadHandle;
import com.keer.fastio.storage.handle.write.DefaultObjectWriteHandle;
import com.keer.fastio.storage.handle.write.DefaultPartWriteHandle;
import com.keer.fastio.storage.manager.LocalDiskManager;
import com.keer.fastio.common.lock.ObjectLockManager;
import com.keer.fastio.storage.manager.RocksDbManager;
import com.keer.fastio.storage.request.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 张经伦
 * @date 2025/12/14 17:42
 * @description:
 */
public class LocalFileStorage implements StorageFacade {
    private static final Logger logger = LoggerFactory.getLogger(LocalFileStorage.class);

    private LocalDiskManager localDiskManager;
    private RocksDbManager dbManager;
    private ObjectLockManager objectLockManager;

    public LocalFileStorage() {
        this.localDiskManager = RootResourceManager.getInstance().getManager(LocalDiskManager.class);
        this.dbManager = RootResourceManager.getInstance().getManager(RocksDbManager.class);
        this.objectLockManager = RootResourceManager.getInstance().getManager(ObjectLockManager.class);
    }

    @Override
    public void createBucket(String bucket) {
        if (bucketExists(bucket)) {
            logger.warn("bucket has alread exist! {}", bucket);
            throw new ServiceException(ExceptionErrorMsg.BucketExists);
        }
        //只是逻辑建桶
        BucketMeta bucketMeta = new BucketMeta();
        bucketMeta.setName(bucket);
        bucketMeta.setCreateTime(System.currentTimeMillis());
        String jsonValue = JsonUtil.toJson(bucketMeta);
        dbManager.put(Constants.CACHE_BUCKET_PREFIX + bucket, jsonValue);
    }

    @Override
    public void deleteBucket(String bucket) {
        if (bucketExists(bucket)) {
            dbManager.delete(Constants.CACHE_BUCKET_PREFIX + bucket);
            //修改本地文件夹 bucket 名称，添加 DEL前缀 通过localDiskManager异步删除
            for (LocalStorageUnit disk : this.localDiskManager.getDisks()) {
                Path source = Paths.get(disk.getPath(), bucket);
                Path target = Paths.get(disk.getPath(), "DEL_" + bucket);
                if (Files.exists(source)) {
                    try {
                        Files.move(source, target);
                    } catch (Exception e) {
                        logger.error("删除bucket失败：node_id:{}，node_path:{},bucket={},error_msg:{}", disk.getId(), disk.getPath(), bucket, e.getMessage());
                        throw new ServiceException(e);
                    }
                }
            }
        }

    }

    @Override
    public BucketMeta headBucket(String bucket) {
        if (bucketExists(bucket)) {
            return JsonUtil.fromJson(dbManager.get(Constants.CACHE_BUCKET_PREFIX + bucket), BucketMeta.class);
        }
        return null;
    }

    @Override
    public boolean bucketExists(String bucket) {
        return dbManager.exists(bucket);
    }

    @Override
    public List<BucketMeta> listBuckets() {
        List<String> results = dbManager.queryByStartPrefix(Constants.CACHE_BUCKET_PREFIX);
        if (results.size() == 0) {
            return Collections.emptyList();
        }
        return results.stream().map(s -> JsonUtil.fromJson(s, BucketMeta.class)).collect(Collectors.toList());
    }


    @Override
    public ObjectWriteHandle putObject(PutObjectRequest request) {
        String hashKey = request.getBucket() + request.getKey();
        String hashStr = HashUtils.hexHash(hashKey);
        LocalStorageUnit unit = localDiskManager.selectUnit(hashKey, LocalDiskManager.WRITE_MODEL);
        if (unit == null) {
            throw new ServiceException(ExceptionErrorMsg.FileNoDiskWriteFail);
        }
        Path finalPath = Paths.get(unit.getPath(), request.getBucket(), hashStr.substring(0, 2), hashStr.substring(2, 4), hashStr.substring(4, 6), request.getKey());
        LockLease lockLease = objectLockManager.acquireWriteLock(LockKeys.object(request.getBucket(), request.getKey()));
        lockLease.lock();
        try {
            //修改对象状态
            ObjectMeta meta = new ObjectMeta();
            meta.setCreateTime(System.currentTimeMillis());
            meta.setModifiedTime(meta.getCreateTime());
            meta.setBucketName(request.getBucket());
            meta.setKey(request.getKey());
            meta.setPhysicalPath(finalPath.toString());
            meta.setStatus(ObjectStatus.PREPARE);
            dbManager.put(buildObjectKey(meta.getBucketName(), meta.getKey()), JsonUtil.toJson(meta));

            //构建临时文件
            String tempId = UUID.randomUUID().toString().replace("-", "");
            Path tempPath = Paths.get(unit.getPath(), request.getBucket(), ".temp", tempId + ".data");
            if (!Files.exists(tempPath.getParent())) {
                try {
                    Files.createDirectories(tempPath.getParent());
                } catch (IOException e) {
                    throw new ServiceException(e);
                }
            }

            return new DefaultObjectWriteHandle(tempPath, lockLease, meta);
        } catch (Exception e) {
            lockLease.unlock();
            throw new ServiceException(e);
        }

    }

    @Override
    public ObjectReadHandle getObject(GetObjectRequest request) {
        LockLease lock = this.objectLockManager.acquireReadLock(LockKeys.object(request.getBucket(), request.getKey()));
        lock.lock();
        ObjectMeta meta = headObject(request.getBucket(), request.getKey());
        if (meta == null) {
            lock.unlock();
            return null;
        }
        if (meta.getStatus() != ObjectStatus.VISIBLE) {
            lock.unlock();
            return null;
        }
        return new DefaultObjectReadHandle(meta, request.isHeadOnly(), lock);
    }

    @Override
    public ObjectMeta headObject(String bucket, String key) {
        String meta = dbManager.get(buildObjectKey(bucket, key));
        if (meta == null) {
            return null;
        }
        return JsonUtil.fromJson(meta, ObjectMeta.class);
    }

    @Override
    public void deleteObject(String bucket, String key) {
        LockLease lock = objectLockManager.acquireWriteLock(LockKeys.object(bucket, key));
        lock.lock();
        try {
            ObjectMeta meta = headObject(bucket, key);
            if (meta == null) {
                lock.unlock();
                return;
            }
            meta.setStatus(ObjectStatus.DELETING);
            dbManager.put(buildObjectKey(bucket, key), JsonUtil.toJson(meta));

            Files.deleteIfExists(Paths.get(meta.getPhysicalPath()));
            dbManager.delete(buildObjectKey(bucket, key));
        } catch (IOException e) {
            throw new ServiceException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<ObjectMeta> listObjects(ListObjectsRequest request) throws ServiceException {
        if (request.getBucket() == null || request.getBucket().equals("")) {
            throw new ServiceException(ExceptionErrorMsg.BucketIsNull);
        }



        return Collections.emptyList();
    }

    @Override
    public String initiateMultipartUpload(String bucket, String key) {
        String uploadId = UUID.randomUUID().toString().replace("-", "");
        MultipartUploadMeta meta = new MultipartUploadMeta();
        meta.setUploadId(uploadId);
        meta.setBucket(bucket);
        meta.setKey(key);
        meta.setCreateTime(System.currentTimeMillis());

        String hashKey = bucket + key;
        LocalStorageUnit unit = localDiskManager.selectUnit(hashKey, LocalDiskManager.WRITE_MODEL);
        if (unit == null) {
            throw new ServiceException(ExceptionErrorMsg.FileNoDiskWriteFail);
        }
        meta.setDiskPath(unit.getPath());
        dbManager.put(buildMultiKey(bucket, uploadId), JsonUtil.toJson(meta));
        return uploadId;
    }

    @Override
    public ObjectWriteHandle uploadPart(UploadPartRequest request) {
        MultipartUploadMeta meta = getMultipartUpload(request.getBucketName(), request.getUploadId());
        Path path = Paths.get(meta.getDiskPath(), meta.getBucket(), ".multipart", meta.getUploadId(), "part." + request.getIndex());
        try {
            Files.createDirectories(path.getParent());
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        LockLease lock = objectLockManager.acquireWriteLock(LockKeys.multipart(meta.getBucket(), request.getUploadId()));
        lock.lock();
        try {
            return new DefaultPartWriteHandle(path, request.getIndex(), lock, meta);
        } catch (Exception e) {
            lock.unlock();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void completeMultipartUpload(CompleteMultipartRequest request) {
        MultipartUploadMeta meta = getMultipartUpload(request.getBucket(), request.getUploadId());
        if (meta == null) {
            return;
        }
        LockLease lock = objectLockManager.acquireWriteLock(LockKeys.multipart(meta.getBucket(), request.getUploadId()));
        LockLease objectLock = objectLockManager.acquireWriteLock(LockKeys.object(meta.getBucket(), meta.getKey()));
        objectLock.lock();
        lock.lock();
        try {
            String tempId = UUID.randomUUID().toString().replace("-", "");
            Path tempPath = Paths.get(meta.getDiskPath(), meta.getBucket(), ".temp", tempId + ".data");
            List<Path> partPaths = new LinkedList<>();
            for (Map.Entry<Integer, PartMeta> e : meta.getParts().entrySet()) {
                Path partPath = Paths.get(e.getValue().getPath());
                partPaths.add(partPath);
            }
            //写入文件
            LocalDiskManager.WriteResult result = localDiskManager.mergerPart(partPaths, tempPath, 1024 * 10);
            String hashStr = HashUtils.hexHash(meta.getBucket() + meta.getKey());
            Path finalPath = Paths.get(meta.getDiskPath(), meta.getBucket(), hashStr.substring(0, 2), hashStr.substring(2, 4), hashStr.substring(4, 6), meta.getKey());


            try {
                Files.createDirectories(finalPath.getParent());
                Files.move(tempPath, finalPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                throw new ServiceException(e);
            }

            ObjectMeta objectMeta = new ObjectMeta();
            objectMeta.setCreateTime(System.currentTimeMillis());
            objectMeta.setModifiedTime(meta.getCreateTime());
            objectMeta.setBucketName(meta.getBucket());
            objectMeta.setKey(meta.getKey());
            objectMeta.setSize(result.getTotalSize());
            objectMeta.setEtag(result.getEtag());
            objectMeta.setPhysicalPath(finalPath.toString());
            dbManager.put(buildObjectKey(meta.getBucket(), meta.getKey()), JsonUtil.toJson(objectMeta));


            dbManager.delete(buildMultiKey(request.getBucket(), request.getUploadId()));
        } finally {
            lock.unlock();
            objectLock.unlock();
        }
    }

    @Override
    public void abortMultipartUpload(String bucket, String uploadId) {
        MultipartUploadMeta meta = getMultipartUpload(bucket, uploadId);
        LockLease lock = objectLockManager.acquireWriteLock(LockKeys.multipart(meta.getBucket(), uploadId));
        lock.lock();
        try {
            dbManager.delete(Constants.CACHE_Multi_PREFIX + uploadId);
            if (meta != null) {
                Path path = Paths.get(meta.getDiskPath(), meta.getBucket(), ".multipart", meta.getUploadId());
                try {
                    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            // 删除文件
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (Exception e) {
                    throw new ServiceException(e);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public MultipartUploadMeta getMultipartUpload(String bucket, String uploadId) {
        String meta = dbManager.get(buildMultiKey(bucket, uploadId));
        if (meta == null) {
            return null;
        }
        return JsonUtil.fromJson(meta, MultipartUploadMeta.class);
    }

    private static String buildObjectKey(String bucketName, String key) {
        return Constants.CACHE_OBJECT_PREFIX + bucketName + "/" + key;
    }

    private static String buildMultiKey(String bucketName, String uploadId) {
        return Constants.CACHE_Multi_PREFIX + bucketName + "/" + uploadId;
    }

}
