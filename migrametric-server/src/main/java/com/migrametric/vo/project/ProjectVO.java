package com.migrametric.vo.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目VO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "项目VO")
public class ProjectVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long id;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称")
    private String projectName;

    /**
     * 客户名称
     */
    @Schema(description = "客户名称")
    private String customerName;

    /**
     * 源系统ID
     */
    @Schema(description = "源系统ID")
    private Long sourceSystemId;

    /**
     * 源系统名称
     */
    @Schema(description = "源系统名称")
    private String sourceSystemName;

    /**
     * 目标系统ID
     */
    @Schema(description = "目标系统ID")
    private Long targetSystemId;

    /**
     * 目标系统名称
     */
    @Schema(description = "目标系统名称")
    private String targetSystemName;

    /**
     * 项目负责人
     */
    @Schema(description = "项目负责人")
    private String projectLeader;

    /**
     * 联系方式
     */
    @Schema(description = "联系方式")
    private String contact;

    /**
     * 项目描述
     */
    @Schema(description = "项目描述")
    private String description;

    /**
     * 评估日期
     */
    @Schema(description = "评估日期")
    private LocalDate evaluationDate;

    /**
     * 项目状态
     */
    @Schema(description = "项目状态")
    private String status;

    /**
     * 状态文本
     */
    @Schema(description = "状态文本")
    private String statusText;

    /**
     * 创建用户ID
     */
    @Schema(description = "创建用户ID")
    private Long userId;

    /**
     * 创建人名称
     */
    @Schema(description = "创建人名称")
    private String createByName;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
