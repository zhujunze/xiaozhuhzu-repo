package com.xiaozhuzhu.autodatabase.v2.core.entity;

import com.xiaozhuzhu.autodatabase.v2.core.config.AutoDatabaseConfig;
import com.xiaozhuzhu.autodatabase.v2.core.enums.database.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.Serializable;
import java.util.List;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日10:35
 * @description: 数据库定义
 */
@Data
@Builder
public class DatabaseDefinition implements Serializable {
    private static final long serialVersionUID = 8684028900481720547L;

    /**
     * 数据库类型
     */
    private DatabaseTypeEnum type;

    /**
     * 存储引擎
     */
    private EngineEnums engine;

    /**
     * 字符集 默认：utf8mb4
     */
    private CharacterSetEnum characterSet;

    /**
     * 字符集的排序规则
     */
    private CollateEnum collate;


    /**
     * 表的行格式
     */
    private RowFormatEnum rowFormat;

    /**
     * 数据库名字
     */
    private String databaseName;

    /**
     * 表
     */
    private List<TableDefinition> table;


    /**
     * 数据库配置
     */
    private AutoDatabaseConfig autoDatabaseConfig;

    /**
     * SQL执行模版
     */
    private JdbcTemplate jdbcTemplate;


}
