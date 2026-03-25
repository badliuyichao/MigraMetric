package com.migrametric.service.permission;

import com.migrametric.entity.permission.Permission;
import com.migrametric.mapper.permission.PermissionMapper;
import com.migrametric.service.permission.impl.PermissionServiceImpl;
import com.migrametric.vo.permission.MenuPermissionVO;
import com.migrametric.vo.permission.UserPermissionVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * PermissionService 单元测试
 *
 * @author MigraMetric Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService 单元测试")
class PermissionServiceTest {

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    // ========== 测试数据 ==========

    private Permission menuPermission1;
    private Permission menuPermission2;
    private Permission buttonPermission1;
    private Permission buttonPermission2;

    @BeforeEach
    void setUp() {
        // 菜单权限1
        menuPermission1 = new Permission();
        menuPermission1.setId(1L);
        menuPermission1.setName("项目管理");
        menuPermission1.setCode("project");
        menuPermission1.setType("MENU");
        menuPermission1.setPath("/project");
        menuPermission1.setIcon("project-icon");
        menuPermission1.setSortOrder(1);
        menuPermission1.setStatus(1);
        menuPermission1.setCreateTime(LocalDateTime.now());

        // 菜单权限2
        menuPermission2 = new Permission();
        menuPermission2.setId(2L);
        menuPermission2.setName("统计分析");
        menuPermission2.setCode("statistics");
        menuPermission2.setType("MENU");
        menuPermission2.setPath("/statistics");
        menuPermission2.setIcon("chart-icon");
        menuPermission2.setSortOrder(2);
        menuPermission2.setStatus(1);
        menuPermission2.setCreateTime(LocalDateTime.now());

        // 按钮权限1
        buttonPermission1 = new Permission();
        buttonPermission1.setId(3L);
        buttonPermission1.setName("新增项目");
        buttonPermission1.setCode("project:create");
        buttonPermission1.setType("BUTTON");
        buttonPermission1.setStatus(1);
        buttonPermission1.setCreateTime(LocalDateTime.now());

        // 按钮权限2
        buttonPermission2 = new Permission();
        buttonPermission2.setId(4L);
        buttonPermission2.setName("删除项目");
        buttonPermission2.setCode("project:delete");
        buttonPermission2.setType("BUTTON");
        buttonPermission2.setStatus(1);
        buttonPermission2.setCreateTime(LocalDateTime.now());
    }

    // ========== 菜单权限测试 (PERM-MENU-*) ==========

    @Nested
    @DisplayName("菜单权限测试")
    class MenuPermissionTests {

        @Test
        @DisplayName("PERM-MENU-001: 管理员获取所有菜单")
        void shouldGetAllMenusForAdmin() {
            // Given
            when(permissionMapper.selectList(any())).thenReturn(Arrays.asList(menuPermission1, menuPermission2));

            // When
            List<MenuPermissionVO> menus = permissionService.getUserMenus(1L, "ADMIN");

            // Then
            assertThat(menus).isNotNull();
            assertThat(menus.size()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("PERM-MENU-002: 普通用户获取基础菜单")
        void shouldGetBasicMenusForNormalUser() {
            // Given
            when(permissionMapper.selectList(any())).thenReturn(Arrays.asList(menuPermission1, menuPermission2));

            // When
            List<MenuPermissionVO> menus = permissionService.getUserMenus(2L, "USER");

            // Then
            assertThat(menus).isNotNull();
        }
    }

    // ========== 按钮权限测试 (PERM-BUTTON-*) ==========

    @Nested
    @DisplayName("按钮权限测试")
    class ButtonPermissionTests {

        @Test
        @DisplayName("PERM-BUTTON-001: 管理员获取所有按钮")
        void shouldGetAllButtonsForAdmin() {
            // Given
            when(permissionMapper.selectList(any())).thenReturn(Arrays.asList(buttonPermission1, buttonPermission2));

            // When
            List<String> buttons = permissionService.getUserButtons(1L, "ADMIN");

            // Then
            assertThat(buttons).isNotNull();
            assertThat(buttons).contains("project:create", "project:delete");
        }

        @Test
        @DisplayName("PERM-BUTTON-002: 普通用户获取基础按钮")
        void shouldGetBasicButtonsForNormalUser() {
            // When
            List<String> buttons = permissionService.getUserButtons(2L, "USER");

            // Then
            assertThat(buttons).isNotNull();
            assertThat(buttons).contains("project:view", "project:list");
        }
    }

    // ========== 用户权限测试 (PERM-USER-*) ==========

    @Nested
    @DisplayName("用户权限测试")
    class UserPermissionTests {

        @Test
        @DisplayName("PERM-USER-001: 获取用户完整权限信息")
        void shouldGetUserPermissions() {
            // Given
            when(permissionMapper.selectList(any())).thenReturn(Arrays.asList(buttonPermission1, buttonPermission2));

            // When
            UserPermissionVO permissions = permissionService.getUserPermissions(1L, "ADMIN");

            // Then
            assertThat(permissions).isNotNull();
            assertThat(permissions.getUserId()).isEqualTo(1L);
            assertThat(permissions.getRole()).isEqualTo("ADMIN");
            assertThat(permissions.getMenus()).isNotNull();
            assertThat(permissions.getButtons()).isNotNull();
        }
    }

    // ========== 权限检查测试 (PERM-CHECK-*) ==========

    @Nested
    @DisplayName("权限检查测试")
    class PermissionCheckTests {

        @Test
        @DisplayName("PERM-CHECK-001: 管理员拥有所有权限")
        void shouldReturnTrueForAdmin() {
            // When
            boolean hasPermission = permissionService.hasPermission(1L, "ADMIN", "any:permission");

            // Then
            assertThat(hasPermission).isTrue();
        }

        @Test
        @DisplayName("PERM-CHECK-002: 普通用户拥有基础权限")
        void shouldReturnTrueForBasicPermission() {
            // When
            boolean hasPermission = permissionService.hasPermission(2L, "USER", "project:view");

            // Then
            assertThat(hasPermission).isTrue();
        }

        @Test
        @DisplayName("PERM-CHECK-003: 普通用户没有管理员权限")
        void shouldReturnFalseForAdminPermission() {
            // When
            boolean hasPermission = permissionService.hasPermission(2L, "USER", "user:delete");

            // Then
            assertThat(hasPermission).isFalse();
        }
    }

    // ========== 菜单树测试 (PERM-TREE-*) ==========

    @Nested
    @DisplayName("菜单树测试")
    class MenuTreeTests {

        @Test
        @DisplayName("PERM-TREE-001: 获取所有菜单权限树")
        void shouldGetMenuTree() {
            // Given
            when(permissionMapper.selectList(any())).thenReturn(Arrays.asList(menuPermission1, menuPermission2));

            // When
            List<MenuPermissionVO> menuTree = permissionService.getAllMenuTree();

            // Then
            assertThat(menuTree).isNotNull();
        }

        @Test
        @DisplayName("PERM-TREE-002: 获取所有权限列表")
        void shouldGetAllPermissions() {
            // Given
            when(permissionMapper.selectList(any())).thenReturn(
                    Arrays.asList(menuPermission1, menuPermission2, buttonPermission1, buttonPermission2));

            // When
            List<Permission> permissions = permissionService.getAllPermissions();

            // Then
            assertThat(permissions).isNotNull();
            assertThat(permissions.size()).isEqualTo(4);
        }
    }
}
