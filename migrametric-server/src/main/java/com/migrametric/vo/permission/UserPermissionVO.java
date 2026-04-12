package com.migrametric.vo.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户权限信息VO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "用户权限信息VO")
public class UserPermissionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 角色
     */
    @Schema(description = "角色")
    private String role;

    /**
     * 菜单权限列表
     */
    @Schema(description = "菜单权限列表")
    private Object menus;

    /**
     * 按钮权限列表
     */
    @Schema(description = "按钮权限列表")
    private Object buttons;
}
