package com.migrametric.vo.log;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志VO
 *
 * @author MigraMetric Team
 */
@Data
public class OperationLogVO {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 请求方式
     */
    private String httpMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 操作IP地址
     */
    private String ipAddress;

    /**
     * 操作地点
     */
    private String location;

    /**
     * 操作状态：0-失败，1-成功
     */
    private Integer status;

    /**
     * 执行时长（毫秒）
     */
    private Long executionTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
