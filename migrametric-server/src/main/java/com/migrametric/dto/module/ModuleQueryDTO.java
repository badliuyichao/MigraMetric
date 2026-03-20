package com.migrametric.dto.module;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 模块查询DTO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "模块查询DTO")
public class ModuleQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模块名称（模糊查询）
     */
    @Schema(description = "模块名称")
    private String moduleName;

    /**
     * 所属系统ID
     */
    @Schema(description = "所属系统ID")
    private Long systemId;

    /**
     * 模块分类
     */
    @Schema(description = "模块分类")
    private String category;

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
     * 每页数量
     */
    @Schema(description = "每页数量")
    private Integer pageSize = 10;
}
