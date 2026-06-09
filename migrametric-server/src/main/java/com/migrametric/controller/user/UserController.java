package com.migrametric.controller.user;

import com.migrametric.common.PageResult;
import com.migrametric.common.Result;
import com.migrametric.dto.user.PasswordResetDTO;
import com.migrametric.dto.user.UserCreateDTO;
import com.migrametric.dto.user.UserQueryDTO;
import com.migrametric.dto.user.UserUpdateDTO;
import com.migrametric.service.user.UserService;
import com.migrametric.vo.user.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理Controller
 *
 * @author MigraMetric Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "用户管理", description = "用户CRUD相关接口")
public class UserController {

    private final UserService userService;

    /**
     * 创建用户
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户")
    public Result<Long> createUser(@Valid @RequestBody UserCreateDTO dto) {
        log.info("创建用户请求, username: {}", dto.getUsername());
        Long userId = userService.createUser(dto);
        return Result.success(userId);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{userId}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    public Result<Void> updateUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("userId") Long userId,
            @Valid @RequestBody UserUpdateDTO dto) {
        log.info("更新用户请求, userId: {}", userId);
        dto.setId(userId);
        userService.updateUser(dto);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户", description = "删除用户")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("userId") Long userId) {
        log.info("删除用户请求, userId: {}", userId);
        userService.deleteUser(userId);
        return Result.success();
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取用户详情", description = "根据ID获取用户信息")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("userId") Long userId) {
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }

    /**
     * 分页查询用户
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询用户", description = "分页查询用户列表")
    public Result<PageResult<UserVO>> queryPage(UserQueryDTO queryDTO) {
        log.info("分页查询用户请求, queryDTO: {}", queryDTO);
        PageResult<UserVO> result = userService.queryPage(queryDTO);
        return Result.success(result);
    }

    /**
     * 重置密码
     */
    @PostMapping("/password/reset")
    @Operation(summary = "重置密码", description = "重置用户密码")
    public Result<Void> resetPassword(@Valid @RequestBody PasswordResetDTO dto) {
        log.info("重置密码请求, userId: {}", dto.getUserId());
        userService.resetPassword(dto);
        return Result.success();
    }

    /**
     * 修改用户状态
     */
    @PutMapping("/{userId}/status")
    @Operation(summary = "修改用户状态", description = "启用或禁用用户")
    public Result<Void> updateStatus(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("userId") Long userId,
            @Parameter(description = "状态：0-禁用，1-启用", required = true)
            @RequestParam("status") Integer status) {
        log.info("修改用户状态请求, userId: {}, status: {}", userId, status);
        userService.updateStatus(userId, status);
        return Result.success();
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check/username")
    @Operation(summary = "检查用户名", description = "检查用户名是否已存在")
    public Result<Boolean> checkUsername(
            @Parameter(description = "用户名", required = true)
            @RequestParam("username") String username,
            @Parameter(description = "排除的用户ID")
            @RequestParam(value = "excludeId", required = false) Long excludeId) {
        boolean exists = userService.isUsernameExists(username, excludeId);
        return Result.success(exists);
    }
}
