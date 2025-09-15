package com.xiaozhuzhu.autodatabase.v2.core.entity;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日10:21
 * @description: 表定义
 */
@Data
@Builder
public class TableDefinition implements Serializable {
    private static final long serialVersionUID = -1008357080606732496L;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 主键字段
     */
    private String primaryKey;


    /**
     * 字段
     */
    private List<FieldDefinition> field;


    /**
     * 索引
     */
    private List<IndexDefinition> index;


    /**
     * 表的描述
     */
    private String comment;


}
