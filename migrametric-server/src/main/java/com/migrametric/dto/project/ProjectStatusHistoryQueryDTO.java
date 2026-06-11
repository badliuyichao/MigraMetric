package com.migrametric.dto.project;

import lombok.Data;

/**
 * 项目状态历史查询 DTO（REQ-§3.2.4）
 */
@Data
public class ProjectStatusHistoryQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String operator;
    private String event;
}
