package com.migrametric.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 人工补录状态历史 DTO（仅 ADMIN，REQ-§3.2.4）
 */
@Data
public class ProjectStatusHistoryCreateDTO {

    @NotBlank(message = "fromStatus 不能为空")
    private String fromStatus;

    @NotBlank(message = "toStatus 不能为空")
    private String toStatus;

    /**
     * 强制 MANUAL_EDIT，service 层会校验
     */
    @NotBlank(message = "event 不能为空")
    private String event;

    private String reason;

    @NotNull(message = "changeTime 不能为空")
    private LocalDateTime changeTime;
}
