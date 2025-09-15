package com.xiaozhuzhu.autodatabase.v2.core.strategy.doc.impl;

import com.xiaozhuzhu.autodatabase.v2.core.entity.*;
import com.xiaozhuzhu.autodatabase.v2.core.enums.field.FieldAttributeEnum;
import com.xiaozhuzhu.autodatabase.v2.core.enums.field.FieldTypeEnum;
import com.xiaozhuzhu.autodatabase.v2.core.enums.index.IndexMethodEnum;
import com.xiaozhuzhu.autodatabase.v2.core.enums.index.IndexTypeEnum;
import com.xiaozhuzhu.autodatabase.v2.core.strategy.doc.DocumentParser;
import com.xiaozhuzhu.autodatabase.v2.core.strategy.field.FieldTypePaserFactory;
import com.xiaozhuzhu.autodatabase.v2.core.utils.StringUtils;
import org.apache.poi.xwpf.usermodel.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日14:25
 * @description: .docx 文档解析器
 */
public class DocxParser implements DocumentParser {


    @Override
    public void parse(XWPFDocument document, DatabaseDefinition database) throws IOException {


//        // (1) 获取数据库名字
//        String databaseName = getDatabaseName(document);
//        // 设置数据库名字
//        database.setDatabaseName(databaseName);

        // (2) 解析表格
        // 获取文件中的表格
        Iterator<XWPFTable> documentTables = document.getTablesIterator();
        // 解析表格
        while (documentTables.hasNext()) {
            // 获取表格
            XWPFTable xwpfTable = documentTables.next();
            // 解析表格
            parseTable(xwpfTable, database);
        }
    }

    @Override
    public String parseDatabaseName(XWPFDocument document) throws IOException {
        //  获取数据库名字
        return getDatabaseName(document);
    }

    /**
     * 解析表格，并描述表
     *
     * @param docTable 带解析的表格
     * @param database 数据库对象
     */
    private void parseTable(XWPFTable docTable, DatabaseDefinition database) {
        // 获取行列表
        List<XWPFTableRow> rows = docTable.getRows();

        // 表格的第一行为表名
        XWPFTableRow firstRow = rows.get(0);
        // 获取表构造者
        TableDefinition table = TableDefinition.builder().build();
        // 解析表名和表名注释
        paseTableNameAndComment(firstRow, table);

        // 解析字段 表格第二行为含义解释，因此跳过直接从第三行开始即行索引为2
        // 遍历表格行 解析字段
        for (int i = 2; i < rows.size(); i++) {
            // 每一行为一个字段的描述
            XWPFTableRow tableRow = rows.get(i);

            // 解析字段
            FieldDefinition field = parseField(tableRow, table);

            // 设置上一个字段
            if (i > 2) {
                String pre = rows.get(i - 1)
                        .getTableCells()
                        .get(0)
                        .getText().trim();
                field.setPre(pre);
            }
            if (i < rows.size() - 1) {
                String next = rows.get(i + 1)
                        .getTableCells()
                        .get(0)
                        .getText().trim();
                field.setNext(next);
            }

            // 获取数据库的表列表
            List<FieldDefinition> fieldList = table.getField();
            if (null == fieldList || fieldList.isEmpty()) {
                fieldList = new ArrayList<>();
                table.setField(fieldList);
            }
            fieldList.add(field);
        }

        // 判断是否有设置主键
        if (StringUtils.isEmpty(table.getPrimaryKey())) {
            throw new UnsupportedOperationException("表：" + table.getTableName() + "未设置主键");
        }

        // 解析索引
        parseIndex(table);

        // 获取数据库的表列表
        List<TableDefinition> tableList = database.getTable();
        if (null == tableList || tableList.isEmpty()) {
            tableList = new ArrayList<>();
            database.setTable(tableList);
        }
        tableList.add(table);
    }

    /**
     * 解析索引
     *
     * @param table 表构建对象
     */
    private void parseIndex(TableDefinition table) {

        // 获取所有字段
        List<FieldDefinition> fieldList = table.getField();

        // 映射字段名、字段
        Map<String, FieldDefinition> fieldMap = fieldList.stream()
                .collect(Collectors.toMap(FieldDefinition::getName, field -> field));

        // 遍历
        for (FieldDefinition field : fieldList) {

            // 获取字段属性
            List<FieldAttributeEnum> attributeList = field.getAttribute();

            // 获取字段名
            String fieldName = field.getName();
            // 获取索引字段字符串
            String attributeIndexString = field.getAttributeIndexString();

            // 遍历
            for (FieldAttributeEnum attributeEnum : attributeList) {
                // 设置索引
                setIndex(table, field, attributeEnum, fieldName, attributeIndexString, fieldMap);
            }
        }


    }

    /**
     * 设置索引
     *
     * @param table                表
     * @param field                字段
     * @param attributeEnum        属性枚举
     * @param fieldName            字段名
     * @param attributeIndexString 索引字段字符串
     * @param fieldMap             字段映射
     */
    private void setIndex(TableDefinition table, FieldDefinition field, FieldAttributeEnum attributeEnum, String fieldName, String attributeIndexString, Map<String, FieldDefinition> fieldMap) {
        switch (attributeEnum) {
            case INDEX:
                // 创建索引对象
                IndexDefinition index = IndexDefinition.builder()
                        .indexName(fieldName)
                        // 索引主字段
                        .indexPrimaryField(fieldName)
                        .method(IndexMethodEnum.BTREE)
                        .sortType("ASC")
                        .type(IndexTypeEnum.INDEX)
                        .build();
                // 设置索引字段
                List<FieldDefinition> indexFields = new ArrayList<>();
                indexFields.add(field);
                index.setFields(indexFields);

                // 设置索引
                wrapIndex(table, index);
                break;
            case UNIQUE:
                // 创建索引对象
                IndexDefinition uniqueIndex = IndexDefinition.builder()
                        .indexName(fieldName + "_UNIQUE")
                        // 索引主字段
                        .indexPrimaryField(fieldName)
                        .method(IndexMethodEnum.BTREE)
                        .sortType("ASC")
                        .type(IndexTypeEnum.UNIQUE)
                        .build();
                // 设置索引字段
                List<FieldDefinition> uniqueIndexFields = new ArrayList<>();
                uniqueIndexFields.add(field);
                uniqueIndex.setFields(uniqueIndexFields);

                // 设置索引
                wrapIndex(table, uniqueIndex);
                break;
            case UNION_INDEX:
                // 创建索引对象
                IndexDefinition unionIndex = IndexDefinition.builder()
                        // 索引主字段
                        .indexPrimaryField(fieldName)
                        .method(IndexMethodEnum.BTREE)
                        .sortType("ASC")
                        .type(IndexTypeEnum.UNION_INDEX)
                        .build();

                // 设置索引名

                String indexNameUnion = attributeIndexString.replace(",", "_") + "_UNION";
                unionIndex.setIndexName(indexNameUnion);

                // 设置索引字段
                // 获取索引字段字符串
                List<FieldDefinition> unionIndexFieldList = getIndexFields(attributeIndexString, fieldMap, table, fieldName);
                unionIndex.setFields(unionIndexFieldList);

                // 设置索引
                wrapIndex(table, unionIndex);
                break;
            case UNION_UNIQUE_INDEX:
                // 创建索引对象
                IndexDefinition unionUniqueIndex = IndexDefinition.builder()
                        // 索引主字段
                        .indexPrimaryField(fieldName)
                        .method(IndexMethodEnum.BTREE)
                        .sortType("ASC")
                        .type(IndexTypeEnum.UNION_UNIQUE_INDEX)
                        .build();
                // 设置索引名
                String indexNameUnionUnique = attributeIndexString.replace(",", "_") + "_UNION_UNIQUE";
                unionUniqueIndex.setIndexName(indexNameUnionUnique);

                // 设置索引字段
                // 获取索引字段字符串
                List<FieldDefinition> unionUniqueIndexFieldList = getIndexFields(attributeIndexString, fieldMap, table, fieldName);
                unionUniqueIndex.setFields(unionUniqueIndexFieldList);

                // 设置索引
                wrapIndex(table, unionUniqueIndex);
                break;
            case FULL_TEXT_INDEX:

                // 创建索引对象
                IndexDefinition fulltextIndex = IndexDefinition.builder()
                        // 索引主字段
                        .indexPrimaryField(fieldName)
                        .type(IndexTypeEnum.FULL_TEXT_INDEX)
                        .build();
                // 设置索引名
                String indexNameFulltext = attributeIndexString.replace(",", "_") + "_FULLTEXT";
                fulltextIndex.setIndexName(indexNameFulltext);

                // 设置索引字段
                // 获取索引字段字符串
                List<FieldDefinition> fulltextIndexFieldList = getIndexFields(attributeIndexString, fieldMap, table, fieldName);
                fulltextIndex.setFields(fulltextIndexFieldList);

                // 设置索引
                wrapIndex(table, fulltextIndex);
                break;
        }
    }

    /**
     * 获取索引字段
     *
     * @param attributeIndexString 索引字段字符串
     * @param fieldMap             字段银蛇
     * @param table                表
     * @param fieldName            字段名
     * @return 索引字段
     */
    private List<FieldDefinition> getIndexFields(String attributeIndexString, Map<String, FieldDefinition> fieldMap,
                                                 TableDefinition table, String fieldName) {
        String[] indexFields = attributeIndexString.split(",");
        List<FieldDefinition> indexFieldList = new ArrayList<>();
        for (String indexField : indexFields) {
            FieldDefinition fieldDefinition = fieldMap.get(indexField.trim());
            // 说明索引字段有误
            if (null == fieldDefinition) {
                throw new UnsupportedOperationException("索引字段有误:" + attributeIndexString +
                        "，存在于表：{" + table.getTableName() + "},字段：{" + fieldName + "}");
            }
            indexFieldList.add(fieldDefinition);
        }
        return indexFieldList;
    }

    /**
     * 组装索引
     *
     * @param table 表对象
     * @param index 索引对象
     */
    private void wrapIndex(TableDefinition table, IndexDefinition index) {

        // 设置索引
        List<IndexDefinition> indexList = table.getIndex();
        if (null == indexList || indexList.isEmpty()) {
            indexList = new ArrayList<>();
            table.setIndex(indexList);
        }
        indexList.add(index);
    }

    /**
     * 解析字段
     *
     * @param tableRow 表格的行
     * @param table    表构建对象
     * @return 字段描述
     */
    private FieldDefinition parseField(XWPFTableRow tableRow, TableDefinition table) {
        // 创建字段对象
        FieldDefinition field = FieldDefinition.builder().build();

        // 空单元格计数 大于0说明存在未完善的字段
        int emptyCount = 0;

        // 获取当前行的所有列
        List<XWPFTableCell> cells = tableRow.getTableCells();
        /*
         * (1) 字段名
         * (2) 字段类型
         * (3) 默认值
         * (4) 属性
         * (5) 字段注释
         */
        // 字段名
        String fieldName = cells.get(0).getText().trim();
        // 字段类型描述
        String fieldTypeString = cells.get(1).getText();
        // 默认值
        String defaultValue = cells.get(2).getText();
        // 属性描述
        String attributeString = cells.get(3).getText();
        // 字段注释
        String fieldComment = cells.get(4).getText();

        // 字段名、字段类型、字段注释为必填项
        if (StringUtils.isEmpty(fieldName) || StringUtils.isEmpty(fieldTypeString) || StringUtils.isEmpty(fieldComment)) {
            emptyCount++;
        }
        // 表名
        String tableName = table.getTableName();
        if (emptyCount > 0) {
            throw new UnsupportedOperationException("表：{" + tableName + "}存在未完善的字段");
        }

        // 设置字段名、字段描述
        field.setName(fieldName.trim());
        field.setComment(fieldComment.trim());

        // 解析字段描述、默认值
        fieldTypeString = StringUtils.bracketFormat(fieldTypeString);
        parseFieldDescriptionAndDefaultValue(fieldTypeString, defaultValue, field, tableName);

        // 解析字段属性
        try {
            parseFieldAttribute(attributeString, field, table);
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new UnsupportedOperationException("解析表：{" + tableName + "}失败");
        }

        return field;

    }

    /**
     * 解析字段属性
     *
     * @param attributeString 字段属性字符串
     * @param field           字段描述对象
     * @param table           表描述对象
     */
    private void parseFieldAttribute(String attributeString, FieldDefinition field, TableDefinition table) {

        // 判断括号数量是否超过2个，仅支持一个()一般联合索引、联合唯一索引、全文索引需要
        attributeString = StringUtils.bracketFormat(attributeString);
        attributeString = StringUtils.commaFormat(attributeString);
        attributeString = attributeString.replaceAll("\n", "");
        int leftBracket = StringUtils.countChar(attributeString, '(');
        int rightBracket = StringUtils.countChar(attributeString, ')');
        String fieldName = field.getName();
        // 获取表名
        String tableName = table.getTableName();
        if (leftBracket > 1 || rightBracket > 1) {
            throw new UnsupportedOperationException("表{" + tableName + "},字段{" + fieldName + "}存在无法识别的字段属性：" + attributeString);
        }

        // 先去除所有带有括号的内容 11(22)11,22(33)22(33) ---> 1111,2222
        String attributeStringFormat = attributeString.replaceAll("\\(.*?\\)", "");

        // 遍历
        // 拆分属性字符串
        String[] attributeItem = attributeStringFormat.split("[，,、]");
        List<FieldAttributeEnum> attributeEnumList = new ArrayList<>();
        // 遍历
        for (String item : attributeItem) {
            if (StringUtils.isEmpty(item)) {
                item = FieldAttributeEnum.NULL.getName();
            }

            // 获取对应属性枚举
            FieldAttributeEnum attribute = FieldAttributeEnum.getFieldAttribute(item);
            if (null == attribute) {
                throw new UnsupportedOperationException("表{" + tableName + "},字段{" + fieldName + "}存在无法识别的字段属性：" + item);
            }
            // 是否是主键
            if (attribute == FieldAttributeEnum.PRIMARY_KEY) {
                // 设置主键字段
                table.setPrimaryKey(fieldName);
            }

            // 获取字段类型
            FieldTypeEnum type = field.getType().getTypeEnum();

            // 如果字段是自增的
            if (attribute == FieldAttributeEnum.AUTO_INCREMENT) {
                // 自增只适用于 tinyint、int、bigint
                if (type != FieldTypeEnum.BIGINT && type != FieldTypeEnum.INT && type != FieldTypeEnum.TINYINT) {
                    throw new UnsupportedOperationException("字段属性有误[" + attribute.getName() + "],存在于表：" + tableName + "，字段：" + fieldName);
                }
            }

            attributeEnumList.add(attribute);
        }
        // 设置字段属性
        field.setAttribute(attributeEnumList);

        // 设置索引字段字符串
        String attributeIndexString = null;
        if (attributeEnumList.contains(FieldAttributeEnum.UNION_INDEX) || attributeEnumList.contains(FieldAttributeEnum.UNION_UNIQUE_INDEX)
                || attributeEnumList.contains(FieldAttributeEnum.FULL_TEXT_INDEX)) {
            // 设置字段为索引字段
            field.setIsIndexField(true);
            // 截取索引字符串
            attributeIndexString = StringUtils.substring(attributeString, "(", ")");
            // 判断索引字段是否合法
            String[] indexFields = attributeIndexString.split("[，,、]");
            if (!attributeEnumList.contains(FieldAttributeEnum.FULL_TEXT_INDEX) && indexFields.length <= 1) {
                throw new UnsupportedOperationException("表{" + tableName + "},字段{" + fieldName + "}存在无法识别的字段属性：索引字段有误");
            }
        } else {
            field.setIsIndexField(false);
            if (attributeEnumList.contains(FieldAttributeEnum.INDEX) || attributeEnumList.contains(FieldAttributeEnum.UNIQUE)) {
                // 设置字段为索引字段
                field.setIsIndexField(true);
                attributeIndexString = fieldName;
            }
        }

        // 设置索引字段字符串
        field.setAttributeIndexString(attributeIndexString);
    }


    /**
     * 解析字段描述、默认值
     *
     * @param fieldTypeString 字段描述
     * @param defaultValue    默认值
     * @param field           字段对象
     * @param tableName       表名
     */
    private void parseFieldDescriptionAndDefaultValue(String fieldTypeString, String defaultValue, FieldDefinition field, String tableName) {

        FieldTypeDefinition fieldTypeDefinition = FieldTypeDefinition.builder().build();
        // 设置字段类型 -- 引用传递
        field.setType(fieldTypeDefinition);

        // 字段类型
        String fieldType = fieldTypeString;
        // 字段长度
        String fieldLength = null;
        // 判断是否存在括号，存在括号说明有长度限制
        if (StringUtils.isExistLeftBracket(fieldTypeString)) {
            // 字段类型截取
            fieldType = fieldType.substring(0, fieldType.indexOf("("));

            // 截取字段类型长度字符串
            fieldLength = StringUtils.substring(fieldTypeString, "(", ")");
        }

        // 解析字段类型
        FieldTypeEnum fieldTypeEnum = FieldTypeEnum.getFieldType(fieldType);
        String fieldName = field.getName();
        if (null == fieldTypeEnum) {
            throw new UnsupportedOperationException("表{" + tableName + "},字段{" + fieldName + "}存在无法识别的字段类型：" + fieldType);
        }
        // 设置字段类型
        fieldTypeDefinition.setTypeEnum(fieldTypeEnum);

        // 获取解析器
        FieldTypePaserFactory.build(fieldTypeEnum)
                // 解析字段类型长度、小数位数 并设置
                .parseFieldType(fieldTypeDefinition, fieldTypeEnum, fieldLength, tableName, fieldName)
                // 解析默认值
                .parseDefaultValue(field, fieldTypeEnum, defaultValue, tableName, fieldName);


    }

    /**
     * 解析表名和表名注释
     *
     * @param firstRow 表格第一行
     * @param table    表
     */
    private void paseTableNameAndComment(XWPFTableRow firstRow, TableDefinition table) {
        // 获取表名
        String tableName = firstRow.getCell(0).getText();
        // 格式化中文冒号
        tableName = StringUtils.colonFormat(tableName);
        // 截取冒号后的内容
        tableName = StringUtils.colonSubstring(tableName);

        // 默认表名
        table.setTableName(tableName);

        // 是否存在 '('
        if (StringUtils.isExistLeftBracket(tableName)) {
            // 格式化括号
            tableName = StringUtils.bracketFormat(tableName);
            // 表名
            table.setTableName(tableName.substring(0, tableName.indexOf("(")).trim());
            // 表名注释
            String tableComment = StringUtils.substring(tableName, "(", ")");
            // 去除注释中的换行符号
            tableComment = tableComment.replaceAll("\n\r", " ");
            table.setComment(tableComment);
        }
    }

    /**
     * 获取数据库名字
     *
     * @param document 文档对象
     * @return 数据库名字
     */
    private String getDatabaseName(XWPFDocument document) {
        // 获取段落文字
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        for (XWPFParagraph paragraph : paragraphs) {
            String text = paragraph.getText();
            if (text.contains("数据库名称")) {
                // 格式化中文冒号
                text = StringUtils.colonFormat(text);
                // 截取冒号后的内容
                return StringUtils.colonSubstring(text);
            }
        }
        // 未设置数据库名字
        throw new UnsupportedOperationException("未设置数据库名字");
    }
}