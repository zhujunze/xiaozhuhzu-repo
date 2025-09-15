package com.xiaozhuzhu.autodatabase.v2.core.strategy.field;


import com.xiaozhuzhu.autodatabase.v2.core.enums.field.FieldTypeEnum;
import com.xiaozhuzhu.autodatabase.v2.core.strategy.field.impl.*;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日17:38
 * @description: 字段类型解析器工厂
 */
public class FieldTypePaserFactory {

    private static final BitFieldTypeParser bitFieldTypeParser = new BitFieldTypeParser();
    private static final TinyintFieldTypeParser tinyintFieldTypeParser = new TinyintFieldTypeParser();
    private static final IntFieldTypeParser intFieldTypeParser = new IntFieldTypeParser();
    private static final BigintFieldTypeParser bigintFieldTypeParser = new BigintFieldTypeParser();
    private static final DecimalFieldTypeParser decimalFieldTypeParser = new DecimalFieldTypeParser();
    private static final CharFieldTypeParser charFieldTypeParser = new CharFieldTypeParser();
    private static final VarcharFieldTypeParser varcharFieldTypeParser = new VarcharFieldTypeParser();
    private static final TinytextFieldTypeParser tinytextFieldTypeParser = new TinytextFieldTypeParser();
    private static final TextFieldTypeParser textFieldTypeParser = new TextFieldTypeParser();
    private static final LongtextFieldTypeParser longtextFieldTypeParser = new LongtextFieldTypeParser();
    private static final DateFieldTypeParser dateFieldTypeParser = new DateFieldTypeParser();
    private static final DatetimeFieldTypeParser datetimeFieldTypeParser = new DatetimeFieldTypeParser();
    private static final JsonFieldTypeParser jsonFieldTypeParser = new JsonFieldTypeParser();

    /**
     * 根据文档类型创建解析器
     *
     * @param fieldTypeEnum 字段类型枚举
     * @return 字段类型解析器
     */
    public static FieldTypePaser build(FieldTypeEnum fieldTypeEnum) {
        FieldTypePaser fieldTypePaser;
        switch (fieldTypeEnum) {
            case BIT:
                fieldTypePaser = bitFieldTypeParser;
                break;
            case TINYINT:
                fieldTypePaser = tinyintFieldTypeParser;
                break;
            case INT:
                fieldTypePaser = intFieldTypeParser;
                break;
            case BIGINT:
                fieldTypePaser = bigintFieldTypeParser;
                break;
            case DECIMAL:
                fieldTypePaser = decimalFieldTypeParser;
                break;
            case CHAR:
                fieldTypePaser = charFieldTypeParser;
                break;
            case VARCHAR:
                fieldTypePaser = varcharFieldTypeParser;
                break;
            case TINYTEXT:
                fieldTypePaser = tinytextFieldTypeParser;
                break;
            case TEXT:
                fieldTypePaser = textFieldTypeParser;
                break;
            case LONGTEXT:
                fieldTypePaser = longtextFieldTypeParser;
                break;
            case JSON:
                fieldTypePaser = jsonFieldTypeParser;
                break;
            case DATE:
                fieldTypePaser = dateFieldTypeParser;
                break;
            case DATETIME:
                fieldTypePaser = datetimeFieldTypeParser;
                break;
            default:
                throw new UnsupportedOperationException("无法解析的字段类型");
        }
        return fieldTypePaser;
    }
}
