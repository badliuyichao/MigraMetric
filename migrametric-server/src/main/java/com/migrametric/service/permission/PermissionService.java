package com.migrametric.service.permission;

import com.migrametric.entity.permission.Permission;
import com.migrametric.vo.permission.MenuPermissionVO;
import com.migrametric.vo.permission.UserPermissionVO;

import java.util.List;

/**
 * 权限服务接口
 *
 * @author MigraMetric Team
 */
public interface PermissionService {

    /**
     * 获取用户菜单权限
     *
     * @param userId 用户ID
     * @param role 角色
     * @return 菜单权限列表
     */
    List<MenuPermissionVO> getUserMenus(Long userId, String role);

    /**
     * 获取用户按钮权限
     *
     * @param userId 用户ID
     * @param role 角色
     * @return 按钮权限编码列表
     */
    List<String> getUserButtons(Long userId, String role);

    /**
     * 获取用户完整权限信息
     *
     * @param userId 用户ID
     * @param role 角色
     * @return 用户权限信息
     */
    UserPermissionVO getUserPermissions(Long userId, String role);

    /**
     * 检查用户是否有指定权限
     *
     * @param userId 用户ID
     * @param role 角色
     * @param permissionCode 权限编码
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String role, String permissionCode);

    /**
     * 获取所有菜单权限树
     *
     * @return 菜单权限树
     */
    List<MenuPermissionVO> getAllMenuTree();

    /**
     * 获取所有启用的权限列表
     *
     * @return 权限列表
     */
    List<Permission> getAllPermissions();
}
