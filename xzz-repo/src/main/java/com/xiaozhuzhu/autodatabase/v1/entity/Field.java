package com.xiaozhuzhu.autodatabase.v1.entity;

import lombok.Data;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2024年01月31日17:46
 * @description: 字段
 */
@Data
public class Field {

    /**
     * 字段名
     */
    private String name;
    /**
     * 类型
     */
    private String type;
    /**
     * 默认值
     */
    private String defaultValue;
    /**
     * 字段约束与属性
     */
    private FieldConstraint constraint;
    /**
     * 注释
     */
    private String comment;
    /**
     * 字段属性是否是text
     */
    private Boolean textFlag = false;
    /**
     * 是否是索引
     */
    private Boolean indexFlag = false;

}
