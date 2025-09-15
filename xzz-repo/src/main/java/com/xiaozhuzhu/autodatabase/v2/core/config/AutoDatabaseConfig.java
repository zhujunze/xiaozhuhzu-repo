package com.xiaozhuzhu.autodatabase.v2.core.config;

import lombok.*;


/**
 * @author xiaozhuzhu
 * @version 1.0
 * @Date 2025年01月15日11:31
 * @description: 自动生成数据库配置
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutoDatabaseConfig {


    /**
     * 主机IP地址
     */
    private String host;

    /**
     * 端口号
     */
    private String port;

    /**
     * 数据库账号
     */
    private String databaseAccount;

    /**
     * 数据库密码
     */
    private String databasePassword;

    /**
     * 数据库类型
     */
    private String databaseType;


    /**
     * 文档读取路劲
     */
    private String documentPath;

    /**
     * 索引前缀
     */
    private String indexPrefix;


}
