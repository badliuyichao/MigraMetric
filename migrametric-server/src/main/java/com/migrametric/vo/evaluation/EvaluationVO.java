package com.migrametric.vo.evaluation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评估VO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "评估VO")
public class EvaluationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评估ID
     */
    @Schema(description = "评估ID")
    private Long id;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 数据库表数量
     */
    @Schema(description = "数据库表数量")
    private Integer tableCount;

    /**
     * 数据量（万条）
     */
    @Schema(description = "数据量（万条）")
    private BigDecimal dataVolume;

    /**
     * 数据量阶梯名称
     */
    @Schema(description = "数据量阶梯名称")
    private String dataVolumeLadderName;

    /**
     * 用户数量
     */
    @Schema(description = "用户数量")
    private Integer userCount;

    /**
     * 用户数阶梯名称
     */
    @Schema(description = "用户数阶梯名称")
    private String userCountLadderName;

    /**
     * 报表数量
     */
    @Schema(description = "报表数量")
    private Integer reportCount;

    /**
     * 是否有客开
     */
    @Schema(description = "是否有客开")
    private Boolean hasCustomDev;

    /**
     * 客开模块数量
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
     * 数据清洗复杂度文本
     */
    @Schema(description = "数据清洗复杂度文本")
    private String dataCleanComplexityText;

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
     * 总工作量
     */
    @Schema(description = "总工作量")
    private BigDecimal totalWorkload;

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
}
