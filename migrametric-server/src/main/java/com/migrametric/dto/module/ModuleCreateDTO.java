package com.migrametric.dto.module;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 模块创建DTO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "模块创建DTO")
public class ModuleCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模块名称
     */
    @NotBlank(message = "模块名称不能为空")
    @Size(max = 100, message = "模块名称长度不能超过100个字符")
    @Schema(description = "模块名称")
    private String moduleName;

    /**
     * 所属系统ID
     */
    @NotNull(message = "所属系统不能为空")
    @Schema(description = "所属系统ID")
    private Long systemId;

    /**
     * 模块分类
     */
    @Size(max = 50, message = "模块分类长度不能超过50个字符")
    @Schema(description = "模块分类")
    private String category;

    /**
     * 基础工作量（人天）
     */
    @NotNull(message = "基础工作量不能为空")
    @DecimalMin(value = "0", message = "基础工作量不能为负数")
    @Digits(integer = 8, fraction = 2, message = "基础工作量格式不正确")
    @Schema(description = "基础工作量（人天）")
    private BigDecimal baseWorkload;

    /**
     * 默认加权系数
     */
    @NotNull(message = "默认加权系数不能为空")
    @DecimalMin(value = "0", message = "加权系数不能为负数")
    @DecimalMax(value = "999.99", message = "加权系数不能超过999.99")
    @Digits(integer = 3, fraction = 2, message = "加权系数格式不正确")
    @Schema(description = "默认加权系数")
    private BigDecimal defaultWeight;

    /**
     * 模块描述
     */
    @Size(max = 500, message = "模块描述长度不能超过500个字符")
    @Schema(description = "模块描述")
    private String description;
}
