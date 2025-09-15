package com.xiaozhuzhu.autodatabase.v2.core.strategy.database.impl;

import com.xiaozhuzhu.autodatabase.v2.core.config.AutoDatabaseConfig;
import com.xiaozhuzhu.autodatabase.v2.core.entity.*;
import com.xiaozhuzhu.autodatabase.v2.core.enums.field.FieldAttributeEnum;
import com.xiaozhuzhu.autodatabase.v2.core.enums.field.FieldTypeEnum;
import com.xiaozhuzhu.autodatabase.v2.core.enums.index.IndexMethodEnum;
import com.xiaozhuzhu.autodatabase.v2.core.enums.index.IndexTypeEnum;
import com.xiaozhuzhu.autodatabase.v2.core.strategy.database.DatabaseSqlExecutor;
import com.xiaozhuzhu.autodatabase.v2.core.utils.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月17日11:02
 * @description: MySql 执行器
 */
public class MySQLExecutor implements DatabaseSqlExecutor {


    /**
     * 创建 MySQL数据库
     *
     * @param autoDatabaseConfig
     * @param databaseName
     * @return
     */
    @SuppressWarnings("all")
    @Override
    public DataSource createDatabase(AutoDatabaseConfig autoDatabaseConfig, String databaseName) {
        String defaultJdbcUrl = "jdbc:mysql://" + autoDatabaseConfig.getHost() + ":" + autoDatabaseConfig.getPort() +
                "/mysql?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";

        // 建表的数据源
        String jdbcUrl = "jdbc:mysql://" + autoDatabaseConfig.getHost() + ":" + autoDatabaseConfig.getPort() +
                "/" + databaseName + "?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";

        // 数据库驱动
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(defaultJdbcUrl);
        dataSource.setUsername(autoDatabaseConfig.getDatabaseAccount());
        dataSource.setPassword(autoDatabaseConfig.getDatabasePassword());

        // 连接数据库
        try (Connection connection = dataSource.getConnection()) {
            // 建表
            // 说明还未建表,执行建表sql
            String createDatabaseSql = "CREATE DATABASE IF NOT EXISTS " + databaseName +
                    " DEFAULT CHARACTER SET utf8mb4" +
                    " COLLATE utf8mb4_0900_ai_ci";
            Statement statement = connection.createStatement();
            statement.executeUpdate(createDatabaseSql);
        } catch (SQLException e) {
            throw new UnsupportedOperationException("创建数据库失败");
        }

        // 切换数据源链接
        dataSource.setUrl(jdbcUrl);
        return dataSource;
    }


    /**
     * 创建 MySQL数据库中的表SQL
     *
     * @param database 数据库描述对象
     * @return key-表名 value-表SQL
     */
    @Override
    public Map<String, String> createTableSql(DatabaseDefinition database) {
        // 获取表的描述
        List<TableDefinition> tableList = database.getTable();

        // 生成SQL
        Map<String, String> sqlMap = new ConcurrentHashMap<>(16);

        StringBuilder sql = new StringBuilder();
        // 遍历
        for (TableDefinition table : tableList) {
            // 获取表名
            String tableName = table.getTableName();
            sql.append("CREATE TABLE `").append(tableName).append("` (\n");
            // 表注释
            String comment = table.getComment() == null ? "" : table.getComment();

            // 表SQL后缀
            String tableSuffix = ") ENGINE = " + database.getEngine() +
                    " CHARACTER SET = " + database.getCharacterSet() +
                    " COLLATE = " + database.getCollate() +
                    " COMMENT = '" + comment + "'" +
                    " ROW_FORMAT = " + database.getRowFormat().getValue() + ";";

            // 获取所有字段
            List<FieldDefinition> fieldList = table.getField();
            // 遍历
            for (FieldDefinition field : fieldList) {
                // 获取字段名
                String fieldName = field.getName();

                // 获取字段类型
                FieldTypeDefinition fieldType = field.getType();
                FieldTypeEnum fType = fieldType.getTypeEnum();
                // 字段类型名字
                String typeName = fType.name();

                // 获取字段长度
                Integer fieldLength = fieldType.getLength();
                // decimal还需获取小数位数
                Integer fieldDecimal = null;
                if (fType == FieldTypeEnum.DECIMAL) {
                    fieldDecimal = fieldType.getDecimal();
                }

                // 获取默认值
                String defaultValue = field.getDefaultValue();

                // 获取字段属性
                List<FieldAttributeEnum> attribute = field.getAttribute();
                // 是否非空
                boolean isNotEmpty = false;
                // 是否自增
                boolean isAutoIncrement = false;
                for (FieldAttributeEnum attributeEnum : attribute) {
                    // 是否非空
                    if (attributeEnum == FieldAttributeEnum.NOT_NULL || attributeEnum == FieldAttributeEnum.PRIMARY_KEY) {
                        isNotEmpty = true;
                    }
                    // 是否自增
                    if (attributeEnum == FieldAttributeEnum.AUTO_INCREMENT) {
                        isAutoIncrement = true;
                    }
                }

                // 获取字段注释
                String fieldComment = field.getComment();

                // 拼接字段名
                sql.append("  `").append(fieldName).append("` ");

                // 拼接字段类型
                sql.append(typeName).append(" ");
                if (fieldLength != -1) {
                    sql.append("(").append(fieldLength);

                    if (fieldDecimal != null) {
                        sql.append(",").append(fieldDecimal);
                    }
                    sql.append(") ");
                }

                // 拼接是否非空
                if (isNotEmpty) {
                    sql.append("NOT NULL ");
                } else {
                    sql.append("NULL ");
                }

                // 拼接自增
                if (isAutoIncrement) {
                    sql.append("AUTO_INCREMENT ");
                } else {
                    // 拼接默认值
                    if (fType == FieldTypeEnum.BIT ||
                            fType == FieldTypeEnum.TINYINT ||
                            fType == FieldTypeEnum.INT ||
                            fType == FieldTypeEnum.BIGINT ||
                            fType == FieldTypeEnum.DECIMAL
                    ) {
                        if (StringUtils.isNotEmpty(defaultValue)) {
                            sql.append(" DEFAULT ").append(defaultValue).append(" ");
                        }
                    } else {
                        if (StringUtils.isNotEmpty(defaultValue)) {
                            sql.append(" DEFAULT '").append(defaultValue).append("' ");
                        }
                    }
                }

                // 拼接字段注释
                sql.append("COMMENT '").append(fieldComment).append("',\n");
            }


            // 拼接主键
            sql.append("  PRIMARY KEY (`").append(table.getPrimaryKey()).append("`)");

            // 拼接索引
            List<IndexDefinition> indexList = table.getIndex();
            // 存在索引
            if (null != indexList && !indexList.isEmpty()) {

                // 换行
                sql.append(",\n");

                // 获取索引前缀
                String indexPrefix = database.getAutoDatabaseConfig().getIndexPrefix();
                for (IndexDefinition index : indexList) {
                    // 索引名称
                    String indexName = index.getIndexName();
                    // 索引类型SQL名字
                    IndexTypeEnum indexTypeEnum = index.getType();
                    String indexSqlName = indexTypeEnum.getName();
                    // 索引方法
                    IndexMethodEnum indexMethodEnum = index.getMethod();
                    String indexMethod = null;
                    if (indexMethodEnum != null) {
                        indexMethod = indexMethodEnum.name();
                    }

                    // 索引排序方法
                    String indexSortType = index.getSortType();
                    // 索引字段
                    List<String> indexFields = index.getFields().stream()
                            .map(FieldDefinition::getName)
                            .collect(Collectors.toList());

                    // 拼接索引类型SQL名字
                    sql.append("  ").append(indexSqlName).append(" ");
                    // 拼接索引名
                    sql.append("`").append(indexPrefix).append(indexName).append("` (");
                    // 拼接索引字段
                    for (String indexField : indexFields) {
                        // 拼接索引字段
                        sql.append(" `").append(indexField).append("` ");
                        // 索引排序
                        if (indexTypeEnum != IndexTypeEnum.FULL_TEXT_INDEX) {
                            // 凭借索引方法
                            sql.append(indexSortType).append(" ");
                        }
                        sql.append(",");
                    }

                    // 删除最后一个逗号
                    sql = new StringBuilder(sql.substring(0, sql.length() - 1) + " ) ");

                    if (indexTypeEnum == IndexTypeEnum.FULL_TEXT_INDEX) {
                        sql.append(",\n");
                    } else {
                        // 凭借索引方法
                        sql.append("USING ").append(indexMethod).append(",\n");
                    }

                }

                // 去除最后一个逗号
                sql = new StringBuilder(sql.substring(0, sql.length() - 2));
            }

            // 拼接表的属性
            sql.append("\n").append(tableSuffix);
            sqlMap.put(tableName, sql.toString());
            // 清空
            sql.delete(0, sql.length());
        }
        return sqlMap;
    }

    @Override
    public Map<String, String> handleExistTableAndCreateUpdateTableSql(DatabaseDefinition database, List<String> existTableNameList) {
        // 没有数据则不继续
        if (existTableNameList == null || existTableNameList.isEmpty()) {
            return null;
        }
        // 更新sql
        Map<String, String> sqlMap = new LinkedHashMap<>(16);

        // 获取数据库名
        String databaseName = database.getDatabaseName();
        JdbcTemplate jdbcTemplate = database.getJdbcTemplate();

        // 获取表
        List<TableDefinition> tableList = database.getTable().stream()
                .map(item -> {
                    if (existTableNameList.contains(item.getTableName())) {
                        return item;
                    }
                    return null;
                })
                // 过滤掉为 null 的节点
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        // 遍历表
        for (TableDefinition table : tableList) {
            // 获取表名
            String tableName = table.getTableName();
            String querySql = "SELECT COLUMN_NAME " +
                    "FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_NAME = '" + tableName + "' AND TABLE_SCHEMA = '" + databaseName + "';";
            List<String> fieldList = jdbcTemplate.query(querySql, (rs, rowNum) -> rs.getString("COLUMN_NAME"));

            List<FieldDefinition> fieldDefinitionList = table.getField();
            List<String> fieldNameList = fieldDefinitionList.stream()
                    .map(FieldDefinition::getName)
                    .collect(Collectors.toList());

            List<FieldDefinition> addFieldList = new ArrayList<>();
            List<String> deleteFieldList = new ArrayList<>();
            // 遍历获取新增字段
            for (FieldDefinition fieldDefinition : fieldDefinitionList) {
                String fieldName = fieldDefinition.getName();
                if (!fieldList.contains(fieldName)) {
                    addFieldList.add(fieldDefinition);
                }
            }

            // 遍历获取删除字段
            for (String fieldName : fieldList) {
                if (!fieldNameList.contains(fieldName)) {
                    deleteFieldList.add(fieldName);
                }
            }

            if (!addFieldList.isEmpty()) {
                StringBuilder addSql = new StringBuilder("ALTER TABLE `" + databaseName + "`.`" + tableName + "`\n");
                StringBuilder indexSql = new StringBuilder("ALTER TABLE `" + databaseName + "`.`" + tableName + "`\n");
                boolean indexFlag = false;
                // 生成sql
                for (FieldDefinition field : addFieldList) {
                    // 是否是索引字段
                    if (field.getIsIndexField()) {
                        indexFlag = true;
                        String fieldName = field.getName();
                        // 找到这个字段的索引描述
                        List<IndexDefinition> indexList = table.getIndex().stream()
                                .filter(item -> item.getIndexPrimaryField().equals(fieldName))
                                .collect(Collectors.toList());

                        // 合成索引sql
                        for (IndexDefinition index : indexList) {
                            indexSql.append(composeAddFieldIndexSql(database.getAutoDatabaseConfig().getIndexPrefix(), index));
                        }
                    }
                    // 合成字段sql
                    addSql.append(composeAddFieldSql(field));
                }
                // 删除最后一个逗号
                addSql = new StringBuilder(addSql.substring(0, addSql.length() - 2) + ";");
                sqlMap.put(tableName, addSql.toString());
                addSql.delete(0, addSql.length());

                if (indexFlag) {
                    indexSql = new StringBuilder(indexSql.substring(0, indexSql.length() - 2) + ";");
                    sqlMap.put(tableName + "(新增索引)", indexSql.toString());
                    indexSql.delete(0, indexSql.length());
                }
            }

            // 删除字段的
            if (!deleteFieldList.isEmpty()) {
                StringBuilder deleteSql = new StringBuilder("ALTER TABLE `" + databaseName + "`.`" + tableName + "`\n");

                for (String fieldName : deleteFieldList) {
                    deleteSql.append("  DROP COLUMN `").append(fieldName).append("`,\n");
                }
                // 删除最后一个逗号
                deleteSql = new StringBuilder(deleteSql.substring(0, deleteSql.length() - 2) + ";");
                sqlMap.put(tableName + "(删除字段)", deleteSql.toString());
                deleteSql.delete(0, deleteSql.length());
            }

        }

        return sqlMap;
    }

    /**
     * 合成索引sql
     *
     * @param indexPrefix 索引名前缀
     * @param index       索引描述
     * @return 索引sql片段
     */
    private String composeAddFieldIndexSql(String indexPrefix, IndexDefinition index) {
        StringBuilder indexSql = new StringBuilder("  ADD");
        // 索引名称
        String indexName = index.getIndexName();
        // 索引类型SQL名字
        IndexTypeEnum indexTypeEnum = index.getType();
        String indexSqlName = indexTypeEnum.getName();
        // 索引方法
        IndexMethodEnum indexMethodEnum = index.getMethod();
        String indexMethod = null;
        if (indexMethodEnum != null) {
            indexMethod = indexMethodEnum.name();
        }

        // 索引排序方法
        String indexSortType = index.getSortType();
        // 索引字段
        List<String> indexFields = index.getFields().stream()
                .map(FieldDefinition::getName)
                .collect(Collectors.toList());

        // 拼接索引类型SQL名字
        indexSql.append("  ").append(indexSqlName).append(" ");
        // 拼接索引名
        indexSql.append("`").append(indexPrefix).append(indexName).append("` (");
        // 拼接索引字段
        for (String indexField : indexFields) {
            // 拼接索引字段
            indexSql.append(" `").append(indexField).append("` ");
            // 索引排序
            if (indexTypeEnum != IndexTypeEnum.FULL_TEXT_INDEX) {
                // 凭借索引方法
                indexSql.append(indexSortType).append(" ");
            }
            indexSql.append(",");
        }

        // 删除最后一个逗号
        indexSql = new StringBuilder(indexSql.substring(0, indexSql.length() - 1) + " ) ");

        if (indexTypeEnum == IndexTypeEnum.FULL_TEXT_INDEX) {
            indexSql.append(",\n");
        } else {
            // 凭借索引方法
            indexSql.append("USING ").append(indexMethod).append(",\n");
        }

        return indexSql.toString();
    }

    /**
     * 合成添加字段sql
     *
     * @param field 字段描述
     * @return 加字段sql片段
     */
    private String composeAddFieldSql(FieldDefinition field) {
        // 获取字段名
        String fieldName = field.getName();

        // 获取字段类型
        FieldTypeDefinition fieldType = field.getType();
        FieldTypeEnum fType = fieldType.getTypeEnum();
        // 字段类型名字
        String typeName = fType.name();

        // 获取字段长度
        Integer fieldLength = fieldType.getLength();
        // decimal还需获取小数位数
        Integer fieldDecimal = null;
        if (fType == FieldTypeEnum.DECIMAL) {
            fieldDecimal = fieldType.getDecimal();
        }

        // 获取默认值
        String defaultValue = field.getDefaultValue();

        // 获取字段属性
        List<FieldAttributeEnum> attribute = field.getAttribute();
        // 是否非空
        boolean isNotEmpty = false;
        for (FieldAttributeEnum attributeEnum : attribute) {
            // 是否非空
            if (attributeEnum == FieldAttributeEnum.NOT_NULL || attributeEnum == FieldAttributeEnum.PRIMARY_KEY) {
                isNotEmpty = true;
                break;
            }
        }

        // 获取字段注释
        String fieldComment = field.getComment();

        // 字段名
        String sql = "  ADD COLUMN `" + fieldName + "` ";
        // 字段类型
        sql += typeName + " ";
        // 字段长度
        if (fieldLength != -1) {
            sql += "(" + fieldLength;

            if (fieldDecimal != null) {
                sql += "," + fieldDecimal;
            }
            sql += ") ";
        }

        // 拼接是否非空
        if (isNotEmpty) {
            sql += "NOT NULL ";
        } else {
            sql += "NULL ";
        }

        // 拼接默认值
        if (fType == FieldTypeEnum.BIT ||
                fType == FieldTypeEnum.TINYINT ||
                fType == FieldTypeEnum.INT ||
                fType == FieldTypeEnum.BIGINT ||
                fType == FieldTypeEnum.DECIMAL
        ) {
            if (StringUtils.isNotEmpty(defaultValue)) {
                sql += " DEFAULT " + defaultValue + " ";
            }
        } else {
            if (StringUtils.isNotEmpty(defaultValue)) {
                sql += " DEFAULT '" + defaultValue + "' ";
            }
        }

        // 拼接字段注释
        sql += "COMMENT '" + fieldComment + "'";

        // 设置字段所在位置
        String pre = field.getPre();
        if (StringUtils.isEmpty(pre)) {
            sql += " FIRST,\n";
        } else {
            sql += " AFTER `" + pre + "`,\n";
        }
        return sql;
    }

}
