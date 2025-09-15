package com.xiaozhuzhu.autodatabase.v2.core.enums.database;

import lombok.Getter;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日10:41
 * @description: 数据库类型枚举
 */
@Getter
public enum DatabaseTypeEnum {

    /**
     * MySQL 数据库
     */
    MYSQL("mysql"),
    ;

    private final String type;


    DatabaseTypeEnum(String type) {
        this.type = type;
    }

    /**
     * 获取数据库类型枚举
     *
     * @param type 数据库类型
     * @return 数据库类型枚举
     */
    public static DatabaseTypeEnum getDatabaseType(String type) {
        for (DatabaseTypeEnum database : values()) {
            if (database.getType().equals(type)) {
                return database;
            }
        }
        return null;
    }

}
