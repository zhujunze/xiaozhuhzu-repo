package com.xiaozhuzhu.autodatabase.v2;

import com.xiaozhuzhu.autodatabase.v2.core.config.AutoDatabaseConfig;
import com.xiaozhuzhu.autodatabase.v2.core.entity.DatabaseDefinition;
import com.xiaozhuzhu.autodatabase.v2.core.enums.database.*;
import com.xiaozhuzhu.autodatabase.v2.core.enums.document.DocumentEnum;
import com.xiaozhuzhu.autodatabase.v2.core.strategy.database.DatabaseSqlExecutorFactory;
import com.xiaozhuzhu.autodatabase.v2.core.strategy.doc.DocumentParserFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日10:18
 * @description: 运行自动创建数据库
 */
@Slf4j
public class AutoDatabaseRunner {

    /**
     * 配置
     */
    private final AutoDatabaseConfig autoDatabaseConfig;

    private static final JdbcTemplate jdbcTemplate = new JdbcTemplate();

    public AutoDatabaseRunner(AutoDatabaseConfig autoDatabaseConfig) {
        this.autoDatabaseConfig = autoDatabaseConfig;
    }


    /**
     * 自动生成数据库表的入口
     */
    public void autoGenerator() throws IOException {
        // 获取文件的绝对路径
        String documentPath = autoDatabaseConfig.getDocumentPath();

        // 获取数据库类型
        String databaseType = autoDatabaseConfig.getDatabaseType();
        DatabaseTypeEnum databaseTypeEnum = DatabaseTypeEnum.getDatabaseType(databaseType);
        if (null == databaseTypeEnum) {
            throw new UnsupportedOperationException("此数据库类型[" + databaseType + "]暂不支持");
        }

        // 创建数据库
        DatabaseDefinition database;

        // 获取文档类型
        DocumentEnum documentType = DocumentEnum.getDocumentType(documentPath);

        try (InputStream in = Files.newInputStream(Paths.get(documentPath))) {
            // 加载文档
            XWPFDocument document = new XWPFDocument(in);

            // 解析数据库名字
            String databaseName = DocumentParserFactory.build(documentType).parseDatabaseName(document);
            // 初始化数据库
            database = defaultDatabaseInit(databaseTypeEnum, databaseName);

            // 解析文档
            DocumentParserFactory.build(documentType).parse(document, database);
        }

        // 建SQL
        Map<String, String> sqlMap = DatabaseSqlExecutorFactory.build(databaseTypeEnum)
                // 组装SQL语句
                .createTableSql(database);
        // 建表
        // 已存在的表
        List<String> existTableNameList = autoCreateTable(sqlMap);

        // 处理已存在的表
        Map<String, String> updateSqlMap = DatabaseSqlExecutorFactory.build(databaseTypeEnum)
                .handleExistTableAndCreateUpdateTableSql(database, existTableNameList);

        // 更新已存在的表
        if (null != updateSqlMap && !updateSqlMap.isEmpty()) {
            autoUpdateTable(updateSqlMap);
        }
    }

    /**
     * 更新表
     *
     * @param updateSqlMap 更新表的SQL
     */
    private void autoUpdateTable(Map<String, String> updateSqlMap) {
        log.info("\n================================================ 修改表开始 ================================================");
        StringBuilder toBeUpdateTable = new StringBuilder();
        int success = 0;
        int fail = 0;
        for (Map.Entry<String, String> entry : updateSqlMap.entrySet()) {
            String key = "";
            String value = "";
            try {
                success++;
                key = entry.getKey();
                value = entry.getValue();
                log.debug("正在更新表 {}", key);
                log.debug("更新表: {}\n执行SQL:\n" +
                        "************************* SQL 执行开始 *************************\n" +
                        "{}" +
                        "\n************************* SQL 执行开始 *************************", key, value);
                jdbcTemplate.execute(value);
                log.debug("更新表 {} 完成", key);
                if (!key.contains("(新增索引)") && !value.contains("(删除字段)")) {
                    toBeUpdateTable.append("\"").append(key).append("\",\n");
                }
            } catch (Exception e) {
                success--;
                fail++;
                log.error("更新表<{}>失败,执行的SQL为:\n{}\n失败原因:{}", key, value, e.getMessage());
            }
        }
        log.info("更新表完成,成功: {},失败: {}", success, fail);
        log.info("待更新实体的表名:\n{}", toBeUpdateTable);
        log.info("\n================================================ 修改表结束 ================================================\n");
    }

    /**
     * 创建新添加的表，返回已存在的表
     *
     * @param sqlMap 建表sql映射 key-表名 value-表SQL
     * @return 已存在的表表名
     */
    private List<String> autoCreateTable(Map<String, String> sqlMap) {
        log.info("\n================================================ 生成表开始 ================================================");
        List<String> existTableNameList = new ArrayList<>();
        int exist = 0;
        for (Map.Entry<String, String> entry : sqlMap.entrySet()) {
            String key = entry.getKey();
            if (isTableExist(key)) {
                // 已存在的表
                existTableNameList.add(key);
                // 表名已存在 剔除当前表
                sqlMap.remove(key);
                exist++;
            }
        }
        // 批量创键表，要么同时成功，要么同时失败
        String key = "";
        String value = "";
        int success = 0;
        int fail = 0;
        StringBuilder toBeGenerateTable = new StringBuilder();
        for (Map.Entry<String, String> entry : sqlMap.entrySet()) {
            try {
                success++;
                key = entry.getKey();
                value = entry.getValue();
                log.debug("正在创建表 {}", key);
                log.debug("创建表: {}\n执行SQL:\n" +
                        "************************* SQL 执行开始 *************************\n" +
                        "{}" +
                        "\n************************* SQL 执行结束 *************************", key, value);
                jdbcTemplate.execute(value);
                log.debug("表 {} 创建完成", key);
                toBeGenerateTable.append("\"").append(key).append("\",\n");
            } catch (Exception e) {
                success--;
                fail++;
                log.error("创建表<{}>失败,执行的SQL为:\n{}\n失败原因:{}", key, value, e.getMessage());
            }
        }
        log.info("建表完成,成功: {},失败: {},已存在: {}", success, fail, exist);
        log.info("待生成实体的表名:\n{}", toBeGenerateTable);
        log.info("\n================================================ 生成表结束 ================================================\n");
        return existTableNameList;
    }

    /**
     * 判断表名是否已存在
     *
     * @param tableName 表名
     * @return true
     */
    @SuppressWarnings("all")
    public boolean isTableExist(String tableName) {
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SHOW TABLES LIKE '" + tableName + "'");
            return resultSet.next();
        } catch (SQLException e) {
            log.error("查询表表名是否创建失败，失败原因:{}", e.getMessage());
        }
        return false;
    }


    /**
     * 默认的数据库初始化方法
     *
     * @return 数据库定义实体类
     */
    private DatabaseDefinition defaultDatabaseInit(DatabaseTypeEnum databaseTypeEnum, String databaseName) {

        // 建表
        DataSource dataSource = DatabaseSqlExecutorFactory.build(databaseTypeEnum).createDatabase(autoDatabaseConfig, databaseName);
        // 设置数据源
        jdbcTemplate.setDataSource(dataSource);

        // 初始化数据库描述
        return DatabaseDefinition.builder()
                .databaseName(databaseName)
                .type(DatabaseTypeEnum.MYSQL)
                .engine(EngineEnums.InnoDB)
                .characterSet(CharacterSetEnum.UTF8MB4)
                .collate(CollateEnum.UTF8MB4_0900_AI_CI)
                .rowFormat(RowFormatEnum.DYNAMIC)
                .autoDatabaseConfig(autoDatabaseConfig)
                .jdbcTemplate(jdbcTemplate)
                .build();
    }
}
