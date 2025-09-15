package com.xiaozhuzhu.autodatabase.v2.core.strategy.field.impl;


import com.xiaozhuzhu.autodatabase.v2.core.entity.FieldDefinition;
import com.xiaozhuzhu.autodatabase.v2.core.entity.FieldTypeDefinition;
import com.xiaozhuzhu.autodatabase.v2.core.enums.field.FieldTypeEnum;
import com.xiaozhuzhu.autodatabase.v2.core.strategy.field.FieldTypePaser;
import com.xiaozhuzhu.autodatabase.v2.core.utils.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日17:46
 * @description: date类型解析器
 */
public class DateFieldTypeParser implements FieldTypePaser {


    /**
     * date 类型解析
     *
     * @param fieldTypeDefinition 字段类型对象
     * @param fieldTypeEnum       字段类型枚举
     * @param fieldLength         字段类型长度
     * @param tableName           表名
     * @param fieldName           字段名
     * @return 对应解析器
     */
    @Override
    public FieldTypePaser parseFieldType(FieldTypeDefinition fieldTypeDefinition, FieldTypeEnum fieldTypeEnum, String fieldLength, String tableName, String fieldName) {
        // 这种类型不需要设置长度
        fieldTypeDefinition.setLength(-1);

        return this;
    }

    /**
     * date 类型默认值解析
     *
     * @param field         字段对象
     * @param fieldTypeEnum 字段类型枚举
     * @param defaultValue  默认值字符串
     * @param tableName     表名
     * @param fieldName     字段名
     */
    @Override
    public void parseDefaultValue(FieldDefinition field, FieldTypeEnum fieldTypeEnum, String defaultValue, String tableName, String fieldName) {
        // 是否设置默认值
        if (StringUtils.isEmpty(defaultValue)) {
            return;
        }

        try {
            // 日期校验
            String pattern = "yyyy-MM-dd";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDate.parse(defaultValue, formatter);

            field.setDefaultValue(defaultValue);
        } catch (Exception e) {
            throw new UnsupportedOperationException("无法识别的默认值存在于表:{" + tableName + "},字段:{" + fieldName + "}中");
        }
    }
}
