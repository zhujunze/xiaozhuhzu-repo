package com.xiaozhuzhu.autodatabase.v1;

import com.xiaozhuzhu.autodatabase.v1.entity.Field;
import com.xiaozhuzhu.autodatabase.v1.entity.FieldConstraint;
import com.xiaozhuzhu.autodatabase.v1.entity.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.xiaozhuzhu.autodatabase.v1.constance.TableConstance.*;


/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2024年01月31日17:30
 * @description: 自动创建数据库表的工具类
 */
@Slf4j
public class DatabaseTableHelper {

    /**
     * 是否去除字段名前缀
     */
    private Boolean removePrefix = false;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseTableHelper(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public DatabaseTableHelper(DataSource dataSource, Boolean removePrefix) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.removePrefix = removePrefix;
    }

    /**
     * 自动生成数据库表的入口
     *
     * @param documentPath 构建数据库表文档的绝对路径
     */
    public void autoGenerator(String documentPath) throws IOException {
        // 解析文档表格
        List<Table> tables = parserDocument(documentPath);
        // 构建SQL 表名-建表SQL
        Map<String, String> sqlMap = generateTableSQL(tables);
        // 剔除
        int exist = 0;
        for (Map.Entry<String, String> entry : sqlMap.entrySet()) {
            String key = entry.getKey();
            if (isTableExist(key)) {
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
                log.debug("创建表: {}\n执行SQL:\n{}", key, value);
                jdbcTemplate.execute(value);
                log.debug("表 {} 创建完成", key);
                toBeGenerateTable.append(DOUBLE_QUOTES).append(key).append(DOUBLE_QUOTES).append(COMMA).append(NEW_LINE);
            } catch (Exception e) {
                success--;
                fail++;
                log.error("创建表<{}>失败,执行的SQL为:\n{}\n失败原因:{}", key, value, e.getMessage());
            }
        }
        log.info("建表完成,成功: {},失败: {},已存在: {}", success, fail, exist);
        log.info("待生成实体的表名:\n{}", toBeGenerateTable);
    }

    /**
     * 判断表名是否已存在
     *
     * @param tableName 表名
     * @return true
     */
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
     * 生成建表SQL
     *
     * @param tables 解析出的表名及字段信息
     * @return 构建SQL 表名-建表SQL
     */
    private Map<String, String> generateTableSQL(List<Table> tables) {
        Map<String, String> sqlMap = new ConcurrentHashMap<>(16);
        StringBuilder sql = new StringBuilder();
        StringBuilder primaryKeySql = new StringBuilder();
        StringBuilder indexSql = new StringBuilder();
        for (Table table : tables) {
            // 获取表名
            String name = table.getName();
            // 构建sql
            // CREATE TABLE `table_test` (
            sql.append(CREATE_TABLE)
                    .append(BLANK)
                    .append(BACK_TICK)
                    .append(name)
                    .append(BACK_TICK)
                    .append(BLANK)
                    .append(LEFT_BRACKET)
                    .append(NEW_LINE)
                    .append(TAB);
            // 获取表的所有字段
            List<Field> columns = table.getColumns();
            // 存放如果是text类型且为索引的字段名字
            List<String> indexFields = new ArrayList<>();
            List<String> indexSqlList = new ArrayList<>();
            // 遍历字段
            for (int i = 0; i < columns.size(); i++) {
                Field field = columns.get(i);
                // 获取字段名
                String fieldName = field.getName();
                // 获取字段约束对象
                FieldConstraint constraint = field.getConstraint();
                if (null == constraint) {
                    // 为空给空对象，使对象内的属性有默认值，防止NullPointException
                    constraint = new FieldConstraint();
                }
                // 获取字段属性 varchar(256)
                String type = field.getType();
                // 类型为text进行处理
                if (field.getIndexFlag()) {
                    // 属性是text类型
                    if (field.getTextFlag()) {
                        // 说明该字段属性为text text(256)
                        // 截取属性 text普通字段不需要配置长度
                        String indexTextLength = "";
                        if (type.matches(BRACKET_REGEX)) {
                            indexTextLength = type.substring(type.indexOf(LEFT_BRACKET), type.indexOf(RIGHT_BRACKET) + 1);
                            type = type.substring(0, type.indexOf(LEFT_BRACKET));
                        }
                        // 将这个属性名(长度) 放入一个集合中，最后将如果是联合索引的情况存在这个属性名则替换掉
                        // 全文索引不需要
                        if (StringUtils.isEmpty(constraint.getFullTextIndex())) {
                            indexFields.add(BACK_TICK + fieldName + BACK_TICK + indexTextLength);
                        }
                    }
                    // 获取索引sql
                    indexSqlList.addAll(constraint.getIndexSql());
                }
                // tinyint、int、bigint、tinytext、text、longtext、date、timestamp不需要设置长度
                if (UnRequireSetTypeLength(type)) {
                    if (type.matches(BRACKET_REGEX)) {
                        type = type.substring(0, type.indexOf(LEFT_BRACKET));
                    }
                }
                // `test_field_1` VARCHAR(45)
                sql.append(BACK_TICK)
                        .append(fieldName)
                        .append(BACK_TICK)
                        .append(BLANK)
                        .append(type.toUpperCase())
                        .append(BLANK);

                // 不是主键
                if (StringUtils.isEmpty(constraint.getPrimaryKey())) {
                    // 是否非空
                    if (StringUtils.isEmpty(constraint.getNotNull())) {
                        // 为空
                        sql.append(NULL).append(BLANK);
                    } else {
                        // 非空
                        sql.append(NOT_NULL).append(BLANK);
                    }
                } else {  // 是主键
                    // 非空
                    sql.append(NOT_NULL).append(BLANK);

                    // 自增
                    if (!StringUtils.isEmpty(constraint.getAutoIncrement())) {
                        sql.append(AUTO_INCREMENT).append(BLANK);
                    }

                    // 构建主键sql
                    primaryKeySql.append(PRIMARY_KEY)
                            .append(BLANK)
                            .append(LEFT_BRACKET)
                            .append(BACK_TICK)
                            .append(fieldName)
                            .append(BACK_TICK)
                            .append(RIGHT_BRACKET);
                }
                // 默认值
                if (!StringUtils.isEmpty(field.getDefaultValue())) {
                    sql.append(DEFAULT)
                            .append(BLANK);
                    if (type.contains("char")) {
                        // char 类型的默认值需要''括起来
                        sql.append(SINGLE_QUOTES).append(field.getDefaultValue()).append(SINGLE_QUOTES).append(BLANK);
                    } else {
                        sql.append(field.getDefaultValue()).append(BLANK);
                    }

                }
                // 注释
                if (!StringUtils.isEmpty(field.getComment())) {
                    sql.append(COMMENT)
                            .append(BLANK)
                            .append(SINGLE_QUOTES)
                            .append(field.getComment())
                            .append(SINGLE_QUOTES)
                            .append(COMMA);
                }else {
                    sql.append(COMMA);
                }
                // 换行
                sql.append(NEW_LINE).append(TAB);
                // 判断是否时最后一个字段
                if (!(i == columns.size() - 1)) {
                    // 非最后一个字段则进入下一个字段处理
                    continue;
                }
                sql.append(primaryKeySql);
            }
            // 有索引拼接索引sql
            if (!indexSqlList.isEmpty()) {
                for (int i = 0; i < indexSqlList.size(); i++) {
                    indexSql.append(indexSqlList.get(i));
                    if (!(i == indexSqlList.size() - 1)) {
                        indexSql.append(COMMA).append(PLACEHOLDER).append(NEW_LINE).append(TAB);
                    }
                }
                // 是否存在text类型的字段被设置为联合索引或联合唯一索引时未给与长度
                String judgement = indexSql.toString();
                // 把索引名字去掉 和本身就是索引的去掉
                StringBuilder temp = new StringBuilder();
                for (String indexField : indexFields) {
                    // 去掉索引名字
                    judgement = judgement.replace(IDX + indexField.substring(0, indexField.indexOf(LEFT_BRACKET)), "");
                    // 将字符传通过","截取，然后一行一行的处理
                    String[] split = judgement.split("\\" + PLACEHOLDER);
                    for (int i = 0; i < split.length; i++) {
                        String s = split[i];
                        if (s.matches(JUDGEMENT_REGEX) && !s.matches(FULLTEXT_REGEX)) {
                            temp.append(s).append(PLACEHOLDER);
                            continue;
                        }
                        String replace = s.replace(indexField, "");
                        temp.append(replace);
                        if (!(i == split.length - 1)) {
                            temp.append(PLACEHOLDER);
                        }
                    }
                    judgement = temp.toString();
                    temp.delete(0, temp.length());
                }

                // 判断
                for (String indexField : indexFields) {
                    boolean contains = judgement.contains(indexField);
                    if (!contains) {
                        throw new UnsupportedOperationException("存在字段{" + indexField.substring(0, indexField.indexOf(LEFT_BRACKET)) + "},在创建索引时未给予长度,位于表{" + name + "}");
                    }
                }
                // 去掉占位符
                String replace = indexSql.toString().replace(PLACEHOLDER, "");
                // 拼接索引
                sql.append(COMMA).append(NEW_LINE).append(TAB).append(replace).append(NEW_LINE);
            } else {
                sql.append(NEW_LINE);
            }
            sql.append(RIGHT_BRACKET)
                    .append(BLANK)
                    .append(ENGINE)
                    .append(BLANK)
                    .append(EQUAL)
                    .append(BLANK)
                    .append(INNODB)
                    .append(BLANK)
                    .append(CHARACTER)
                    .append(BLANK)
                    .append(SET)
                    .append(BLANK)
                    .append(EQUAL)
                    .append(BLANK)
                    .append(UTF8MB4)
                    .append(BLANK)
                    .append(COLLATE)
                    .append(BLANK)
                    .append(EQUAL)
                    .append(UTF9MB4_0900_AI_CI);
            if (!StringUtils.isEmpty(table.getComment())) {
                sql.append(BLANK)
                        .append(COMMENT)
                        .append(EQUAL)
                        .append(BLANK)
                        .append(SINGLE_QUOTES)
                        .append(table.getComment())
                        .append(SINGLE_QUOTES);
            }
            sql.append(SEMICOLON);
            sqlMap.put(name, sql.toString());
            // 打印 Map 中的所有键值对
            sql.delete(0, sql.length());
            indexSql.delete(0, indexSql.length());
            primaryKeySql.delete(0, primaryKeySql.length());
        }
        return sqlMap;
    }

    /**
     * 字段的这些属性不需要设置长度
     *
     * @param type 字段类型
     * @return true 不需要设置长度 false需要设置长度
     */
    private boolean UnRequireSetTypeLength(String type) {
        return type.contains("tinyint") || type.contains("int") || type.contains("bigint") || type.contains("tinytext")
                || type.contains("text") || type.contains("longtext") || type.matches("date") || type.contains("timestamp");
    }

    /**
     * 解析文档
     *
     * @param documentPath 构建数据库表文档的绝对路径
     * @return 表的集合
     */
    private List<Table> parserDocument(String documentPath) throws IOException {
        List<Table> tables = new ArrayList<>();
        //加载文档
        try (FileInputStream in = new FileInputStream(documentPath)) {
            // .docx后缀的文件 office2007 以后的版本  .doc后缀的文件 office2003的版本
            if (documentPath.endsWith(DOCX)) {
                // 解析 .docx 文件
                // 获取word文档信息
                XWPFDocument document = new XWPFDocument(in);
                // 获取文件中的表格
                Iterator<XWPFTable> documentTables = document.getTablesIterator();
                // 解析表格数据
                parserDocumentTables(documentTables, tables);
                return tables;
            }
            if (documentPath.endsWith(DOC)) {
                // 解析 .doc 文件
                throw new UnsupportedOperationException("暂时不支持解析.doc文档");
            }
            // 不支持的文件格式
            throw new UnsupportedOperationException("无法解析的文件");
        }
    }

    /**
     * 解析文档中的表格内容 docx
     *
     * @param documentTables 文档中的表格
     * @param tables         存放解析出的表的信息集合
     */
    private void parserDocumentTables(Iterator<XWPFTable> documentTables, List<Table> tables) {
        String tableName = "";
        // 遍历表格
        while (documentTables.hasNext()) {
            Table table = new Table();
            // 获取表格
            XWPFTable xwpfTable = documentTables.next();
            // 获取行列表
            List<XWPFTableRow> rows = xwpfTable.getRows();
            StringBuilder line = new StringBuilder();
            // 遍历每一行
            for (XWPFTableRow row : rows) {
                if (rows.isEmpty()) {
                    continue;
                }
                // 获取当前行的所有列
                List<XWPFTableCell> cells = row.getTableCells();
                int count = 0;
                line.append(LINE_SEPARATOR);
                // 遍历当前行的每一列
                for (XWPFTableCell cell : cells) {
                    String text = cell.getText();
                    text = text.replace("：", COLON);
                    if (text.matches(TABLE_NAME_REGEX)) {
                        tableName = text;
                    }
                    if (StringUtils.isEmpty(text)) {
                        count++;
                        // 空单元格最多存在2个(即：默认值和属性可以为空)
                        if (count > 2) {
                            throw new UnsupportedOperationException(tableName + "存在空单元格");
                        }
                    }
                    line.append(text).append(LINE_SEPARATOR);
                }
                // |字段|类型|默认|属性|注释|
                // 构建Table 整合表信息
                conformTable(table, line.toString());
                line.delete(0, line.length());
            }
            if (StringUtils.isEmpty(table.getName())) {
                // 存在没有填写表名的表
                throw new UnsupportedOperationException("存在没有设置表名的表");
            }
            if (!table.getPrimaryKeyFlag()) {
                // 说明此表没有主键
                throw new UnsupportedOperationException("表{" + table.getName() + "}没有设置主键");
            }
            tables.add(table);
        }
    }

    /**
     * 整合表信息
     *
     * @param table 用于构建数据库表sql的表信息
     * @param line  文档中每个表格中每行处理过的字符串集合
     */
    private void conformTable(Table table, String line) {
        // \s 匹配任何空白字符，包括空格、制表符、换行符
        String replace = line.replaceAll("\\s", "")
                .replaceAll("、，", COMMA)
                .replace("（", LEFT_BRACKET)
                .replace("）", RIGHT_BRACKET);
        // 表名
        if (replace.replace(LINE_SEPARATOR, "").matches(TABLE_NAME_REGEX)) {
            String tableName = replace.replace(TABLE_NAME, "")
                    .replace(LINE_SEPARATOR, "");
            // 是否有表注解
            if (tableName.matches(BRACKET_REGEX)) {
                // 获取注解
                String tableComment = tableName.substring(tableName.indexOf(LEFT_BRACKET) + 1, tableName.lastIndexOf(RIGHT_BRACKET));
                table.setComment(tableComment);
                tableName = tableName.substring(0, tableName.lastIndexOf(LEFT_BRACKET));
            }
            // 设置表名
            table.setName(tableName);
        } else if (replace.matches(ROW_REGEX)) {
            // 字段
            Field field = new Field();
            // |id|bigint||主键|ID| 去掉第一个 | 在截取
            String[] split = replace.substring(1).split("\\|",-1);
            // ["id", "bigint", "", "主键", "ID"]
            // 设置字段名
            String fieldName = split[0];
            // removePrefix = true 时去掉字段前缀
            fieldName = removeFieldNamePrefix(fieldName);
            field.setName(fieldName);
            // 设置字段类型
            String type = split[1];
            // 判断 char、varchar、datetime、decimal是否给了长度，没有就报错
            if (type.contains("varchar") && !type.matches(BRACKET_REGEX)) {
                throw new UnsupportedOperationException("字段类型{varchar},字段{" + fieldName + "},位与表{" + table.getName() + "},未给予长度");
            }
            // 如果字段类型是 tinytext、text、longtext 时标记为true
            boolean tag = false;
            String typeSubString = type;
            if (type.matches(BRACKET_REGEX)) {
                typeSubString = type.substring(0, type.indexOf(LEFT_BRACKET));
            }
            if (typeSubString.contains("text")) {
                tag = true;
                // 设置字段属性是否是text为true
                field.setTextFlag(true);
            }
            for (String fieldType : fieldType) {
                if (StringUtils.equals(typeSubString, fieldType)) {
                    // 设置字段类型
                    field.setType(type);
                    break;
                }
            }
            if (StringUtils.isEmpty(field.getType())) {
                throw new UnsupportedOperationException("非法的字段类型{" + type + "},存在于表名{" + table.getName() + "}");
            }
            // 设置默认值
            field.setDefaultValue(split[2]);
            // 设置属性
            String attributeString = split[3];
            if (!StringUtils.isEmpty(attributeString)) {
                FieldConstraint constraint = new FieldConstraint();
                // 非空，联合唯一索引 () 截取括号之前的片段
                String substring = attributeString;
                if (attributeString.contains(LEFT_BRACKET)) {
                    substring = attributeString.substring(0, attributeString.indexOf(LEFT_BRACKET));
                }
                String[] temp = substring.split("[，,、]");
                for (String t : temp) {
                    // 解析字段约束
                    parserFieldConstraint(table, t, constraint, field, attributeString, type, tag);
                }
                // 设置字段约束与属性
                field.setConstraint(constraint);
            }
            // 设置注释
            field.setComment(split[4]);
            // 将解析好的字段设置到table中
            table.getColumns().add(field);
        }
    }

    /**
     * 解析字段约束，构建准备sql
     *
     * @param table           表
     * @param t               当前字段的约束属性
     * @param constraint      字段约束对象
     * @param field           字段
     * @param attributeString 字段约束字符串来自文档
     * @param type            字段属性
     * @param tag             标记
     */
    private void parserFieldConstraint(Table table, String t, FieldConstraint constraint,
                                       Field field, String attributeString, String type, boolean tag) {
        switch (t) {
            case PRIMARY_KEY_NAME: // 主键
                constraint.setPrimaryKey(PRIMARY_KEY);
                table.setPrimaryKeyFlag(true);
                break;
            case AUTO_INCREMENT_NAME: // 自增
                constraint.setAutoIncrement(AUTO_INCREMENT);
                break;
            case NOT_NULL_NAME: // 非空
                constraint.setNotNull(NOT_NULL);
                break;
            case INDEX_NAME: // 索引
                // 组装索引sql
                String indexSql = handleIndex(table.getName(), field.getName(), type, tag);
                field.setIndexFlag(true);
                constraint.setIndex(indexSql);
                constraint.getIndexSql().add(indexSql);
                break;
            case UNIQUE_INDEX_NAME: // 唯一
                String uniqueIndex = handleUniqueIndex(table.getName(), field.getName(), type, tag);
                field.setIndexFlag(true);
                constraint.setUniqueIndex(uniqueIndex);
                constraint.getIndexSql().add(uniqueIndex);
                break;
            case UNION_INDEX_NAME: // 联合索引
                String unionIndexSql = handleUnionIndex(table.getName(), field.getName(), type, attributeString, tag);
                // 设置联合索引约束
                field.setIndexFlag(true);
                constraint.setUnionIndex(unionIndexSql);
                constraint.getIndexSql().add(unionIndexSql);
                break;
            case UNION_UNIQUE_INDEX_NAME: // 联合唯一索引
                String unionUniqueIndexSql = handleUnionUniqueIndex(table.getName(), field.getName(), type, attributeString, tag);
                // 设置联合唯一索引
                field.setIndexFlag(true);
                constraint.setUnionUniqueIndex(unionUniqueIndexSql);
                constraint.getIndexSql().add(unionUniqueIndexSql);
                break;
            case FULL_TEXT_INDEX_NAME: // 全文索引
                String fullTextIndexSql = handleFullTextIndex(table.getName(), field.getName(), attributeString);
                // 设置全文索引
                field.setIndexFlag(true);
                constraint.setFullTextIndex(fullTextIndexSql);
                constraint.getIndexSql().add(fullTextIndexSql);
                break;
            default:
                throw new UnsupportedOperationException("非法的字段约束或属性{" + t + "},来自表{" + table.getName() + "},字段名为{" + field.getName() + "}");
        }
    }

    /**
     * 处理全文索引
     *
     * @param tableName       表名
     * @param fieldName       字段名字
     * @param attributeString 字段约束字符串来自文档
     * @return 全文索引sql
     */
    private String handleFullTextIndex(String tableName, String fieldName, String attributeString) {
        // 截取括号内的内容
        String text;
        try {
            text = attributeString.substring(attributeString.indexOf(LEFT_BRACKET) + 1,
                    attributeString.lastIndexOf(RIGHT_BRACKET));
        } catch (StringIndexOutOfBoundsException e) {
            throw new UnsupportedOperationException("表『" + tableName + "』字段『" + fieldName + "』给定正确的属性");
        }
        String[] indexes = text.split(COMMA);
        StringBuilder sb = new StringBuilder(BLANK);
        sb.append(FULL_TEXT_INDEX)
                .append(BLANK)
                .append(BACK_TICK)
                .append(IDX)
                .append(fieldName)
                .append(UNDER_LINE)
                .append("FULLTEXT")
                .append(BACK_TICK)
                .append(BLANK)
                .append(LEFT_BRACKET)
                .append(BLANK);
        for (int i = 0; i < indexes.length; i++) {
            // 去除字段前缀
            indexes[i] = removeFieldNamePrefix(indexes[i]);
            // 这里需要对
            sb.append(BACK_TICK)
                    .append(indexes[i])
                    .append(BACK_TICK);
            if (i == indexes.length - 1) {
                // 最后一个时
                sb.append(BLANK).append(RIGHT_BRACKET);
                break;
            }
            sb.append(COMMA).append(BLANK);
        }
//        sb.append(BLANK).append(WITH_PARSER_NGRAM);
        return sb.toString();
    }

    /**
     * 处理联合唯一索引
     *
     * @param tableName       名
     * @param fieldName       字段名字
     * @param type            字段属性
     * @param attributeString 字段约束字符串来自文档
     * @param tag             标记
     * @return 联合唯一索引sql
     */
    private String handleUnionUniqueIndex(String tableName, String fieldName, String type, String attributeString, boolean tag) {
        // 截取括号内的内容
        String text;
        try {
            text = attributeString.substring(attributeString.indexOf(LEFT_BRACKET) + 1,
                    attributeString.lastIndexOf(RIGHT_BRACKET));
        } catch (StringIndexOutOfBoundsException e) {
            throw new UnsupportedOperationException("表『" + tableName + "』字段『" + fieldName + "』给定正确的属性");
        }
        String[] indexes = text.split(COMMA);
        StringBuilder sb = new StringBuilder(BLANK);
        sb.append(UNION_UNIQUE_INDEX)
                .append(BLANK)
                .append(BACK_TICK)
                .append(IDX)
                .append(fieldName)
                .append(UNDER_LINE)
                .append("UNIQUE")
                .append(BACK_TICK)
                .append(BLANK)
                .append(LEFT_BRACKET)
                .append(BLANK);
        for (int i = 0; i < indexes.length; i++) {
            // 去除字段前缀
            indexes[i] = removeFieldNamePrefix(indexes[i]);
            if (tag && StringUtils.equals(indexes[i], fieldName)) {
                if (!indexes[i].matches(BRACKET_REGEX) && indexes[i].contains(fieldName)) {
                    // 说明未配置长度
                    throw new UnsupportedOperationException("错误的属性「" + type + "」,存在字段名为「" + fieldName + "」,创建联合唯一索引时「" + indexes[i] + "」为给予长度,位于表「" + tableName + "」");
                }
            }
            if (tag && indexes[i].contains(LEFT_BRACKET)) {
                sb.append(BACK_TICK)
                        .append(indexes[i], 0, indexes[i].indexOf(LEFT_BRACKET))
                        .append(BACK_TICK)
                        .append(indexes[i], indexes[i].indexOf(LEFT_BRACKET), indexes[i].indexOf(RIGHT_BRACKET) + 1)
                        .append(BLANK)
                        .append(ASC);
            } else {
                sb.append(BACK_TICK)
                        .append(indexes[i])
                        .append(BACK_TICK)
                        .append(BLANK)
                        .append(ASC);
            }
            if (i == indexes.length - 1) {
                // 最后一个时
                sb.append(BLANK).append(RIGHT_BRACKET);
                break;
            }
            sb.append(COMMA).append(BLANK);
        }
        sb.append(BLANK).append(USING_BTREE);
        return sb.toString();
    }

    /**
     * 处理联合索引
     *
     * @param tableName       名
     * @param fieldName       字段名字
     * @param type            字段属性
     * @param attributeString 字段约束字符串来自文档
     * @param tag             标记
     * @return 联合索引sql
     */
    private String handleUnionIndex(String tableName, String fieldName, String type, String attributeString, boolean tag) {
        // 截取括号内的内容
        String text;
        try {
            text = attributeString.substring(attributeString.indexOf(LEFT_BRACKET) + 1,
                    attributeString.lastIndexOf(RIGHT_BRACKET));
        } catch (StringIndexOutOfBoundsException e) {
            throw new UnsupportedOperationException("表『" + tableName + "』字段『" + fieldName + "』给定正确的属性");
        }
        String[] indexes = text.split(COMMA);
        StringBuilder sb = new StringBuilder(BLANK);
        sb.append(UNION_INDEX)
                .append(BLANK)
                .append(BACK_TICK)
                .append(IDX)
                .append(fieldName)
                .append(UNDER_LINE)
                .append("UNION_INDEX")
                .append(BACK_TICK)
                .append(BLANK)
                .append(LEFT_BRACKET)
                .append(BLANK);
        for (int i = 0; i < indexes.length; i++) {
            // 去除字段前缀
            indexes[i] = removeFieldNamePrefix(indexes[i]);
            if (tag && StringUtils.equals(indexes[i], fieldName) && indexes[i].contains(fieldName)) {
                if (!indexes[i].matches(BRACKET_REGEX)) {
                    // 说明未配置长度
                    throw new UnsupportedOperationException("错误的属性「" + type + "」,存在字段名为「" + fieldName + "」,创建联合索引时「" + indexes[i] + "」为给予长度,位于表「" + tableName + "」");
                }
            }
            if (tag && indexes[i].contains(LEFT_BRACKET)) {
                sb.append(BACK_TICK)
                        .append(indexes[i], 0, indexes[i].indexOf(LEFT_BRACKET))
                        .append(BACK_TICK)
                        .append(indexes[i], indexes[i].indexOf(LEFT_BRACKET), indexes[i].indexOf(RIGHT_BRACKET) + 1)
                        .append(BLANK)
                        .append(ASC);
            } else {
                sb.append(BACK_TICK)
                        .append(indexes[i])
                        .append(BACK_TICK)
                        .append(BLANK)
                        .append(ASC);
            }
            if (i == indexes.length - 1) {
                // 最后一个时
                sb.append(BLANK).append(RIGHT_BRACKET);
                break;
            }
            sb.append(COMMA).append(BLANK);
        }
        sb.append(BLANK).append(USING_BTREE);
        return sb.toString();
    }

    /**
     * 处理唯一
     *
     * @param tableName 名
     * @param fieldName 字段名字
     * @param type      字段属性
     * @param tag       标记
     * @return 唯一sql
     */
    private String handleUniqueIndex(String tableName, String fieldName, String type, boolean tag) {
        String uniqueIndex = BLANK + UNIQUE_INDEX + BLANK + BACK_TICK + IDX + fieldName + UNDER_LINE +
                "UNIQUE" + BACK_TICK + BLANK + LEFT_BRACKET + BLANK + BACK_TICK + fieldName + BACK_TICK;
        if (tag) {
            if (!type.matches(BRACKET_REGEX)) {
                // 说明未配置长度
                throw new UnsupportedOperationException("错误的属性「" + type + "」,存在字段名为「" + fieldName + "」,创建唯一索引时为给予长度,位于表「" + tableName + "」");
            }
            // 获取text属性的长度
            String substring = type.substring(type.indexOf(LEFT_BRACKET));
            uniqueIndex += substring;
        }
        uniqueIndex += BLANK + ASC + BLANK + RIGHT_BRACKET + BLANK + USING_BTREE;
        return uniqueIndex;
    }

    /**
     * 处理索引
     *
     * @param tableName 表名
     * @param fieldName 字段名
     * @param type      字段属性
     * @param tag       标记
     * @return 索引sql
     */
    private String handleIndex(String tableName, String fieldName, String type, boolean tag) {
        String indexSql = BLANK + INDEX + BLANK + BACK_TICK + IDX + fieldName + BACK_TICK + BLANK + LEFT_BRACKET
                + BLANK + BACK_TICK + fieldName + BACK_TICK;
        if (tag) {
            if (!type.matches(BRACKET_REGEX)) {
                // 说明未配置长度
                throw new UnsupportedOperationException("错误的属性「" + type + "」,存在字段名为「" + fieldName + "」,创建索引时为给予长度,位于表「" + tableName + "」");
            }
            // 获取text属性的长度
            String substring = type.substring(type.indexOf(LEFT_BRACKET));
            indexSql += substring;
        }
        indexSql += BLANK + ASC + BLANK + RIGHT_BRACKET + BLANK + USING_BTREE;
        return indexSql;
    }

    /**
     * 去除字段名前缀
     *
     * @param fieldName 字段名
     * @return 去掉前缀的字段名
     */
    private String removeFieldNamePrefix(String fieldName) {
        if (removePrefix && fieldName.contains(UNDER_LINE)) {
            fieldName = fieldName.substring(fieldName.indexOf(UNDER_LINE) + 1);
        }
        return fieldName;
    }

}
