package com.xiaozhuzhu.autodatabase.v1.constance;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2024年01月31日17:52
 * @description: 构建表sql的常量
 */
public interface TableConstance {

    /** office2007 以后的版本文件后缀 */
    String DOCX = ".docx";
    /** .doc后缀的文件 office2003的版本本文件后缀 */
    String DOC = ".doc";
    /** | 符号 用于拼接表格每行的字符串 */
    String LINE_SEPARATOR = "|";
    /** 表名 */
    String TABLE_NAME = "表名:";
    /** 表名正则表达式 */
    String TABLE_NAME_REGEX = "表名:.*";
    /** 行正则表达是 \w+ 匹配一个或多个单词字符 .* 匹配零个或多个任意字符 */
    String ROW_REGEX = "\\|\\w+\\|.*\\|.*\\|.*\\|.*\\|";
    /** \\(.*\\) */
    String BRACKET_REGEX = ".*\\(.*\\)";
    /** xxx(xxx,xxx,...,xxx)xxx */
    String JUDGEMENT_REGEX = "\r\n.*\\(([^,]+(,[^,]+)*)\\).*";
    /** \r\n.*FULLTEXT.* */
    String FULLTEXT_REGEX = "\r\n.*FULLTEXT.*";
    /** \r\n */
    String NEW_LINE = "\r\n";
    /** \t */
    String TAB = "\t";
    /** ( */
    String LEFT_BRACKET = "(";
    /** ) */
    String RIGHT_BRACKET = ")";
    /** , */
    String COMMA = ",";
    /** : */
    String COLON = ":";
    /** ; */
    String SEMICOLON = ";";
    /** 占位符 */
    String PLACEHOLDER = "?";
    /** 空格 */
    String BLANK = " ";
    /** ` 反引号字符 */
    String BACK_TICK = "`";
    /** ' */
    String SINGLE_QUOTES = "'";
    /** " */
    String DOUBLE_QUOTES = "\"";
    /** _ 下划线 */
    String UNDER_LINE = "_";
    /** = */
    String EQUAL = "=";
    /** idx */
    String IDX = "idx_";
    /** ASC */
    String ASC = "ASC";
    /** USING BTREE */
    String USING_BTREE = "USING BTREE";
    /** WITH PARSER `ngram` */
    String WITH_PARSER_NGRAM = "WITH PARSER `ngram`";
    /** ENGINE */
    String ENGINE = "ENGINE";
    /** InnoDB */
    String INNODB = "InnoDB";
    /** CHARACTER */
    String CHARACTER = "CHARACTER";
    /** SET */
    String SET = "SET";
    /** utf8mb4 */
    String UTF8MB4 = "utf8mb4";
    /** COLLATE */
    String COLLATE = "COLLATE";
    /** utf8mb4_0900_ai_ci */
    String UTF9MB4_0900_AI_CI = "utf8mb4_0900_ai_ci";
    /** 主键 */
    String PRIMARY_KEY = "PRIMARY KEY";
    /** 自增 */
    String AUTO_INCREMENT = "AUTO_INCREMENT";
    /** 非空 */
    String NOT_NULL = "NOT NULL";
    /** 空 */
    String NULL = "NULL";
    /** 注释 */
    String COMMENT = "COMMENT";
    /** 默认值 */
    String DEFAULT = "DEFAULT";
    /** 索引 */
    String INDEX = "INDEX";
    /** 唯一 唯一(唯一约束、唯一索引，在 MySQL 中创建唯一约束会创建唯一索引，唯一约束是通过唯一索引实现的) */
    String UNIQUE_INDEX = "UNIQUE INDEX";
    /** 联合索引 */
    String UNION_INDEX = "INDEX";
    /** 联合唯一索引 */
    String UNION_UNIQUE_INDEX = "UNIQUE INDEX";
    /** 全文索引 */
    String FULL_TEXT_INDEX = "FULLTEXT INDEX";
    /** 主键 */
    String PRIMARY_KEY_NAME = "主键";
    /** 自增 */
    String AUTO_INCREMENT_NAME = "自增";
    /** 非空 */
    String NOT_NULL_NAME = "非空";
    /** 索引 */
    String INDEX_NAME = "索引";
    /** 唯一 */
    String UNIQUE_INDEX_NAME = "唯一";
    /** 联合索引 */
    String UNION_INDEX_NAME = "联合索引";
    /** 联合唯一索引 */
    String UNION_UNIQUE_INDEX_NAME = "联合唯一索引";
    /** 全文索引 */
    String FULL_TEXT_INDEX_NAME = "全文索引";
    /** CREATE TABLE */
    String CREATE_TABLE = "CREATE TABLE";
    /** 字段类型 */
    String[] fieldType = {
            "bit",
            "tinyint", "int", "bigint",
            "char", "varchar",
            "tinytext", "text", "longtext",
            "date", "datetime", "timestamp",
            "decimal"
    };

}
