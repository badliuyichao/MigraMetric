package com.migrametric.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 项目更新DTO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "项目更新DTO")
public class ProjectUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目名称
     */
    @Size(max = 200, message = "项目名称不能超过200个字符")
    @Schema(description = "项目名称")
    private String projectName;

    /**
     * 客户名称
     */
    @Size(max = 200, message = "客户名称不能超过200个字符")
    @Schema(description = "客户名称")
    private String customerName;

    /**
     * 源系统ID
     */
    @NotNull(message = "源系统不能为空")
    @Schema(description = "源系统ID")
    private Long sourceSystemId;

    /**
     * 目标系统ID
     */
    @NotNull(message = "目标系统不能为空")
    @Schema(description = "目标系统ID")
    private Long targetSystemId;

    /**
     * 项目负责人
     */
    @Size(max = 100, message = "负责人名称不能超过100个字符")
    @Schema(description = "项目负责人")
    private String projectLeader;

    /**
     * 联系方式
     */
    @Size(max = 100, message = "联系方式不能超过100个字符")
    @Schema(description = "联系方式")
    private String contact;

    /**
     * 项目描述
     */
    @Size(max = 1000, message = "描述不能超过1000个字符")
    @Schema(description = "项目描述")
    private String description;

    /**
     * 评估日期
     */
    @Schema(description = "评估日期")
    private LocalDate evaluationDate;
}
