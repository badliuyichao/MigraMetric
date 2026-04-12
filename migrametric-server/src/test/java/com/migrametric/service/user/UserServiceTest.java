package com.migrametric.service.user;

import com.migrametric.common.BusinessException;
import com.migrametric.common.PageResult;
import com.migrametric.dto.user.PasswordResetDTO;
import com.migrametric.dto.user.UserCreateDTO;
import com.migrametric.dto.user.UserQueryDTO;
import com.migrametric.dto.user.UserUpdateDTO;
import com.migrametric.entity.user.User;
import com.migrametric.mapper.user.UserMapper;
import com.migrametric.service.user.impl.UserServiceImpl;
import com.migrametric.vo.user.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 *
 * @author MigraMetric Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 单元测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    // ========== 测试数据 ==========

    private User testUser;
    private UserCreateDTO createDTO;
    private UserUpdateDTO updateDTO;
    private PasswordResetDTO passwordResetDTO;

    @BeforeEach
    void setUp() {
        // 测试用户
        testUser = createTestUser();

        // 创建DTO
        createDTO = new UserCreateDTO();
        createDTO.setUsername("newuser");
        createDTO.setPassword("password123");
        createDTO.setName("新用户");
        createDTO.setEmail("new@example.com");
        createDTO.setPhone("13900139000");
        createDTO.setRole("USER");

        // 更新DTO
        updateDTO = new UserUpdateDTO();
        updateDTO.setId(1L);
        updateDTO.setName("更新后的用户");
        updateDTO.setEmail("updated@example.com");

        // 密码重置DTO
        passwordResetDTO = new PasswordResetDTO();
        passwordResetDTO.setUserId(1L);
        passwordResetDTO.setNewPassword("newpassword123");
        passwordResetDTO.setConfirmPassword("newpassword123");
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("$2a$10$encoded");
        user.setName("测试用户");
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setRole("USER");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return user;
    }

    // ========== 创建用户测试 (USER-CREATE-*) ==========

    @Nested
    @DisplayName("创建用户测试")
    class CreateUserTests {

        @Test
        @DisplayName("USER-CREATE-001: 成功创建用户")
        void shouldCreateUserSuccessfully() {
            // Given
            when(userMapper.selectCount(any())).thenReturn(0L);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");
            when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return 1;
            });

            // When
            Long userId = userService.createUser(createDTO);

            // Then
            assertThat(userId).isEqualTo(1L);
            verify(userMapper).insert(any(User.class));
            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("USER-CREATE-002: 用户名已存在抛出异常")
        void shouldThrowExceptionWhenUsernameExists() {
            // Given
            when(userMapper.selectCount(any())).thenReturn(1L);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(createDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户名已存在");
        }
    }

    // ========== 更新用户测试 (USER-UPDATE-*) ==========

    @Nested
    @DisplayName("更新用户测试")
    class UpdateUserTests {

        @Test
        @DisplayName("USER-UPDATE-001: 成功更新用户")
        void shouldUpdateUserSuccessfully() {
            // Given
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            // When
            userService.updateUser(updateDTO);

            // Then
            verify(userMapper).updateById(any(User.class));
        }

        @Test
        @DisplayName("USER-UPDATE-002: 用户不存在抛出异常")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userMapper.selectById(999L)).thenReturn(null);

            updateDTO.setId(999L);

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(updateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户不存在");
        }
    }

    // ========== 删除用户测试 (USER-DELETE-*) ==========

    @Nested
    @DisplayName("删除用户测试")
    class DeleteUserTests {

        @Test
        @DisplayName("USER-DELETE-001: 成功删除用户")
        void shouldDeleteUserSuccessfully() {
            // Given
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.deleteById(1L)).thenReturn(1);

            // When
            userService.deleteUser(1L);

            // Then
            verify(userMapper).deleteById(1L);
        }

        @Test
        @DisplayName("USER-DELETE-002: 不能删除管理员账户")
        void shouldThrowExceptionWhenDeletingAdmin() {
            // Given
            testUser.setRole("ADMIN");
            when(userMapper.selectById(1L)).thenReturn(testUser);

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不能删除管理员账户");
        }

        @Test
        @DisplayName("USER-DELETE-003: 用户不存在抛出异常")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userMapper.selectById(999L)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户不存在");
        }
    }

    // ========== 获取用户详情测试 (USER-GET-*) ==========

    @Nested
    @DisplayName("获取用户详情测试")
    class GetUserByIdTests {

        @Test
        @DisplayName("USER-GET-001: 成功获取用户详情")
        void shouldGetUserByIdSuccessfully() {
            // Given
            when(userMapper.selectById(1L)).thenReturn(testUser);

            // When
            UserVO result = userService.getUserById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getName()).isEqualTo("测试用户");
        }

        @Test
        @DisplayName("USER-GET-002: 用户不存在抛出异常")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userMapper.selectById(999L)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> userService.getUserById(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户不存在");
        }
    }

    // ========== 分页查询测试 (USER-PAGE-*) ==========

    @Nested
    @DisplayName("分页查询测试")
    class QueryPageTests {

        @Test
        @DisplayName("USER-PAGE-001: 成功分页查询用户")
        void shouldQueryPageSuccessfully() {
            // Given
            UserQueryDTO queryDTO = new UserQueryDTO();
            queryDTO.setCurrent(1);
            queryDTO.setPageSize(10);

            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            page.setRecords(Collections.singletonList(testUser));
            page.setTotal(1);

            when(userMapper.selectPage(any(), any())).thenReturn(page);

            // When
            PageResult<UserVO> result = userService.queryPage(queryDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getRecords()).hasSize(1);
        }

        @Test
        @DisplayName("USER-PAGE-002: 按用户名模糊查询")
        void shouldQueryPageWithUsernameFilter() {
            // Given
            UserQueryDTO queryDTO = new UserQueryDTO();
            queryDTO.setUsername("test");
            queryDTO.setCurrent(1);
            queryDTO.setPageSize(10);

            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            page.setRecords(Collections.singletonList(testUser));
            page.setTotal(1);

            when(userMapper.selectPage(any(), any())).thenReturn(page);

            // When
            PageResult<UserVO> result = userService.queryPage(queryDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRecords()).hasSize(1);
        }
    }

    // ========== 密码重置测试 (USER-PASSWORD-*) ==========

    @Nested
    @DisplayName("密码重置测试")
    class PasswordResetTests {

        @Test
        @DisplayName("USER-PASSWORD-001: 成功重置密码")
        void shouldResetPasswordSuccessfully() {
            // Given
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$newencoded");

            // When
            userService.resetPassword(passwordResetDTO);

            // Then
            verify(userMapper).updateById(any(User.class));
            verify(passwordEncoder).encode("newpassword123");
        }

        @Test
        @DisplayName("USER-PASSWORD-002: 两次密码输入不一致")
        void shouldThrowExceptionWhenPasswordMismatch() {
            // Given
            passwordResetDTO.setConfirmPassword("differentpassword");

            // When & Then
            assertThatThrownBy(() -> userService.resetPassword(passwordResetDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("两次密码输入不一致");
        }

        @Test
        @DisplayName("USER-PASSWORD-003: 用户不存在抛出异常")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            passwordResetDTO.setUserId(999L);

            // When & Then
            assertThatThrownBy(() -> userService.resetPassword(passwordResetDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户不存在");
        }
    }

    // ========== 状态更新测试 (USER-STATUS-*) ==========

    @Nested
    @DisplayName("状态更新测试")
    class UpdateStatusTests {

        @Test
        @DisplayName("USER-STATUS-001: 成功更新用户状态")
        void shouldUpdateStatusSuccessfully() {
            // Given
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            // When
            userService.updateStatus(1L, 0);

            // Then
            verify(userMapper).updateById(any(User.class));
        }

        @Test
        @DisplayName("USER-STATUS-002: 不能禁用管理员账户")
        void shouldThrowExceptionWhenDisablingAdmin() {
            // Given
            testUser.setRole("ADMIN");
            when(userMapper.selectById(1L)).thenReturn(testUser);

            // When & Then
            assertThatThrownBy(() -> userService.updateStatus(1L, 0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不能禁用管理员账户");
        }
    }

    // ========== 用户名检查测试 (USER-CHECK-*) ==========

    @Nested
    @DisplayName("用户名检查测试")
    class CheckUsernameTests {

        @Test
        @DisplayName("USER-CHECK-001: 用户名已存在")
        void shouldReturnTrueWhenUsernameExists() {
            // Given
            when(userMapper.selectCount(any())).thenReturn(1L);

            // When
            boolean exists = userService.isUsernameExists("testuser", null);

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("USER-CHECK-002: 用户名不存在")
        void shouldReturnFalseWhenUsernameNotExists() {
            // Given
            when(userMapper.selectCount(any())).thenReturn(0L);

            // When
            boolean exists = userService.isUsernameExists("newuser", null);

            // Then
            assertThat(exists).isFalse();
        }
    }
}
