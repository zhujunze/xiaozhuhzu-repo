package com.xiaozhuzhu.autodatabase.v2.core.strategy.database;

import com.xiaozhuzhu.autodatabase.v2.core.config.AutoDatabaseConfig;
import com.xiaozhuzhu.autodatabase.v2.core.entity.DatabaseDefinition;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月17日10:33
 * @description: 数据库SQl执行器
 */
public interface DatabaseSqlExecutor {


    /**
     * 创建数据库
     *
     * @param autoDatabaseConfig 数据库配置
     * @param databaseName       数据库名字
     * @return 数据源驱动
     */
    DataSource createDatabase(AutoDatabaseConfig autoDatabaseConfig, String databaseName);

    /**
     * 创建表SQL
     *
     * @param database 数据库描述对象
     * @return key-表名 value-表SQL
     */
    Map<String, String> createTableSql(DatabaseDefinition database);

    /**
     * 处理已存在代表，创建更新sql
     *
     * @param database           数据库描述对象
     * @param existTableNameList 已存在的表名
     * @return key-表名 value-更新表的sql
     */
    Map<String, String> handleExistTableAndCreateUpdateTableSql(DatabaseDefinition database, List<String> existTableNameList);


}
