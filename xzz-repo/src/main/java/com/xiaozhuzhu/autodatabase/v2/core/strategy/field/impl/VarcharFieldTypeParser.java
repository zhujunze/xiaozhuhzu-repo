package com.xiaozhuzhu.autodatabase.v2.core.strategy.field.impl;


import com.xiaozhuzhu.autodatabase.v2.core.entity.FieldDefinition;
import com.xiaozhuzhu.autodatabase.v2.core.entity.FieldTypeDefinition;
import com.xiaozhuzhu.autodatabase.v2.core.enums.field.FieldTypeEnum;
import com.xiaozhuzhu.autodatabase.v2.core.strategy.field.FieldTypePaser;
import com.xiaozhuzhu.autodatabase.v2.core.utils.StringUtils;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日17:45
 * @description: varchar类型解析器
 */
public class VarcharFieldTypeParser implements FieldTypePaser {

    /**
     * varchar 类型解析
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
        int maxLength = 8192;
        // 最小长度
        int minLength = 1;
        // 默认长度
        int defaultLength = 256;

        try {
            // 设置默认长度
            int length = defaultLength;
            // 判断是否有设置长度
            if (StringUtils.isNotEmpty(fieldLength)) {
                length = Integer.parseInt(fieldLength);
                if (length < minLength) {
                    length = defaultLength;
                }
                if (length > maxLength) {
                    throw new UnsupportedOperationException("字段类型" + fieldTypeEnum.getType() +
                            "长度超过8192,请使用text类型," +
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
     * varchar 类型默认值解析
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

        field.setDefaultValue(defaultValue);
    }
}
