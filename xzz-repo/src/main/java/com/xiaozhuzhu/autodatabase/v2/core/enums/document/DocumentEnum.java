package com.xiaozhuzhu.autodatabase.v2.core.enums.document;

import lombok.Getter;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日14:29
 * @description: 文档枚举
 */
@Getter
public enum DocumentEnum {

    DOCX(".docx"),
    ;

    private final String suffix;

    DocumentEnum(String suffix) {
        this.suffix = suffix;
    }


    /**
     * 获取文档类型
     *
     * @param documentPath 文档全路径
     */
    public static DocumentEnum getDocumentType(String documentPath) {
        // 获取最后一个.的索引
        int index = documentPath.lastIndexOf(".");
        // 截取文档类型后缀
        String suffix = documentPath.substring(index);
        // 匹配枚举
        for (DocumentEnum documentEnum : DocumentEnum.values()) {
            if (documentEnum.suffix.equalsIgnoreCase(suffix)) {
                return documentEnum;
            }
        }
        throw new UnsupportedOperationException("无法解析" + suffix + "文档类型。");
    }
}
