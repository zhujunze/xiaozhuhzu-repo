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
 * @description: datetime类型解析器
 */
public class DatetimeFieldTypeParser implements FieldTypePaser {


    /**
     * datetime 类型解析
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
        // 最大长度
        int maxLength = 6;
        try {
            int length = -1;
            // 判断是否有设置长度
            if (StringUtils.isNotEmpty(fieldLength)) {
                length = Integer.parseInt(fieldLength);
                if (length > maxLength) {
                    throw new UnsupportedOperationException("字段类型" + fieldTypeEnum.getType() +
                            "长度超出了最大长度 maxLength = " + maxLength + " 的范围," +
                            "在于表:{" + tableName + "},字段:{" + fieldName + "}中");
                }
            }
            // 设置字段长度
            fieldTypeDefinition.setLength(length);

            return this;
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new UnsupportedOperationException("无法识别的字段类型长度存在于表:{" + tableName + "},字段:{" + fieldName + "}中");
        }
    }

    /**
     * datetime 类型默认值解析
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
            String pattern = "yyyy-MM-dd HH:mm:ss";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDate.parse(defaultValue, formatter);

            field.setDefaultValue(defaultValue);
        } catch (Exception e) {
            throw new UnsupportedOperationException("无法识别的默认值存在于表:{" + tableName + "},字段:{" + fieldName + "}中");
        }
    }
}
