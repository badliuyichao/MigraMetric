package com.migrametric.controller.permission;

import com.migrametric.common.Result;
import com.migrametric.entity.permission.Permission;
import com.migrametric.service.permission.PermissionService;
import com.migrametric.vo.permission.MenuPermissionVO;
import com.migrametric.vo.permission.UserPermissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理Controller
 *
 * @author MigraMetric Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/permissions")
@Tag(name = "权限管理", description = "权限相关接口")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 获取当前用户权限信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前用户权限", description = "获取当前登录用户的权限信息")
    public Result<UserPermissionVO> getCurrentUserPermissions(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "角色", required = true)
            @RequestParam String role) {
        log.info("获取用户权限请求, userId: {}, role: {}", userId, role);
        UserPermissionVO permissions = permissionService.getUserPermissions(userId, role);
        return Result.success(permissions);
    }

    /**
     * 获取用户菜单权限
     */
    @GetMapping("/menus")
    @Operation(summary = "获取用户菜单", description = "获取用户的菜单权限列表")
    public Result<List<MenuPermissionVO>> getUserMenus(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "角色", required = true)
            @RequestParam String role) {
        log.info("获取用户菜单请求, userId: {}, role: {}", userId, role);
        List<MenuPermissionVO> menus = permissionService.getUserMenus(userId, role);
        return Result.success(menus);
    }

    /**
     * 获取用户按钮权限
     */
    @GetMapping("/buttons")
    @Operation(summary = "获取用户按钮权限", description = "获取用户的按钮权限列表")
    public Result<List<String>> getUserButtons(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "角色", required = true)
            @RequestParam String role) {
        log.info("获取用户按钮权限请求, userId: {}, role: {}", userId, role);
        List<String> buttons = permissionService.getUserButtons(userId, role);
        return Result.success(buttons);
    }

    /**
     * 检查用户是否有指定权限
     */
    @GetMapping("/check")
    @Operation(summary = "检查权限", description = "检查用户是否有指定权限")
    public Result<Boolean> checkPermission(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "角色", required = true)
            @RequestParam String role,
            @Parameter(description = "权限编码", required = true)
            @RequestParam String permissionCode) {
        log.info("检查权限请求, userId: {}, role: {}, code: {}", userId, role, permissionCode);
        boolean hasPermission = permissionService.hasPermission(userId, role, permissionCode);
        return Result.success(hasPermission);
    }

    /**
     * 获取所有菜单权限树
     */
    @GetMapping("/menu-tree")
    @Operation(summary = "获取菜单权限树", description = "获取所有菜单权限的树形结构")
    public Result<List<MenuPermissionVO>> getMenuTree() {
        log.info("获取菜单权限树请求");
        List<MenuPermissionVO> menuTree = permissionService.getAllMenuTree();
        return Result.success(menuTree);
    }

    /**
     * 获取所有权限列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有权限", description = "获取所有启用的权限列表")
    public Result<List<Permission>> getAllPermissions() {
        log.info("获取所有权限请求");
        List<Permission> permissions = permissionService.getAllPermissions();
        return Result.success(permissions);
    }
}
