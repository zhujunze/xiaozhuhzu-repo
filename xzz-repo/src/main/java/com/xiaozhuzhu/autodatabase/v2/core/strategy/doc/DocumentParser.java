package com.xiaozhuzhu.autodatabase.v2.core.strategy.doc;

import com.xiaozhuzhu.autodatabase.v2.core.entity.DatabaseDefinition;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日14:22
 * @description: 文档解析器接口
 */
public interface DocumentParser {


    /**
     * 解析文档
     *
     * @param document 文档对象
     * @param database 数据库对象
     */
    void parse(XWPFDocument document, DatabaseDefinition database) throws IOException;

    /**
     * 解析数据库名字
     *
     * @param document 文档对象
     * @return 数据库名字
     */
    String parseDatabaseName(XWPFDocument document) throws IOException;
}
