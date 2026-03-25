package com.migrametric.vo.evaluation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 模块配置VO（包含模块信息）
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "模块配置VO")
public class ModuleConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @Schema(description = "配置ID")
    private Long id;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

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
     * 基础工作量（人天）
     */
    @Schema(description = "基础工作量（人天）")
    private BigDecimal baseWorkload;

    /**
     * 默认加权系数
     */
    @Schema(description = "默认加权系数")
    private BigDecimal defaultWeight;

    /**
     * 当前加权系数
     */
    @Schema(description = "加权系数")
    private BigDecimal weight;

    /**
     * 是否选中
     */
    @Schema(description = "是否选中")
    private Boolean checked;

    /**
     * 模块工作量（人天）
     */
    @Schema(description = "模块工作量")
    private BigDecimal moduleWorkload;
}
