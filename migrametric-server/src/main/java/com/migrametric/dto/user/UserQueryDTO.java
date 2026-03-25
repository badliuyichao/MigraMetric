package com.migrametric.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户查询DTO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "用户查询DTO")
public class UserQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名（模糊查询）
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 用户姓名（模糊查询）
     */
    @Schema(description = "用户姓名")
    private String name;

    /**
     * 角色
     */
    @Schema(description = "角色")
    private String role;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码")
    private Integer current = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小")
    private Integer pageSize = 10;
}
