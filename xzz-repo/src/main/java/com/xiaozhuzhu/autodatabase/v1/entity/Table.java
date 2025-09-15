package com.xiaozhuzhu.autodatabase.v1.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2024年01月31日17:42
 * @description: 表的实体类
 */
@Data
public class Table {

    /**
     * 表名
     */
    private String name;

    /**
     * 注释
     */
    private String comment;

    /**
     * 表所包含的字段
     */
    private List<Field> columns = new ArrayList<>();

    /**
     * 表是否有主键
     */
    private Boolean primaryKeyFlag = false;
}
