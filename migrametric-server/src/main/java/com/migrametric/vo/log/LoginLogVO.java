package com.migrametric.vo.log;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志VO
 *
 * @author MigraMetric Team
 */
@Data
public class LoginLogVO {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录IP地址
     */
    private String ipAddress;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 登录状态：success-成功，fail-失败
     */
    private String status;

    /**
     * 提示消息
     */
    private String msg;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
}
