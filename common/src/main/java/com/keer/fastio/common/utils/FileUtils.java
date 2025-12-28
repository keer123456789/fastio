package com.keer.fastio.common.utils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author 张经伦
 * @date 2025/12/14 19:51
 * @description:
 */
public class FileUtils {
    public static boolean mkdirs(String path) {
        File file = new File(path);
        if (!file.exists()) {
           return file.mkdirs();
        }
        return true;
    }
    /**
     * 将大文件按指定大小切分成多个小文件
     *
     * @param sourceFilePath 源文件路径
     * @param chunkSize      每个分片的大小（字节）
     * @param outputDir      输出目录路径
     */
    public static void splitFile(String sourceFilePath, long chunkSize, String outputDir) throws IOException {
        Path sourcePath = Paths.get(sourceFilePath);
        Path outputDirectory = Paths.get(outputDir);

        // 确保输出目录存在
        if (!outputDirectory.toFile().exists()) {
            outputDirectory.toFile().mkdirs();
        }

        try (FileChannel sourceChannel = FileChannel.open(sourcePath, StandardOpenOption.READ)) {
            long fileSize = sourceChannel.size();
            long position = 0;
            int partNumber = 1;

            ByteBuffer buffer = ByteBuffer.allocate((int) Math.min(chunkSize, Integer.MAX_VALUE));

            while (position < fileSize) {
                // 构建分片文件名
                String partFileName = sourcePath.getFileName().toString() + ".part" + String.format("%03d", partNumber);
                Path partPath = outputDirectory.resolve(partFileName);

                try (FileChannel partChannel = FileChannel.open(partPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                    long remaining = fileSize - position;
                    long currentChunkSize = Math.min(chunkSize, remaining);

                    // 使用 transferTo 高效复制（可选方式）
                    long transferred = 0;
                    while (transferred < currentChunkSize) {
                        long count = sourceChannel.transferTo(position + transferred, currentChunkSize - transferred, partChannel);
                        if (count <= 0) break; // 防止无限循环
                        transferred += count;
                    }

                    position += currentChunkSize;
                    partNumber++;
                }
            }
        }
    }
    // 测试方法
    public static void main(String[] args) {
        String sourceFile = "C:\\Users\\15110\\Desktop\\windows工具箱\\20251216144548.WAV";   // 替换为你的大文件路径
        String outputDir = "C:\\Users\\15110\\Desktop\\windows工具箱\\";          // 替换为输出目录
        long chunkSize = 50 * 1024 * 1024;             // 100 MB

        try {
            splitFile(sourceFile, chunkSize, outputDir);
            System.out.println("文件切分完成！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
