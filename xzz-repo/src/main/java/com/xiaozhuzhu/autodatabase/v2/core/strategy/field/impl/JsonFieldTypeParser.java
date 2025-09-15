package com.xiaozhuzhu.autodatabase.v2.core.strategy.field.impl;


import com.xiaozhuzhu.autodatabase.v2.core.entity.FieldDefinition;
import com.xiaozhuzhu.autodatabase.v2.core.entity.FieldTypeDefinition;
import com.xiaozhuzhu.autodatabase.v2.core.enums.field.FieldTypeEnum;
import com.xiaozhuzhu.autodatabase.v2.core.strategy.field.FieldTypePaser;
import com.xiaozhuzhu.autodatabase.v2.core.utils.StringUtils;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年08月01日9:24
 * @description: JSON类型字段
 */
public class JsonFieldTypeParser implements FieldTypePaser {

    /**
     * 解析字段类型
     *
     * @param fieldTypeDefinition 字段类型对象
     * @param fieldTypeEnum       字段类型枚举
     * @param fieldLength         字段类型长度
     * @param tableName           表名
     * @param fieldName           字段名
     * @return
     */
    @Override
    public FieldTypePaser parseFieldType(FieldTypeDefinition fieldTypeDefinition, FieldTypeEnum fieldTypeEnum, String fieldLength, String tableName, String fieldName) {
        // 这种类型不需要设置长度
        fieldTypeDefinition.setLength(-1);
        return this;
    }

    /**
     * 解析默认值
     *
     * @param field         字段对象
     * @param fieldTypeEnum 字段类型枚举
     * @param defaultValue  默认值字符串
     * @param tableName     表名
     * @param fieldName     字段名
     */
    @Override
    public void parseDefaultValue(FieldDefinition field, FieldTypeEnum fieldTypeEnum, String defaultValue, String tableName, String fieldName) {
        // Json字段不能复默认值
        if (StringUtils.isNotEmpty(defaultValue)) {
            throw new UnsupportedOperationException("字段类型" + fieldTypeEnum.getType() + "不能复默认值" +
                    "存在于表:{" + tableName + "},字段:{" + fieldName + "}中");
        }
    }
}
