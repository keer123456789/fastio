package com.keer.fastio.api.entity;

/**
 * @author 张经伦
 * @date 2025/12/27 9:52
 * @description:
 */
public class PathInfo {
    public String path;
    public String[] paths;

    public PathInfo(String path) {
        this.path = path;
        if (this.path != null) {
            this.paths = this.path.split("/");
        }
    }

    public String getIndex(int index) {
        if (index >= paths.length || index < 0) {
            return null;
        }
        return paths[index];
    }
}
