package com.xiaozhuzhu.autodatabase.v2.core.enums.database;

import lombok.Getter;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日10:45
 * @description: 字符集的排序规则枚举
 */
@Getter
public enum CollateEnum {

    UTF8MB4_0900_AI_CI("utf8mb4_0900_ai_ci"),
    ;

    private final String value;

    CollateEnum(String value) {
        this.value = value;
    }
}
