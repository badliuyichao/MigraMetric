package com.migrametric.vo.evaluation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 工作量计算结果VO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "工作量计算结果VO")
public class WorkloadResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 核心迁移工作量（人天）
     */
    @Schema(description = "核心迁移工作量（人天）")
    private BigDecimal coreWorkload;

    /**
     * 报表迁移工作量（人天）
     */
    @Schema(description = "报表迁移工作量（人天）")
    private BigDecimal reportWorkload;

    /**
     * 客开工作量（人天）
     */
    @Schema(description = "客开工作量（人天）")
    private BigDecimal customDevWorkload;

    /**
     * 总工作量（人天）
     */
    @Schema(description = "总工作量（人天）")
    private BigDecimal totalWorkload;

    /**
     * 模块工作量明细列表
     */
    @Schema(description = "模块工作量明细列表")
    private List<ModuleWorkloadVO> moduleWorkloads;

    /**
     * 数据量阶梯系数
     */
    @Schema(description = "数据量阶梯系数")
    private BigDecimal dataVolumeWeight;

    /**
     * 用户数阶梯系数
     */
    @Schema(description = "用户数阶梯系数")
    private BigDecimal userCountWeight;

    /**
     * 报表系数
     */
    @Schema(description = "报表系数")
    private BigDecimal reportCoefficient;
}
