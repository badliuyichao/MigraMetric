package com.migrametric.service.log;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.migrametric.dto.log.LoginLogQueryDTO;
import com.migrametric.entity.log.LoginLog;
import com.migrametric.vo.log.LoginLogVO;

/**
 * 登录日志服务接口
 *
 * @author MigraMetric Team
 */
public interface LoginLogService {

    /**
     * 记录登录日志
     *
     * @param loginLog 登录日志实体
     */
    void saveLoginLog(LoginLog loginLog);

    /**
     * 分页查询登录日志
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<LoginLogVO> queryPage(LoginLogQueryDTO queryDTO);

    /**
     * 获取登录日志详情
     *
     * @param id 日志ID
     * @return 登录日志详情
     */
    LoginLogVO getById(Long id);

    /**
     * 清空所有登录日志
     */
    void clearAll();

    /**
     * 删除指定登录日志
     *
     * @param id 日志ID
     */
    void deleteById(Long id);

    /**
     * 获取用户最后登录信息
     *
     * @param username 用户名
     * @return 最后登录日志
     */
    LoginLogVO getLastLogin(String username);
}
