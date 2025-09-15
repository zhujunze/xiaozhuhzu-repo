package com.xiaozhuzhu.autodatabase.v2.core.entity;

import com.xiaozhuzhu.autodatabase.v2.core.enums.index.IndexMethodEnum;
import com.xiaozhuzhu.autodatabase.v2.core.enums.index.IndexTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日10:31
 * @description: 索引定义
 */
@Data
@Builder
public class IndexDefinition implements Serializable {
    private static final long serialVersionUID = 3604198075950192187L;


    /**
     * 索引名称
     */
    private String indexName;

    /**
     * 索引所在字段
     */
    private String indexPrimaryField;

    /**
     * 索引字段
     */
    private List<FieldDefinition> fields;

    /**
     * 索引排序类型 ACS、DESC
     */
    private String sortType;

    /**
     * 索引类型
     */
    private IndexTypeEnum type;

    /**
     * 索引方法
     */
    private IndexMethodEnum method;
}
