package com.keer.fastio.enums;

/**
 * @author 张经伦
 * @date 2025/12/14 16:45
 * @description:
 */
public enum StorageWriteMode {

    NORMAL,        // 普通 PUT
    MULTIPART,     // multipart 最终合并
    REPLICATION,   // 节点复制
    REBALANCE,     // 数据迁移
    RECOVERY       // 崩溃恢复

}
