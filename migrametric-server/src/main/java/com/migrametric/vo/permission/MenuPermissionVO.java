package com.migrametric.vo.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 菜单权限VO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "菜单权限VO")
public class MenuPermissionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    @Schema(description = "权限ID")
    private Long id;

    /**
     * 权限名称
     */
    @Schema(description = "权限名称")
    private String name;

    /**
     * 权限编码
     */
    @Schema(description = "权限编码")
    private String code;

    /**
     * 权限类型：MENU-菜单，BUTTON-按钮
     */
    @Schema(description = "权限类型")
    private String type;

    /**
     * 路径
     */
    @Schema(description = "路径")
    private String path;

    /**
     * 图标
     */
    @Schema(description = "图标")
    private String icon;

    /**
     * 子权限
     */
    @Schema(description = "子权限")
    private List<MenuPermissionVO> children;
}
