package com.migrametric.dto.log;

import lombok.Data;

/**
 * 登录日志查询DTO
 *
 * @author MigraMetric Team
 */
@Data
public class LoginLogQueryDTO {

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录状态：success-成功，fail-失败
     */
    private String status;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}
