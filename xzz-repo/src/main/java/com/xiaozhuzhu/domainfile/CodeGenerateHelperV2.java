package com.xiaozhuzhu.domainfile;

import com.xiaozhuzhu.domainfile.entity.TableField;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2024年04月10日10:54
 * @description: 实体类、DAO层类生成
 */
@Slf4j
@SuppressWarnings("all")
@AllArgsConstructor
public class CodeGenerateHelperV2 {

    /**
     * jdbc template
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 是否去除字段名前缀
     */
    private Boolean removePrefix = false;

    /**
     * 是否生成dao层类 true 需要生成(默认) false 不生成
     */
    private Boolean daoFile = true;
    /**
     * 项目路径
     */
    private String projectPath;

    /**
     * 类路径
     */
    private String classPath;

    /**
     * .
     */
    private final static String PERIOD = ".";

    /**
     * 空格
     */
    private final static String BLANK = " ";

    {
        String path = System.getProperty("user.dir") + "/src/main/java/";
        init(path);
    }


    public CodeGenerateHelperV2(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public CodeGenerateHelperV2(DataSource dataSource, Boolean removePrefix) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.removePrefix = removePrefix;
    }


    /**
     * 生成所有表
     */
    public void generate() {
        // 获取所有表名
        List<String> tableNames = queryAllTableNames();
        // 生成 DatabaseIDUtil java文件
        generateUtilFile(tableNames);
        for (String tableName : tableNames) {
            // 生成文件
            generateFile(tableName);
        }
    }

    /**
     * 生成 DatabaseIDUtil java文件
     *
     * @param tableNames 表名集合
     */
    private void generateUtilFile(List<String> tableNames) {
        // 包名
        String packageName = "package" + classPath + ".domain;\n\n";
        StringBuilder sb = new StringBuilder(packageName);
        sb.append("import com.bentongchain.utils.IdUtil;\n")
                .append("import com.bentongchain.utils.SnowflakeIdWorker;\n")
                .append("import finance.project.common.constant.Constant;\n")
                .append("import lombok.extern.slf4j.Slf4j;\n\n")
                .append("import java.util.HashMap;\n")
                .append("import java.util.Map;\n\n")
                // 注释部分
                .append("/**\n")
                .append(" * @author xiaozhuzhu\n")
                .append(" * @version 1.0\n")
                .append(" * @Date ").append(
                        new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date())
                ).append("\n")
                .append(" * @description: TODO\n")
                .append(" */\n")
                .append("@Slf4j\n")
                .append("public class DatabaseIDUtil {\n\n")
                .append("\t// 需要生成id的表名分配序号\n")
                .append("\tprivate final static String[] TABLES = {\n");
        for (String tableName : tableNames) {
            sb.append("\t\t\t\"").append(tableName).append("\",\n");
        }
        sb.append("\t};\n\n")
                .append("\tprivate static final Map<String, IdUtil> idUtils = new HashMap<String, IdUtil>();\n\n")
                .append("\t/**\n")
                .append("\t *\n")
                .append("\t * 传入表名，生成下一个id\n")
                .append("\t * @param tableName 表名\n")
                .append("\t * @return id\n")
                .append("\t */\n")
                .append("\tpublic static long nextId(String tableName) {\n")
                .append("        IdUtil idUtil = idUtils.get(tableName);\n" +
                        "\n" +
                        "        if (idUtil == null) {\n" +
                        "            long workerId = Constant.getWorkerId();\n" +
                        "            long custom = 0L;\n" +
                        "\n" +
                        "            for (int i = 0; i < TABLES.length; i++) {\n" +
                        "                String name = TABLES[i];\n" +
                        "                if (name.equals(tableName)) {\n" +
                        "                    custom = i;\n" +
                        "                }\n" +
                        "            }\n" +
                        "\n" +
                        "            idUtil = new SnowflakeIdWorker(workerId, custom);\n" +
                        "            idUtils.put(tableName, idUtil);\n" +
                        "            if (log.isDebugEnabled()) {\n" +
                        "                log.debug(\"生成工具对象：\" + tableName);\n" +
                        "            }\n" +
                        "        } else {\n" +
                        "            if (log.isDebugEnabled()) {\n" +
                        "                log.debug(\"使用工具对象：\" + tableName);\n" +
                        "            }\n" +
                        "        }\n" +
                        "        return idUtil.nextId();\n" +
                        "    }\n" +
                        "}");
        // 输出路径
        // 文件名
        String typeFileName = "DatabaseIDUtil.java";
        // 文件输出目录
        String outputPath = projectPath + "domain/";
        // 输出java文件
        outputJavaFile(outputPath, typeFileName, sb.toString());
    }

    /**
     * 指定表名
     *
     * @param tableNames 表名
     */
    public void generate(String... tableNames) {
        for (String tableName : tableNames) {
            // 生成文件
            generateFile(tableName);
        }
    }

    /**
     * 根据表名生成文件
     *
     * @param tableName 表名
     */
    private void generateFile(String tableName) {
        // 生成实体类
        generateEntity(tableName);
        if (daoFile) {
            // 生成DAO
            generateDao(tableName);
            // 生成DAOImpl
            generateDaoImpl(tableName);
        }
    }

    /**
     * 生成Dao层
     *
     * @param tableName 表名
     */
    private void generateDaoImpl(String tableName) {
        // 获取表名转大驼峰
        String typeName = underlineToCamel(tableName, true);
        // 生成类名
        String daoImplName = typeName + "DaoImpl";
        // 生成包名
        String packageName = "package " + classPath + ".dao.impl;\n\n";
        // 注释部分
        String sb = packageName +
                "import " + classPath + ".dao." + typeName + "Dao" + ";\n" +
                "import com.bentongchain.system.BaseDao;\n" +
                "import lombok.extern.slf4j.Slf4j;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.jdbc.core.JdbcTemplate;\n" +
//                "import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;\n" +
                "import org.springframework.stereotype.Repository;\n\n" +
                "/**\n" +
                " * @author xiaozhuzhu\n" +
                " * @version 1.0\n" +
                " * @Date " +
                new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date()) +
                "\n" +
                " * @description: TODO\n" +
                " */\n" +
                "@Slf4j\n" +
                "@Repository\n" +
                "public class " + daoImplName + " extends BaseDao implements " + typeName + "Dao" + " {\n\n" +
                "\t@Autowired\n" +
                "\tprivate JdbcTemplate jdbcTemplate;\n" +
//                "\t@Autowired\n" +
//                "\tprivate NamedParameterJdbcTemplate namedParameterJdbcTemplate;\n\n\n" +
                "}";
        // 文件名
        String typeFileName = daoImplName + ".java";
        // 文件输出目录
        String outputPath = projectPath + "dao/impl/";
        // 输出java文件
        outputJavaFile(outputPath, typeFileName, sb);
    }

    /**
     * 生成Dao实现层
     *
     * @param tableName 表名
     */
    private void generateDao(String tableName) {
        // 生成类名
        String daoName = underlineToCamel(tableName, true) + "Dao";
        // 生成包名
        String packageName = "package " + classPath + ".dao;\n\n";
        String content = packageName + "/**\n" +
                // 注释部分
                " * @author xiaozhuzhu\n" +
                " * @version 1.0\n" +
                " * @Date " +
                new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date()) +
                "\n" +
                " * @description: TODO\n" +
                " */\n" +
                "public interface " + daoName + " {\n\n" +
                "\tString TABLE_NAME = \"" + tableName + "\";\n\n\n" + "}";
        // 文件名
        String typeFileName = daoName + ".java";
        // 文件输出目录
        String outputPath = projectPath + "dao/";
        // 输出java文件
        outputJavaFile(outputPath, typeFileName, content);
    }


    /**
     * 生成实体类
     *
     * @param tableName 表名
     */
    private void generateEntity(String tableName) {
        // 生成类的内容
        String typeContent = generateEntityContent(tableName);
        // 文件名
        String typeFileName = underlineToCamel(tableName, true) + ".java";
        // 文件输出目录
        String outputPath = projectPath + "domain/";
        // 输出java文件
        outputJavaFile(outputPath, typeFileName, typeContent);
    }

    /**
     * 输出Java文件
     *
     * @param outputPath   输出路径
     * @param typeFileName 类文件名
     * @param typeContent  类的内容
     */
    private void outputJavaFile(String outputPath, String typeFileName, String typeContent) {
        File dir = new File(outputPath);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                log.error("无法创建目录，请检查权限或路径是否正确！");
            }
        }
        if (new File(outputPath + typeFileName).exists()) {
            log.info("文件{}已存在", typeFileName);
            return;
        }
        // 输出类文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath + typeFileName))) {
            writer.write(typeContent);
            writer.flush();
        } catch (IOException e) {
            log.error("生成{}--->失败", typeFileName, e);
        }
    }

    /**
     * 生成实体类的内容
     *
     * @param tableName 表名
     * @return 实体类的内容
     */
    private String generateEntityContent(String tableName) {
        // 生成类名
        String typeName = underlineToCamel(tableName, true);
        // 生成包名
        String packageName = "package " + classPath + ".domain;\n\n";
        // 获取表的字段
        List<TableField> fields = tableFieldDefinition(tableName);
        StringBuilder fieldContent = new StringBuilder();
        boolean longFlag = false;
        boolean dateFlag = false;
        boolean bigDecimalFlag = false;
        for (TableField field : fields) {
            // 获取字段类型
            String fieldType = field.getFieldType();
            // 获取字段名
            String fieldName = field.getTypeFieldName(removePrefix);
            // 获取字段注释
            String comment = Optional.ofNullable(field.getComment()).orElse("");
            // 判断是否是 Long 类型
            if (StringUtils.equals("Long", fieldType)) {
                if (!longFlag) {
                    longFlag = true;
                }
                fieldContent.append("\t@JsonFormat(shape = JsonFormat.Shape.STRING)\n");
            }
            if (StringUtils.equals("BigDecimal", fieldType) && !bigDecimalFlag) {
                bigDecimalFlag = true;
            }
            if (StringUtils.equals("Date", fieldType)) {
                if (!dateFlag) {
                    dateFlag = true;
                }
                fieldContent.append("\t@JsonFormat(timezone = \"GMT+8\", pattern = \"yyyy-MM-dd HH:mm:ss\")\n")
                        .append("\t@DateTimeFormat(pattern = \"yyyy-MM-dd HH:mm:ss\")\n");
            }
            fieldContent.append("\t@ApiModelProperty(\"").append(comment).append("\")\n")
                    .append("\tprivate ").append(fieldType).append(BLANK).append(fieldName).append(";\n\n");
        }
        // 组合类的上半部分
        StringBuilder typeContent = new StringBuilder();
        typeContent.append(packageName);
        if (longFlag || dateFlag) {
            typeContent.append("import com.fasterxml.jackson.annotation.JsonFormat;\n");
        }
        typeContent.append("import io.swagger.annotations.ApiModelProperty;\n")
                .append("import lombok.*;\n")
                .append("import lombok.experimental.Accessors;\n");
        if (dateFlag) {
            typeContent.append("import org.springframework.format.annotation.DateTimeFormat;\n");
        }
        typeContent.append("import org.springframework.jdbc.core.RowMapper;\n\n")
                .append("import java.io.Serializable;\n")
                .append("import java.sql.ResultSet;\n");
        if (dateFlag) {
            typeContent.append("import java.util.Date;\n");
        }
        if (bigDecimalFlag) {
            typeContent.append("import java.math.BigDecimal;\n");
        }
        typeContent.append("import java.sql.SQLException;\n\n")
                // 注释部分
                .append("/**\n")
                .append(" * @author xiaozhuzhu\n")
                .append(" * @version 1.0\n")
                .append(" * @Date ").append(
                        new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date())
                ).append("\n")
                .append(" * @description: TODO\n")
                .append(" */\n")
                // 类名
                .append("@Getter\n")
                .append("@Setter\n")
                .append("@NoArgsConstructor\n")
                .append("@AllArgsConstructor\n")
                .append("@Accessors(chain = true)\n")
                .append("public class ").append(typeName).append(" implements RowMapper<").append(typeName).append(">, Serializable {\n\n")
                .append("\tprivate static final long serialVersionUID = 1L;\n\n")
                // 拼接字段
                .append(fieldContent);

        // 构造重写RowMapper<>方法
        StringBuilder method = new StringBuilder();
        // 首字母小写的类名
        String lowerTypeName = firstLetterUpperAndLower(typeName, false);
        method.append("\t@Override\n")
                .append("\tpublic ").append(typeName).append(" mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {\n")
                .append("\t\t").append(typeName).append(BLANK).append(lowerTypeName).append(" = ").append("new ").append(typeName).append("();\n");
        for (TableField field : fields) {
            // 获取字段
            String fieldName = field.getFieldName();
            String typeFieldName = field.getTypeFieldName(removePrefix);
            String rowMapperFieldType = field.getRowMapperFieldType();
            method.append("\t\t").append(lowerTypeName).append(PERIOD).append("set").append(firstLetterUpperAndLower(typeFieldName, true))
                    .append("(isExistColumn(rs, \"").append(fieldName).append("\") ? rs.").append(rowMapperFieldType).append("(\"")
                    .append(fieldName).append("\") : null);\n");
        }
        method.append("\t\treturn ").append(lowerTypeName).append(";\n").append("\t}\n\n")
                // isExistColumn 方法
                .append("\tpublic boolean isExistColumn(ResultSet rs, String columnName) {\n")
                .append("\t\ttry {\n")
                .append("\t\t\tif (rs.findColumn(columnName) > 0) {\n")
                .append("\t\t\t\t return true;\n")
                .append("\t\t\t}\n")
                .append("\t\t} catch (SQLException e) {\n")
                .append("\t\t\treturn false;\n")
                .append("\t\t}\n")
                .append("\t\treturn false;\n")
                .append("\t}\n");
        typeContent.append(method).append("}");
        return typeContent.toString();
    }

    /**
     * 描述表的字段
     *
     * @param tableName 表名
     * @return 字段的描述集合
     */
    private List<TableField> tableFieldDefinition(String tableName) {
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SHOW FULL COLUMNS FROM " + tableName);
        List<TableField> fields = new ArrayList<>();
        for (Map<String, Object> row : resultList) {
            // {Field=id, Type=bigint, Collation=null, Null=NO, Key=PRI, Default=null, Extra=, Privileges=select,insert,update,references, Comment=ID}
            TableField field = new TableField();
            field.setFieldName((String) row.get("Field"))
                    .setFieldType((String) row.get("Type"))
                    .setPrimaryKey(StringUtils.equals(row.get("Key").toString(), "PRI"))
                    .setComment(((String) row.get("Comment")).replace("\r", "").replace("\n", ""));
            fields.add(field);
        }
        return fields;
    }


    /**
     * 获取所有表名
     *
     * @return 所有表名
     */
    private List<String> queryAllTableNames() {
        // 获取数据库库名
        String databaseName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        // 获取所有表名
        List<Map<String, Object>> results = jdbcTemplate.queryForList("SELECT table_name FROM information_schema.tables WHERE table_schema=?", databaseName);
        return results.stream()
                .map(result -> result.get("TABLE_NAME").toString())
                .collect(Collectors.toList());
    }


    /**
     * 初始化 获取项目路径 和 类目路径
     *
     * @param path 项目根路径
     */
    private void init(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (null == files) {
            throw new RuntimeException("路径有误");
        }
        for (File f : files) {
            if (f.isFile() && f.getName().matches(".*Application.java")) {
                String applicationPath = f.getPath().replace("\\", "/");

                projectPath = applicationPath.substring(0, applicationPath.lastIndexOf("/") + 1);
                classPath = projectPath.replace(System.getProperty("user.dir").replace("\\", "/") + "/src/main/java/", "")
                        .replace("/", ".");
                // com.example
                classPath = classPath.substring(0, classPath.length() - 1);
                break;
            }
            if (f.isDirectory()) {
                init(f.getAbsolutePath());
            }
        }

        if (StringUtils.isEmpty(projectPath)) {
            throw new RuntimeException("未找到Application启动类");
        }
    }

    /**
     * 下划线转驼峰命名
     */
    private String underlineToCamel(String underline, boolean isTypeName) {
        // 校验
        if (StringUtils.isBlank(underline)) {
            return "";
        }
        // 只有一个字符的情况
        if (underline.length() == 1) {
            return underline.toUpperCase();
        }
        // 转换字符
        String firstLetter = String.valueOf(isTypeName ? Character.toUpperCase(underline.charAt(0)) : underline.charAt(0));
        StringBuilder sb = new StringBuilder(firstLetter);
        for (int i = 1; i < underline.length(); i++) {
            if (!Character.isLetter(underline.charAt(i))) {
                 // 是数字
                if (Character.isDigit(underline.charAt(i))){
                    sb.append(underline.charAt(i));
                }else {
                    sb.append(Character.toUpperCase(underline.charAt(++i)));
                }
                continue;
            }
            sb.append(underline.charAt(i));
        }
        return sb.toString();
    }

    /**
     * 字符串首字母大小写切换
     *
     * @param str   待修改字符串
     * @param upper 是否首字母大写
     * @return 修改后的字符串
     */
    private String firstLetterUpperAndLower(String str, boolean upper) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        // 一个字符
        if (str.length() == 1) {
            if (upper) {
                return str.toUpperCase();
            }
            return str.toLowerCase();
        }
        // 多个字符
        if (upper) {
            return Character.toUpperCase(str.charAt(0)) + str.substring(1);
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }


}
