package com.migrametric.vo.module;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模块VO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "模块VO")
public class ModuleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模块ID
     */
    @Schema(description = "模块ID")
    private Long id;

    /**
     * 模块名称
     */
    @Schema(description = "模块名称")
    private String moduleName;

    /**
     * 所属系统ID
     */
    @Schema(description = "所属系统ID")
    private Long systemId;

    /**
     * 所属系统名称
     */
    @Schema(description = "所属系统名称")
    private String systemName;

    /**
     * 所属系统类别（1-源系统，2-目标系统）
     */
    @Schema(description = "所属系统类别")
    private Integer systemCategory;

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
     * 模块描述
     */
    @Schema(description = "模块描述")
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 状态文本
     */
    @Schema(description = "状态文本")
    private String statusText;

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
