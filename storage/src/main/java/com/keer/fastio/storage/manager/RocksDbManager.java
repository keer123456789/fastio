package com.keer.fastio.storage.manager;

import com.keer.fastio.common.config.RockDbConfig;
import com.keer.fastio.common.manager.AbstractResourceManager;
import com.keer.fastio.common.utils.ByteUtils;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author 张经伦
 * @date 2025/12/14 19:26
 * @description: rocke
 */
public class RocksDbManager extends AbstractResourceManager {
    private static final Logger logger = LoggerFactory.getLogger(RocksDbManager.class);
    private String dbpath;
    private RocksDB db;
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    public RocksDbManager(RockDbConfig config) {
        this.dbpath = config.getPath();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    protected void doInit() throws Exception {
        try {
            final Options options = new Options();
            options.setCreateIfMissing(true);
            options.setCompressionType(CompressionType.LZ4_COMPRESSION); // 启用压缩
            options.setWriteBufferSize(64 * 1024 * 1024); // 64MB 写缓冲
            options.setMaxWriteBufferNumber(3);
            options.setTargetFileSizeBase(64 * 1024 * 1024); // 64MB SST 文件

            File dbDir = new File(this.dbpath);
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }

            db = RocksDB.open(options, this.dbpath);
            logger.info("RocksDB 初始化成功，路径: {}", this.dbpath);
        } catch (Exception e) {
            logger.error("RocksDB 初始化失败", e);
            throw new RuntimeException("无法启动 RocksDB", e);
        }
    }

    @Override
    protected void doClose() throws Exception {
        lock.writeLock().lock();
        try {
            if (db != null) {
                db.close();
                db = null;
                logger.info("RocksDB 已关闭");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    public String getDbpath() {
        return dbpath;
    }

    public void setDbpath(String dbpath) {
        this.dbpath = dbpath;
    }


    public void put(String key, String value) {
        put(key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8));
    }

    // 写入 byte[]
    public void put(byte[] key, byte[] value) {
        lock.writeLock().lock();
        try {
            db.put(key, value);
        } catch (RocksDBException e) {
            logger.error("RocksDB put 失败: key={}", ByteUtils.bytesToHex(key), e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // 读取字符串
    public String get(String key) {
        byte[] val = get(key.getBytes(StandardCharsets.UTF_8));
        return val == null ? null : new String(val, StandardCharsets.UTF_8);
    }

    // 读取 byte[]
    public byte[] get(byte[] key) {
        lock.readLock().lock();
        try {
            return db.get(key);
        } catch (RocksDBException e) {
            logger.error("RocksDB get 失败: key={}", ByteUtils.bytesToHex(key), e);
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    // 删除
    public void delete(String key) {
        delete(key.getBytes(StandardCharsets.UTF_8));
    }

    public void delete(byte[] key) {
        lock.writeLock().lock();
        try {
            db.delete(key);
        } catch (RocksDBException e) {
            logger.error("RocksDB delete 失败: key={}", ByteUtils.bytesToHex(key), e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // 检查是否存在
    public boolean exists(String key) {
        return get(key) != null;
    }


    public List<String> queryByStartPrefix(String prefix) {
        List<String> results = new LinkedList<>();
        // 使用前缀迭代器
        try (final ReadOptions readOptions = new ReadOptions();
             final RocksIterator iterator = db.newIterator(readOptions)) {

            // 查找以 "user_" 开头的所有key
            byte[] prefixBytes = prefix.getBytes();

            for (iterator.seek(prefixBytes); iterator.isValid(); iterator.next()) {
                byte[] key = iterator.key();

                // 检查是否仍然匹配前缀
                if (!startsWith(key, prefixBytes)) {
                    break;
                }

                // 处理匹配的键值对
                byte[] value = iterator.value();
                try {
                    results.add(new String(db.get(value)));
                } catch (Exception e) {

                }
            }
        }
        return results;
    }

    private boolean startsWith(byte[] array, byte[] prefix) {
        if (prefix.length > array.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) return false;
        }
        return true;
    }
}
