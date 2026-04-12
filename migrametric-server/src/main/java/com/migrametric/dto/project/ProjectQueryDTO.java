package com.migrametric.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 项目查询DTO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "项目查询DTO")
public class ProjectQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @Schema(description = "页码")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小")
    private Integer pageSize = 10;

    /**
     * 项目名称（模糊查询）
     */
    @Schema(description = "项目名称")
    private String projectName;

    /**
     * 客户名称（模糊查询）
     */
    @Schema(description = "客户名称")
    private String customerName;

    /**
     * 源系统ID
     */
    @Schema(description = "源系统ID")
    private Long sourceSystemId;

    /**
     * 目标系统ID
     */
    @Schema(description = "目标系统ID")
    private Long targetSystemId;

    /**
     * 项目状态
     */
    @Schema(description = "项目状态")
    private String status;
}
