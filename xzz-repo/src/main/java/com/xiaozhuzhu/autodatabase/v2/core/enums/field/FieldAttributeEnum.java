package com.xiaozhuzhu.autodatabase.v2.core.enums.field;

import lombok.Getter;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日11:09
 * @description: 字段属性枚举
 */
@Getter
public enum FieldAttributeEnum {

    /**
     * 主键
     */
    PRIMARY_KEY("主键"),
    /**
     * 自增
     */
    AUTO_INCREMENT("自增"),
    /**
     * 非空
     */
    NOT_NULL("非空"),
    /**
     * 空
     */
    NULL("空"),
    /**
     * 索引
     */
    INDEX("索引"),
    /**
     * 唯一
     */
    UNIQUE("唯一"),
    /**
     * 联合索引
     */
    UNION_INDEX("联合索引"),
    /**
     * 联合唯一索引
     */
    UNION_UNIQUE_INDEX("联合唯一索引"),
    /**
     * 全文索引
     */
    FULL_TEXT_INDEX("全文索引"),
    ;

    private final String name;

    FieldAttributeEnum(String name) {
        this.name = name;
    }

    /**
     * 获取字段属性枚举
     *
     * @param name 字段属性字符串
     * @return 字段属性枚举
     */
    public static FieldAttributeEnum getFieldAttribute(String name) {
        for (FieldAttributeEnum attribute : values()) {
            if (attribute.getName().equals(name)) {
                return attribute;
            }
        }
        return null;
    }

}
