package com.xiaozhuzhu.autodatabase.v2.core.enums.index;

import lombok.Getter;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日11:27
 * @description: 索引类型枚举
 */
@Getter
public enum IndexTypeEnum {

    /**
     * 索引
     */
    INDEX("INDEX"),
    /**
     * 唯一
     */
    UNIQUE("UNIQUE INDEX"),
    /**
     * 联合索引
     */
    UNION_INDEX("INDEX"),
    /**
     * 联合唯一索引
     */
    UNION_UNIQUE_INDEX("UNIQUE INDEX"),
    /**
     * 全文索引
     */
    FULL_TEXT_INDEX("FULLTEXT INDEX"),
    ;

    private final String name;

    IndexTypeEnum(String name) {
        this.name = name;
    }


}
