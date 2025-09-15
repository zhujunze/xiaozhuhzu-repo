package com.xiaozhuzhu.autodatabase.v2.core.enums.database;

import lombok.Getter;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日10:44
 * @description: 字符集枚举
 */
@Getter
public enum CharacterSetEnum {


    UTF8MB4("utf8mb4", "UTF-8 Unicode字符集"),
    ;

    private final String value;
    private final String description;

    CharacterSetEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
