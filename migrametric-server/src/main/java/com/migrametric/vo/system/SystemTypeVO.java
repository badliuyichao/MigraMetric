package com.migrametric.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统类型VO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "系统类型VO")
public class SystemTypeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 系统类型ID
     */
    @Schema(description = "系统类型ID")
    private Long id;

    /**
     * 系统名称
     */
    @Schema(description = "系统名称")
    private String systemName;

    /**
     * 系统类别：1-源系统，2-目标系统
     */
    @Schema(description = "系统类别")
    private Integer systemCategory;

    /**
     * 系统类别文本
     */
    @Schema(description = "系统类别文本")
    private String systemCategoryText;

    /**
     * 描述信息
     */
    @Schema(description = "描述信息")
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
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
