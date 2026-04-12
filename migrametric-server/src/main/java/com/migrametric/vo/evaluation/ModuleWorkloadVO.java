package com.migrametric.vo.evaluation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 模块工作量明细VO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "模块工作量明细VO")
public class ModuleWorkloadVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模块ID
     */
    @Schema(description = "模块ID")
    private Long moduleId;

    /**
     * 模块名称
     */
    @Schema(description = "模块名称")
    private String moduleName;

    /**
     * 模块分类
     */
    @Schema(description = "模块分类")
    private String category;

    /**
     * 基础人天
     */
    @Schema(description = "基础人天")
    private BigDecimal baseWorkload;

    /**
     * 加权系数
     */
    @Schema(description = "加权系数")
    private BigDecimal weight;

    /**
     * 数据量系数
     */
    @Schema(description = "数据量系数")
    private BigDecimal dataVolumeWeight;

    /**
     * 用户数系数
     */
    @Schema(description = "用户数系数")
    private BigDecimal userCountWeight;

    /**
     * 模块工作量（人天）
     */
    @Schema(description = "模块工作量（人天）")
    private BigDecimal moduleWorkload;
}
