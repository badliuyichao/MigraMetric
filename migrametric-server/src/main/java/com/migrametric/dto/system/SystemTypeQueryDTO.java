package com.migrametric.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统类型查询DTO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "系统类型查询DTO")
public class SystemTypeQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 系统名称（模糊查询）
     */
    @Schema(description = "系统名称")
    private String systemName;

    /**
     * 系统类别：1-源系统，2-目标系统
     */
    @Schema(description = "系统类别")
    private Integer systemCategory;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 页码
     */
    @Schema(description = "页码")
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数")
    private Integer pageSize = 10;
}
