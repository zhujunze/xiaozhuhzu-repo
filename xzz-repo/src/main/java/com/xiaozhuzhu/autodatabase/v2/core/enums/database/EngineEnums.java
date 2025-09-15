package com.xiaozhuzhu.autodatabase.v2.core.enums.database;

import lombok.Getter;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日10:37
 * @description: 数据库引擎枚举
 */
@Getter
public enum EngineEnums {

    InnoDB("InnoDB", "InnoDB 是 MySQL 的默认存储引擎，提供事务支持、外键约束、崩溃恢复等功能，适用于大多数复杂的数据库操作"),

    ;


    private final String engine;
    private final String description;

    EngineEnums(String engine, String description) {
        this.engine = engine;
        this.description = description;
    }
}
