package com.migrametric.dto.ladder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 数据量阶梯更新DTO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "数据量阶梯更新DTO")
public class DataVolumeLadderUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 阶梯ID
     */
    @NotNull(message = "阶梯ID不能为空")
    @Schema(description = "阶梯ID")
    private Long id;

    /**
     * 阶梯名称
     */
    @NotBlank(message = "阶梯名称不能为空")
    @Size(max = 50, message = "阶梯名称长度不能超过50个字符")
    @Schema(description = "阶梯名称")
    private String ladderName;

    /**
     * 数据量下限（万条）
     */
    @NotNull(message = "数据量下限不能为空")
    @DecimalMin(value = "0", message = "数据量下限不能为负数")
    @Digits(integer = 13, fraction = 2, message = "数据量下限格式不正确")
    @Schema(description = "数据量下限（万条）")
    private BigDecimal minVolume;

    /**
     * 数据量上限（万条），NULL表示无上限
     */
    @DecimalMin(value = "0", message = "数据量上限不能为负数")
    @Digits(integer = 13, fraction = 2, message = "数据量上限格式不正确")
    @Schema(description = "数据量上限（万条），NULL表示无上限")
    private BigDecimal maxVolume;

    /**
     * 对应工作量系数
     */
    @NotNull(message = "工作量系数不能为空")
    @DecimalMin(value = "0", message = "工作量系数不能为负数")
    @DecimalMax(value = "99.99", message = "工作量系数不能超过99.99")
    @Digits(integer = 2, fraction = 2, message = "工作量系数格式不正确")
    @Schema(description = "对应工作量系数")
    private BigDecimal weight;

    /**
     * 排序顺序
     */
    @NotNull(message = "排序顺序不能为空")
    @Min(value = 0, message = "排序顺序不能小于0")
    @Schema(description = "排序顺序")
    private Integer sortOrder;
}
