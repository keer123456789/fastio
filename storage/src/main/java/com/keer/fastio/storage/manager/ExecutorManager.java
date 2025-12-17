package com.keer.fastio.storage.manager;

import com.keer.fastio.storage.config.ExecutorManagerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Stream;

/**
 * @author 张经伦
 * @date 2025/12/15 21:31
 * @description: 线程管理器
 */
public class ExecutorManager {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorManager.class);
    private static ExecutorManager _instance;
    /**
     * 删除bucket单线程 线程池
     */
    private ExecutorService deleteBucketTaskExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "DBT");
            return t;
        }
    });
    private ExecutorManagerConfig config;

    private ExecutorManager(ExecutorManagerConfig config) {

        initDeleteBucketTask();
    }


    public static ExecutorManager getInstance(ExecutorManagerConfig config) {
        if (_instance == null) {
            _instance = new ExecutorManager(config);
        }
        return _instance;
    }

    /**
     * 初始化删除bucket 任务
     */
    private void initDeleteBucketTask() {
        deleteBucketTaskExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {

                for (String path : config.getDataPaths()) {
                    Path p = Paths.get(path);
                    if (!Files.exists(p) || !Files.isDirectory(p)) {

                        continue;
                    }
                    try (Stream<Path> stream = Files.list(p)) {
                        stream.filter(Files::isDirectory)
                                .filter(ps -> {
                                    String folderName = ps.getFileName().toString();
                                    return folderName.startsWith("DEL_");
                                })
                                .forEach(ps -> {
                                    try {
                                        Files.walkFileTree(ps, new SimpleFileVisitor<Path>() {
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
                                    } catch (IOException e) {
                                        logger.warn("递归删除桶（{}）失败,错误信息：{}", ps.getFileName().getFileName(), e.getMessage());
                                    }
                                });
                    } catch (IOException e) {
                        logger.warn("查找路径（{}）下的文件夹失败，导致本次删除桶任务失败,错误信息：{}", path, e.getMessage());
                    }
                }
                try {
                    Thread.sleep(config.getDeleteBucketTaskInterval());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }
}
