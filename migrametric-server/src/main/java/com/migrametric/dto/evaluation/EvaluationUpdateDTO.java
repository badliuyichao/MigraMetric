package com.migrametric.dto.evaluation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 评估指标更新DTO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "评估指标更新DTO")
public class EvaluationUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据库表数量（个）
     */
    @NotNull(message = "数据库表数量不能为空")
    @Min(value = 0, message = "数据库表数量不能为负数")
    @Schema(description = "数据库表数量")
    private Integer tableCount;

    /**
     * 数据量（万条）
     */
    @NotNull(message = "数据量不能为空")
    @DecimalMin(value = "0", message = "数据量不能为负数")
    @Schema(description = "数据量（万条）")
    private BigDecimal dataVolume;

    /**
     * 数据量阶梯ID
     */
    @NotNull(message = "数据量阶梯ID不能为空")
    @Schema(description = "数据量阶梯ID")
    private Long dataVolumeLadderId;

    /**
     * 用户数量（人）
     */
    @NotNull(message = "用户数量不能为空")
    @Min(value = 0, message = "用户数量不能为负数")
    @Schema(description = "用户数量（人）")
    private Integer userCount;

    /**
     * 用户数阶梯ID
     */
    @NotNull(message = "用户数阶梯ID不能为空")
    @Schema(description = "用户数阶梯ID")
    private Long userCountLadderId;

    /**
     * 报表数量（个）
     */
    @NotNull(message = "报表数量不能为空")
    @Min(value = 0, message = "报表数量不能为负数")
    @Schema(description = "报表数量")
    private Integer reportCount;

    /**
     * 是否有客开
     */
    @NotNull(message = "是否有客开不能为空")
    @Schema(description = "是否有客开")
    private Boolean hasCustomDev;

    /**
     * 客开模块数量（个）
     */
    @Min(value = 0, message = "客开模块数量不能为负数")
    @Schema(description = "客开模块数量")
    private Integer customDevCount;

    /**
     * 客开评估人天
     */
    @DecimalMin(value = "0", message = "客开人天不能为负数")
    @Schema(description = "客开评估人天")
    private BigDecimal customDevWorkload;

    /**
     * 数据清洗需求描述
     */
    @Size(max = 2000, message = "数据清洗描述不能超过2000个字符")
    @Schema(description = "数据清洗需求描述")
    private String dataCleanDesc;

    /**
     * 数据清洗复杂度：1-简单，2-中等，3-复杂
     */
    @Min(value = 1, message = "数据清洗复杂度值无效")
    @Max(value = 3, message = "数据清洗复杂度值无效")
    @Schema(description = "数据清洗复杂度：1-简单，2-中等，3-复杂")
    private Integer dataCleanComplexity;
}
