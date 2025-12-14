package com.keer.fastio.utils;

import java.io.File;

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
}
