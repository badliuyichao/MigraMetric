package com.migrametric.dto.evaluation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 模块配置保存DTO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "模块配置保存DTO")
public class ModuleConfigDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模块ID
     */
    @NotNull(message = "模块ID不能为空")
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
     * 调整后的加权系数
     */
    @NotNull(message = "加权系数不能为空")
    @DecimalMin(value = "0.1", message = "加权系数最小为0.1")
    @DecimalMax(value = "10.0", message = "加权系数最大为10.0")
    @Schema(description = "加权系数")
    private BigDecimal weight;

    /**
     * 是否选中
     */
    @Schema(description = "是否选中")
    private Boolean checked;
}
