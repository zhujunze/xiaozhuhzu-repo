package com.xiaozhuzhu.autodatabase.v2.core.entity;

import com.xiaozhuzhu.autodatabase.v2.core.enums.field.FieldAttributeEnum;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日10:24
 * @description: 字段定义
 */
@Data
@Builder
public class FieldDefinition implements Serializable {
    private static final long serialVersionUID = 5960053435599102267L;


    /**
     * 字段名称
     */
    private String name;

    /**
     * 字段类型
     */
    private FieldTypeDefinition type;

    /**
     * 字段默认值
     */
    private String defaultValue;

    /**
     * 是否索引字段
     */
    private Boolean isIndexField;

    /**
     * 字段属性
     */
    private List<FieldAttributeEnum> attribute;

    /**
     * 字段属性中的索引字段字符串
     */
    private String attributeIndexString;

    /**
     * 字段注释
     */
    private String comment;

    /**
     * 上一个字段，用于新增字段时，确定添加位置
     */
    private String pre;

    /**
     * 下一个字段，用于新增字段时，确定添加位置
     */
    private String next;
}
