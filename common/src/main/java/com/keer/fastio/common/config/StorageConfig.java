package com.keer.fastio.common.config;

import java.util.List;

/**
 * @author 张经伦
 * @date 2025/12/20 14:47
 * @description:
 */ //本地存储配置
public class StorageConfig {
    private RockDbConfig rockDb;
    private List<StorageUnitConfig> units;

    public RockDbConfig getRockDb() {
        return rockDb;
    }

    public void setRockDb(RockDbConfig rockDb) {
        this.rockDb = rockDb;
    }

    public List<StorageUnitConfig> getUnits() {
        return units;
    }

    public void setUnits(List<StorageUnitConfig> units) {
        this.units = units;
    }
}
