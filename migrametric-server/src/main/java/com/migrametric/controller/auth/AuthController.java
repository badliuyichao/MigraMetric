package com.migrametric.controller.auth;

import com.migrametric.common.Result;
import com.migrametric.dto.auth.LoginDTO;
import com.migrametric.service.auth.AuthService;
import com.migrametric.vo.auth.LoginVO;
import com.migrametric.vo.auth.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "登录、登出、用户信息相关接口")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "通过用户名密码登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        log.info("登录请求: username={}", loginDTO.getUsername());
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户信息")
    public Result<UserInfoVO> getUserInfo() {
        UserInfoVO userInfo = authService.getCurrentUserInfo();
        return Result.success(userInfo);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出登录")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}