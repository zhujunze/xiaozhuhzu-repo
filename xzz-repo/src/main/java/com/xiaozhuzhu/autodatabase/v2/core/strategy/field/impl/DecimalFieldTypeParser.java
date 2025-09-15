package com.xiaozhuzhu.autodatabase.v2.core.strategy.field.impl;


import com.xiaozhuzhu.autodatabase.v2.core.entity.FieldDefinition;
import com.xiaozhuzhu.autodatabase.v2.core.entity.FieldTypeDefinition;
import com.xiaozhuzhu.autodatabase.v2.core.enums.field.FieldTypeEnum;
import com.xiaozhuzhu.autodatabase.v2.core.strategy.field.FieldTypePaser;
import com.xiaozhuzhu.autodatabase.v2.core.utils.StringUtils;

import java.math.BigDecimal;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日17:45
 * @description: decimal类型解析器
 */
public class DecimalFieldTypeParser implements FieldTypePaser {


    /**
     * decimal 类型解析
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
        int maxLength = 65;
        int minLength = 1;
        // 默认长度
        int defaultLength = 10;
        // 最大小数位数
        int maxDecimal = 30;
        // 默认小数位数
        int defaultDecimal = 2;
        // 最小小数位数
        int minDecimal = 0;

        try {
            int length = defaultLength;
            int decimal = defaultDecimal;

            if (StringUtils.isNotEmpty(fieldLength)) {
                // 格式化逗号
                fieldLength = StringUtils.commaFormat(fieldLength);
                String[] items = fieldLength.trim().split(",");
                // 获取长度
                length = Integer.parseInt(items[0]);
                if (length < minLength) {
                    length = defaultLength;
                }
                if (length > maxLength) {
                    throw new UnsupportedOperationException("字段类型" + fieldTypeEnum.getType() + "长度超出了最大长度 maxLength = " + maxLength + " 的范围," +
                            "在于表:{" + tableName + "},字段:{" + fieldName + "}中");
                }

                // 获取小数位数
                decimal = Integer.parseInt(items[1]);
                if (decimal < minDecimal) {
                    length = defaultDecimal;
                }
                if (decimal > length) {
                    throw new UnsupportedOperationException("字段类型" + fieldTypeEnum.getType() + "小数位数需小于等于字段长度 length = " + length + " 的范围," +
                            "在于表:{" + tableName + "},字段:{" + fieldName + "}中");
                } else if (decimal > maxDecimal) {
                    throw new UnsupportedOperationException("字段类型" + fieldTypeEnum.getType() + "小数位数不能超过最大长度 maxDecimal = " + maxDecimal + " 的范围," +
                            "在于表:{" + tableName + "},字段:{" + fieldName + "}中");
                }
            }

            // 设置字段长度
            fieldTypeDefinition.setLength(length);
            // 设置小数位数
            fieldTypeDefinition.setDecimal(decimal);

            return this;
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new UnsupportedOperationException("无法识别的字段类型长度存在于表:{" + tableName + "},字段:{" + fieldName + "}中");
        }
    }

    /**
     * decimal 类型默认值解析
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
            // 校验是否是数字
            new BigDecimal(defaultValue);
            field.setDefaultValue(defaultValue);
        } catch (Exception e) {
            throw new UnsupportedOperationException("无法识别的默认值存在于表:{" + tableName + "},字段:{" + fieldName + "}中");
        }
    }

}
