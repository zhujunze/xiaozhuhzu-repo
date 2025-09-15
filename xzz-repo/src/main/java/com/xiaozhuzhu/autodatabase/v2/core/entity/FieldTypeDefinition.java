package com.xiaozhuzhu.autodatabase.v2.core.entity;

import com.xiaozhuzhu.autodatabase.v2.core.enums.field.FieldTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日10:59
 * @description: 字段类型定义
 */
@Data
@Builder
public class FieldTypeDefinition implements Serializable {
    private static final long serialVersionUID = -8708382978595780258L;


    /**
     * 字段类型
     */
    private FieldTypeEnum typeEnum;

    /**
     * 长度
     */
    private Integer length;

    /**
     * 小数位数
     */
    private Integer decimal;
}
