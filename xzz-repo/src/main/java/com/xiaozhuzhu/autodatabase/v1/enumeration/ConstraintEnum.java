package com.xiaozhuzhu.autodatabase.v1.enumeration;


import lombok.Getter;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2024年02月01日18:57
 * @description: TODO
 */
@Getter
public enum ConstraintEnum {

    PRIMARY_KEY("PRIMARY_KEY", "主键"),
    AUTO_INCREMENT("AUTO_INCREMENT", "自增"),
    NOT_NULL("NOT_NULL", "非空"),
    INDEX("INDEX", "索引"),
    UNIQUE_INDEX("UNIQUE_INDEX", "唯一"),
    UNION_INDEX("UNION_INDEX", "联合索引"),
    UNION_UNIQUE_INDEX("UNION_UNIQUE_INDEX", "联合唯一索引"),
    FULL_TEXT_INDEX("FULL_TEXT_INDEX", "全文索引");

    private final String name;
    private final String desc;

    ConstraintEnum(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

}
