package com.migrametric.vo.ladder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 阶梯匹配结果VO（统一返回格式）
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "阶梯匹配结果")
public class LadderMatchResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 阶梯ID
     */
    @Schema(description = "阶梯ID")
    private Long ladderId;

    /**
     * 阶梯名称
     */
    @Schema(description = "阶梯名称")
    private String ladderName;

    /**
     * 阶梯系数
     */
    @Schema(description = "阶梯系数")
    private BigDecimal weight;

    /**
     * 阶梯类型：DATA_VOLUME - 数据量阶梯，USER_COUNT - 用户数阶梯
     */
    @Schema(description = "阶梯类型")
    private String ladderType;

    /**
     * 输入值（匹配时使用的数据量或用户数）
     */
    @Schema(description = "输入值")
    private String inputValue;

    /**
     * 匹配范围文本
     */
    @Schema(description = "匹配范围文本")
    private String rangeText;
}
