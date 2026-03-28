package com.migrametric.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.migrametric.common.BusinessException;
import com.migrametric.common.ResultCode;
import com.migrametric.config.JwtProperties;
import com.migrametric.context.UserContext;
import com.migrametric.dto.auth.LoginDTO;
import com.migrametric.entity.user.User;
import com.migrametric.mapper.user.UserMapper;
import com.migrametric.vo.auth.LoginVO;
import com.migrametric.vo.auth.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Transactional
    public LoginVO login(LoginDTO loginDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_ERROR);
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_ERROR);
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        String token = jwtService.generateToken(user);

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setExpiresIn(jwtProperties.getExpireTime());
        return loginVO;
    }

    public UserInfoVO getCurrentUserInfo() {
        UserContext.UserInfo userInfo = UserContext.getCurrentUser();
        if (userInfo == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        User user = userMapper.selectById(userInfo.getId());
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setName(user.getName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        return vo;
    }

    public void logout() {
        Long userId = UserContext.getCurrentUserId();
        UserContext.clear();
        log.info("用户登出: userId={}", userId);
    }
}