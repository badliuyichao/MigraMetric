package com.migrametric.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.migrametric.common.BusinessException;
import com.migrametric.common.PageResult;
import com.migrametric.common.ResultCode;
import com.migrametric.dto.user.PasswordResetDTO;
import com.migrametric.dto.user.UserCreateDTO;
import com.migrametric.dto.user.UserQueryDTO;
import com.migrametric.dto.user.UserUpdateDTO;
import com.migrametric.entity.user.User;
import com.migrametric.mapper.user.UserMapper;
import com.migrametric.service.user.UserService;
import com.migrametric.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * @author MigraMetric Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Long createUser(UserCreateDTO dto) {
        log.info("创建用户, username: {}", dto.getUsername());

        // 检查用户名是否存在
        if (isUsernameExists(dto.getUsername(), null)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }

        // 创建用户实体
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        user.setStatus(1); // 默认启用
        user.setCreateTime(LocalDateTime.now());
        user.setRemark(dto.getRemark());

        userMapper.insert(user);
        log.info("用户创建成功, userId: {}", user.getId());

        return user.getId();
    }

    @Override
    public void updateUser(UserUpdateDTO dto) {
        log.info("更新用户, userId: {}", dto.getId());

        User user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        // 更新字段
        if (StringUtils.hasText(dto.getName())) {
            user.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        if (dto.getRemark() != null) {
            user.setRemark(dto.getRemark());
        }

        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户更新成功, userId: {}", dto.getId());
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("删除用户, userId: {}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        // 不允许删除管理员账户
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能删除管理员账户");
        }

        userMapper.deleteById(userId);
        log.info("用户删除成功, userId: {}", userId);
    }

    @Override
    public UserVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    public PageResult<UserVO> queryPage(UserQueryDTO queryDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(User::getUsername, queryDTO.getUsername());
        }
        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(User::getName, queryDTO.getName());
        }
        if (StringUtils.hasText(queryDTO.getRole())) {
            wrapper.eq(User::getRole, queryDTO.getRole());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(User::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(User::getCreateTime);

        Page<User> page = new Page<>(queryDTO.getCurrent(), queryDTO.getPageSize());
        IPage<User> result = userMapper.selectPage(page, wrapper);

        List<UserVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public void resetPassword(PasswordResetDTO dto) {
        log.info("重置密码, userId: {}", dto.getUserId());

        // 验证两次密码输入
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "两次密码输入不一致");
        }

        User user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("密码重置成功, userId: {}", dto.getUserId());
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        log.info("更新用户状态, userId: {}, status: {}", userId, status);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        // 不允许禁用管理员账户
        if ("ADMIN".equals(user.getRole()) && status == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能禁用管理员账户");
        }

        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户状态更新成功, userId: {}, status: {}", userId, status);
    }

    @Override
    public boolean isUsernameExists(String username, Long excludeId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (excludeId != null) {
            wrapper.ne(User::getId, excludeId);
        }
        return userMapper.selectCount(wrapper) > 0;
    }

    /**
     * 转换为VO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setName(user.getName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setRoleName(getRoleName(user.getRole()));
        vo.setStatus(user.getStatus());
        vo.setStatusName(getStatusName(user.getStatus()));
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setRemark(user.getRemark());
        return vo;
    }

    /**
     * 获取角色名称
     */
    private String getRoleName(String role) {
        if (role == null) return "-";
        return switch (role) {
            case "ADMIN" -> "系统管理员";
            case "USER" -> "普通用户";
            default -> role;
        };
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) return "-";
        return status == 1 ? "启用" : "禁用";
    }
}
