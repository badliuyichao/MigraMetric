package com.migrametric.vo.ladder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 数据量阶梯VO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "数据量阶梯VO")
public class DataVolumeLadderVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 阶梯ID
     */
    @Schema(description = "阶梯ID")
    private Long id;

    /**
     * 阶梯名称
     */
    @Schema(description = "阶梯名称")
    private String ladderName;

    /**
     * 数据量下限（万条）
     */
    @Schema(description = "数据量下限（万条）")
    private BigDecimal minVolume;

    /**
     * 数据量上限（万条）
     */
    @Schema(description = "数据量上限（万条）")
    private BigDecimal maxVolume;

    /**
     * 数据量范围描述
     */
    @Schema(description = "数据量范围描述")
    private String volumeRangeText;

    /**
     * 对应工作量系数
     */
    @Schema(description = "对应工作量系数")
    private BigDecimal weight;

    /**
     * 排序顺序
     */
    @Schema(description = "排序顺序")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 创建者
     */
    @Schema(description = "创建者")
    private String createBy;
}
