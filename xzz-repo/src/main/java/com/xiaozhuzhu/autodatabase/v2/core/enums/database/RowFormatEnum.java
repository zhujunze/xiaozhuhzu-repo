package com.xiaozhuzhu.autodatabase.v2.core.enums.database;

import lombok.Getter;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日10:48
 * @description: 表的行格式枚举
 */
@Getter
public enum RowFormatEnum {

    DYNAMIC("Dynamic", " InnoDB 的一种行存储格式，允许更灵活地存储可变长度列");


    private final String value;
    private final String description;

    RowFormatEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
