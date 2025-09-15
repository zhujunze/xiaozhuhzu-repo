package com.xiaozhuzhu.autodatabase.v2.core.enums.field;

import lombok.Getter;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日10:56
 * @description: 字段类型枚举
 */
@Getter
public enum FieldTypeEnum {

    BIT("bit", "用于存储 二进制位"),
    TINYINT("tinyint", "存储较小的整数值"),
    INT("int", "存储整数值"),
    BIGINT("bigint", "存储大整数值"),
    DECIMAL("decimal", "存储带小数的数值"),
    CHAR("char", "存储长度固定的字符串，最大长度：255 字符"),
    VARCHAR("varchar", "存储长度可变的字符串，最大长度：65535 字节"),
    TINYTEXT("tinytext", "存储简短的文本字段"),
    TEXT("text", "存储中等长度的文本"),
    LONGTEXT("longtext", "存储非常长的文本"),
    JSON("json", "Json格式的数据"),
    DATE("date", "存储日期信息"),
    DATETIME("datetime", "存储日期和时间的详细记录"),
    ;

    private final String type;
    private final String description;

    FieldTypeEnum(String type, String description) {
        this.type = type;
        this.description = description;
    }

    /**
     * 获取字段类型
     *
     * @param type 字段类型字符串
     * @return 字段类型枚举
     */
    public static FieldTypeEnum getFieldType(String type) {
        for (FieldTypeEnum fieldType : values()) {
            if (fieldType.getType().equals(type)) {
                return fieldType;
            }
        }
        return null;
    }
}
