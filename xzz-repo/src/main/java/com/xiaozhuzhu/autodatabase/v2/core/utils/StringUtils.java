package com.xiaozhuzhu.autodatabase.v2.core.utils;

/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日15:36
 * @description: 字符串工具类
 */
public class StringUtils {

    /**
     * 中文冒号格式化
     *
     * @param str 需要格式化的字符串
     * @return 格式化后的字符串
     */
    public static String colonFormat(String str) {
        return str.replace("：", ":").trim();
    }

    /**
     * 中文逗号格式化
     *
     * @param str 需要格式化的字符串
     * @return 格式化后的字符串
     */
    public static String commaFormat(String str) {
        return str.replace("，", ",").trim();
    }

    /**
     * 中文括号格式化
     *
     * @param str 需要格式化的字符串
     * @return 格式化后的字符串
     */
    public static String bracketFormat(String str) {
        return str.replace("（", "(")
                .replace("）", ")").trim();
    }

    /**
     * 截取 第一个冒号之后的字符串
     *
     * @param str 需要处理的字符串
     * @return 处理后的字符创
     */
    public static String colonSubstring(String str) {
        int index = str.indexOf(":");
        return str.substring(index + 1).trim();
    }

    /**
     * 截取字符串通过指定前后缀
     *
     * @param str    需要处理的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 截取后的字符串
     */
    public static String substring(String str, String prefix, String suffix) {
        int startIndex = str.indexOf(prefix) + 1;
        int endIndex = str.indexOf(suffix);
        return str.substring(startIndex, endIndex).trim();
    }

    /**
     * 是否存在 '('
     *
     * @param str 需要处理的字符串
     * @return true 存在 false 不存在
     */
    public static boolean isExistLeftBracket(String str) {
        // 先格式化
        str = bracketFormat(str);
        return str.contains("(");
    }

    /**
     * 判断是否是空字符串
     *
     * @param str 需要处理的字符串
     * @return 判断结果
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }


    /**
     * 判断是否不是空字符串
     *
     * @param str 需要处理的字符串
     * @return 判断结果
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    /**
     * 计数某个字符出现的评率
     *
     * @param str    需要处理的字符串
     * @param target 需要计数的字符
     * @return 结束结果
     */
    public static int countChar(String str, char target) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == target) {
                count++;
            }
        }
        return count;
    }
}
