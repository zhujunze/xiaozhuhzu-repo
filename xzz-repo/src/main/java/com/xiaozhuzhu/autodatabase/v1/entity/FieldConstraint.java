package com.xiaozhuzhu.autodatabase.v1.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2024年02月01日14:02
 * @description: 字段约束
 */
@Data
public class FieldConstraint {


    /**
     * 主键
     */
    private String primaryKey;
    /**
     * 自增
     */
    private String autoIncrement;
    /**
     * 非空
     */
    private String notNull;
    /**
     * 索引
     */
    private String index;
    /**
     * 唯一(唯一约束、唯一索引，在 MySQL 中创建唯一约束会创建唯一索引，唯一约束是通过唯一索引实现的)
     */
    private String uniqueIndex;
    /**
     * 联合索引
     */
    private String unionIndex;
    /**
     * 联合唯一索引
     */
    private String unionUniqueIndex;
    /**
     * 全文索引
     */
    private String fullTextIndex;
    /**
     * 索引sql
     */
    private List<String> indexSql = new ArrayList<>();

}
