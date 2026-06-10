package com.migrametric.entity.evaluation;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评估实体
 *
 * @author MigraMetric Team
 */
@Data
@TableName("eval_evaluation")
@Schema(description = "评估实体")
public class Evaluation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评估ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "评估ID")
    private Long id;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 数据库表数量（个）
     */
    @Schema(description = "数据库表数量")
    private Integer tableCount;

    /**
     * 数据量（万条）
     */
    @Schema(description = "数据量（万条）")
    private BigDecimal dataVolume;

    /**
     * 数据量阶梯ID
     */
    @Schema(description = "数据量阶梯ID")
    private Long dataVolumeLadderId;

    /**
     * 用户数量（人）
     */
    @Schema(description = "用户数量")
    private Integer userCount;

    /**
     * 用户数阶梯ID
     */
    @Schema(description = "用户数阶梯ID")
    private Long userCountLadderId;

    /**
     * 报表数量（个）
     */
    @Schema(description = "报表数量")
    private Integer reportCount;

    /**
     * 是否有客开：0-否，1-是
     */
    @Schema(description = "是否有客开")
    private Integer hasCustomDev;

    /**
     * 客开模块数量（个）
     */
    @Schema(description = "客开模块数量")
    private Integer customDevCount;

    /**
     * 客开评估人天
     */
    @Schema(description = "客开评估人天")
    private BigDecimal customDevWorkload;

    /**
     * 数据清洗需求描述
     */
    @Schema(description = "数据清洗需求描述")
    private String dataCleanDesc;

    /**
     * 数据清洗复杂度：1-简单，2-中等，3-复杂
     */
    @Schema(description = "数据清洗复杂度")
    private Integer dataCleanComplexity;

    /**
     * 核心迁移工作量（人天）
     */
    @Schema(description = "核心迁移工作量")
    private BigDecimal coreWorkload;

    /**
     * 报表迁移工作量（人天）
     */
    @Schema(description = "报表迁移工作量")
    private BigDecimal reportWorkload;

    /**
     * 总工作量（人天）
     */
    @Schema(description = "总工作量")
    private BigDecimal totalWorkload;

    /**
     * 评估状态：DRAFT-草稿，IN_PROGRESS-进行中，COMPLETED-已完成
     */
    @Schema(description = "评估状态")
    private String evaluationStatus;

    /**
     * 评估完成时间
     */
    @Schema(description = "评估完成时间")
    private LocalDateTime evaluationTime;

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

    /**
     * 创建者
     */
    @Schema(description = "创建者")
    private String createBy;

    /**
     * 更新者
     */
    @Schema(description = "更新者")
    private String updateBy;
}
