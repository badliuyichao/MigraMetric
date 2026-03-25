package com.migrametric.service.user;

import com.migrametric.common.PageResult;
import com.migrametric.dto.user.PasswordResetDTO;
import com.migrametric.dto.user.UserCreateDTO;
import com.migrametric.dto.user.UserQueryDTO;
import com.migrametric.dto.user.UserUpdateDTO;
import com.migrametric.vo.user.UserVO;

/**
 * 用户服务接口
 *
 * @author MigraMetric Team
 */
public interface UserService {

    /**
     * 创建用户
     *
     * @param dto 用户创建信息
     * @return 用户ID
     */
    Long createUser(UserCreateDTO dto);

    /**
     * 更新用户
     *
     * @param dto 用户更新信息
     */
    void updateUser(UserUpdateDTO dto);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getUserById(Long userId);

    /**
     * 分页查询用户
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<UserVO> queryPage(UserQueryDTO queryDTO);

    /**
     * 重置密码
     *
     * @param dto 密码重置信息
     */
    void resetPassword(PasswordResetDTO dto);

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     */
    void updateStatus(Long userId, Integer status);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    boolean isUsernameExists(String username, Long excludeId);
}
