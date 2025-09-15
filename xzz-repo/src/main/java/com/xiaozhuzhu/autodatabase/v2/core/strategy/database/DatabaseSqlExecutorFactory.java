package com.xiaozhuzhu.autodatabase.v2.core.strategy.database;

import com.xiaozhuzhu.autodatabase.v2.core.enums.database.DatabaseTypeEnum;
import com.xiaozhuzhu.autodatabase.v2.core.strategy.database.impl.MySQLExecutor;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月17日10:44
 * @description: 数据库SQL执行器工厂
 */
public class DatabaseSqlExecutorFactory {

    private static final MySQLExecutor mySQLExecutor = new MySQLExecutor();


    /**
     * 根据数据库类型获取对应构建对象
     *
     * @param databaseTypeEnum 文档类型枚举
     * @return 数据库SQL执行器
     */
    public static DatabaseSqlExecutor build(DatabaseTypeEnum databaseTypeEnum) {
        DatabaseSqlExecutor databaseSqlExecutor;
        switch (databaseTypeEnum) {
            case MYSQL:
                databaseSqlExecutor = mySQLExecutor;
                break;
            default:
                throw new UnsupportedOperationException("不支持的数据库配置");
        }
        return databaseSqlExecutor;
    }
}
