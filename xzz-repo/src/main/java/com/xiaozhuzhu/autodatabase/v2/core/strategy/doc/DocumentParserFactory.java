package com.xiaozhuzhu.autodatabase.v2.core.strategy.doc;

import com.xiaozhuzhu.autodatabase.v2.core.enums.document.DocumentEnum;
import com.xiaozhuzhu.autodatabase.v2.core.strategy.doc.impl.DocxParser;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日14:24
 * @description: 文档解析器工厂
 */
public class DocumentParserFactory {

    private static final DocxParser docxParser = new DocxParser();

    /**
     * 根据文档类型创建解析器
     *
     * @param documentEnum 文档类型枚举
     * @return 文档解析器
     */
    public static DocumentParser build(DocumentEnum documentEnum) {
        DocumentParser documentParser;
        switch (documentEnum) {
            case DOCX:
                documentParser = docxParser;
                break;
            default:
                throw new UnsupportedOperationException("未找到该文档类型对应的解析器");
        }
        return documentParser;
    }
}
