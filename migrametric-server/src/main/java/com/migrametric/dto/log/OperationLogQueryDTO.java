package com.migrametric.dto.log;

import lombok.Data;

/**
 * 操作日志查询DTO
 *
 * @author MigraMetric Team
 */
@Data
public class OperationLogQueryDTO {

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 操作状态：0-失败，1-成功
     */
    private Integer status;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}
