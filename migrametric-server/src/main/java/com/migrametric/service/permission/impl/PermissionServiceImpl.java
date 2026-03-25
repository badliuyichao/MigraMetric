package com.migrametric.service.permission.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.migrametric.entity.permission.Permission;
import com.migrametric.mapper.permission.PermissionMapper;
import com.migrametric.service.permission.PermissionService;
import com.migrametric.vo.permission.MenuPermissionVO;
import com.migrametric.vo.permission.UserPermissionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 *
 * @author MigraMetric Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;

    /**
     * 管理员角色
     */
    private static final String ADMIN_ROLE = "ADMIN";

    @Override
    public List<MenuPermissionVO> getUserMenus(Long userId, String role) {
        log.info("获取用户菜单权限, userId: {}, role: {}", userId, role);

        // 管理员拥有所有菜单权限
        if (ADMIN_ROLE.equals(role)) {
            return getAllMenuPermissions();
        }

        // 普通用户返回基础菜单
        return getBasicMenus();
    }

    @Override
    public List<String> getUserButtons(Long userId, String role) {
        log.info("获取用户按钮权限, userId: {}, role: {}", userId, role);

        // 管理员拥有所有按钮权限
        if (ADMIN_ROLE.equals(role)) {
            return getAllButtonPermissions();
        }

        // 普通用户返回基础按钮权限
        return getBasicButtons();
    }

    @Override
    public UserPermissionVO getUserPermissions(Long userId, String role) {
        UserPermissionVO vo = new UserPermissionVO();
        vo.setUserId(userId);
        vo.setRole(role);
        vo.setMenus(getUserMenus(userId, role));
        vo.setButtons(getUserButtons(userId, role));
        return vo;
    }

    @Override
    public boolean hasPermission(Long userId, String role, String permissionCode) {
        // 管理员拥有所有权限
        if (ADMIN_ROLE.equals(role)) {
            return true;
        }

        // 检查用户是否拥有指定权限
        List<String> buttons = getUserButtons(userId, role);
        return buttons.contains(permissionCode);
    }

    @Override
    public List<MenuPermissionVO> getAllMenuTree() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getType, "MENU")
               .eq(Permission::getStatus, 1)
               .orderByAsc(Permission::getSortOrder);

        List<Permission> permissions = permissionMapper.selectList(wrapper);
        return buildMenuTree(permissions, null);
    }

    @Override
    public List<Permission> getAllPermissions() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getStatus, 1)
               .orderByAsc(Permission::getType, Permission::getSortOrder);
        return permissionMapper.selectList(wrapper);
    }

    /**
     * 获取所有菜单权限
     */
    private List<MenuPermissionVO> getAllMenuPermissions() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getType, "MENU")
               .eq(Permission::getStatus, 1)
               .orderByAsc(Permission::getSortOrder);

        List<Permission> permissions = permissionMapper.selectList(wrapper);
        return buildMenuTree(permissions, null);
    }

    /**
     * 获取基础菜单
     */
    private List<MenuPermissionVO> getBasicMenus() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getType, "MENU")
               .eq(Permission::getStatus, 1)
               .orderByAsc(Permission::getSortOrder)
               .last("LIMIT 10"); // 限制基础菜单数量

        List<Permission> permissions = permissionMapper.selectList(wrapper);
        return buildMenuTree(permissions, null);
    }

    /**
     * 获取所有按钮权限
     */
    private List<String> getAllButtonPermissions() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getType, "BUTTON")
               .eq(Permission::getStatus, 1);

        List<Permission> permissions = permissionMapper.selectList(wrapper);
        return permissions.stream()
                .map(Permission::getCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取基础按钮权限
     */
    private List<String> getBasicButtons() {
        // 返回基础按钮权限
        return Arrays.asList(
            "project:view",
            "project:list",
            "evaluation:view",
            "statistics:view"
        );
    }

    /**
     * 构建菜单树
     */
    private List<MenuPermissionVO> buildMenuTree(List<Permission> permissions, Long parentId) {
        return permissions.stream()
                .filter(p -> (parentId == null && p.getParentId() == null) ||
                             (parentId != null && parentId.equals(p.getParentId())))
                .map(this::convertToMenuVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为菜单VO
     */
    private MenuPermissionVO convertToMenuVO(Permission permission) {
        MenuPermissionVO vo = new MenuPermissionVO();
        vo.setId(permission.getId());
        vo.setName(permission.getName());
        vo.setCode(permission.getCode());
        vo.setType(permission.getType());
        vo.setPath(permission.getPath());
        vo.setIcon(permission.getIcon());

        // 递归获取子菜单
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getParentId, permission.getId())
               .eq(Permission::getType, "MENU")
               .eq(Permission::getStatus, 1)
               .orderByAsc(Permission::getSortOrder);

        List<Permission> children = permissionMapper.selectList(wrapper);
        if (!children.isEmpty()) {
            vo.setChildren(buildMenuTree(children, permission.getId()));
        }

        return vo;
    }
}
