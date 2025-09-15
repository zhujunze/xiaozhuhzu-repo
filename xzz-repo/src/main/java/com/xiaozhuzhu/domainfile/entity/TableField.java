package com.xiaozhuzhu.domainfile.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2024年04月22日17:05
 * @description: 字段，描述表中的字段
 */
@Setter
@ToString
@Accessors(chain = true)
public class TableField implements Serializable {

    private static final long serialVersionUID = -6969520027769827634L;

    /**
     * 字段名
     */
    @Getter
    private String fieldName;


    /**
     * 字段类型
     */
    private String fieldType;

    /**
     * 是否主键
     */
    @Getter
    private boolean primaryKey = false;

    /**
     * 字段注释
     */
    @Getter
    private String comment;

    /**
     * 获取字段名（转化为驼峰命名）
     *
     * @return 属性名
     */
    public String getTypeFieldName(boolean removePrefix) {
        String fieldName = this.fieldName;
        if (removePrefix) {
            fieldName = this.fieldName.substring(this.fieldName.indexOf("_") + 1);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldName.length(); i++) {
            if (!Character.isLetter(fieldName.charAt(i))) {
                // 是数字
                if (Character.isDigit(fieldName.charAt(i))){
                    sb.append(fieldName.charAt(i));
                }else {
                    sb.append(Character.toUpperCase(fieldName.charAt(++i)));
                }
                continue;
            }
            sb.append(fieldName.charAt(i));
        }
        return sb.toString();
    }

    /**
     * 获取字段类型
     *
     * @return 字段类型
     */
    public String getFieldType() {
        if (fieldType.contains("(")) {
            fieldType = fieldType.substring(0, fieldType.indexOf("("));
        }
        switch (fieldType) {
            // String
            case "char":
            case "varchar":
            case "tinytext":
            case "text":
            case "longtext":
                return "String";

            // Integer
            case "tinyint":
            case "int":
                return "Integer";
            // Long
            case "bigint":
                return "Long";
            // Date
            case "date":
            case "datetime":
            case "timestamp":
//                return "Date";
                return "String";
            // Float
            case "decimal":
                return "BigDecimal";
            // Boolean
            case "bit":
                return "Boolean";
            default:
                return "String";
        }
    }

    /**
     * RowMapper映射字段类型
     *
     * @return 获取类型字符串
     */
    public String getRowMapperFieldType() {
        if (fieldType.contains("(")) {
            fieldType = fieldType.substring(0, fieldType.indexOf("("));
        }
        switch (fieldType) {
            // String
            case "char":
            case "varchar":
            case "tinytext":
            case "text":
            case "longtext":
                return "getString";

            // Integer
            case "tinyint":
            case "int":
                return "getInt";
            // Long
            case "bigint":
                return "getLong";
            // Date
            case "date":
            case "datetime":
            case "timestamp":
//                return "getDate";
                return "getString";
            // Float
            case "decimal":
                return "getBigDecimal";
            // Boolean
            case "bit":
                return "getBoolean";
            default:
                return "getString";
        }
    }


}
