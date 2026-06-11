package com.migrametric.vo.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目状态历史 VO（REQ-§3.2.4）
 */
@Data
@Schema(description = "项目状态历史VO")
public class ProjectStatusHistoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "历史记录ID")
    private Long id;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "变更前状态")
    private String fromStatus;

    @Schema(description = "变更后状态")
    private String toStatus;

    @Schema(description = "事件")
    private String event;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "业务备注")
    private String reason;

    @Schema(description = "变更时间")
    private LocalDateTime changeTime;

    @Schema(description = "是否人工补录")
    private Boolean manualEdit;
}
