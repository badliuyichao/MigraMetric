package com.migrametric.vo.project;

import com.migrametric.vo.evaluation.EvaluationVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 项目详情VO（包含评估概况）
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "项目详情VO")
public class ProjectDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==================== 项目基本信息 ====================

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
    private String evaluationDate;

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
    private String createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private String updateTime;

    // ==================== 评估概况 ====================

    /**
     * 是否有评估记录
     */
    @Schema(description = "是否有评估记录")
    private Boolean hasEvaluation;

    /**
     * 已选模块数量
     */
    @Schema(description = "已选模块数量")
    private Integer selectedModuleCount;

    /**
     * 总工作量（人天）
     */
    @Schema(description = "总工作量")
    private BigDecimal totalWorkload;

    /**
     * 核心迁移工作量
     */
    @Schema(description = "核心迁移工作量")
    private BigDecimal coreWorkload;

    /**
     * 报表迁移工作量
     */
    @Schema(description = "报表迁移工作量")
    private BigDecimal reportWorkload;

    /**
     * 客开工作量
     */
    @Schema(description = "客开工作量")
    private BigDecimal customDevWorkload;

    /**
     * 数据量（万条）
     */
    @Schema(description = "数据量")
    private BigDecimal dataVolume;

    /**
     * 用户数量
     */
    @Schema(description = "用户数量")
    private Integer userCount;

    /**
     * 报表数量
     */
    @Schema(description = "报表数量")
    private Integer reportCount;

    /**
     * 数据库表数量
     */
    @Schema(description = "数据库表数量")
    private Integer tableCount;

    /**
     * 评估状态
     */
    @Schema(description = "评估状态")
    private String evaluationStatus;

    /**
     * 评估状态文本
     */
    @Schema(description = "评估状态文本")
    private String evaluationStatusText;

    /**
     * 评估完成时间
     */
    @Schema(description = "评估完成时间")
    private String evaluationTime;
}
