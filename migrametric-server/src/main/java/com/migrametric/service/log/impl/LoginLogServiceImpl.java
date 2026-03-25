package com.migrametric.service.log.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.migrametric.dto.log.LoginLogQueryDTO;
import com.migrametric.entity.log.LoginLog;
import com.migrametric.mapper.log.LoginLogMapper;
import com.migrametric.service.log.LoginLogService;
import com.migrametric.vo.log.LoginLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录日志服务实现类
 *
 * @author MigraMetric Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogMapper loginLogMapper;

    @Override
    public void saveLoginLog(LoginLog loginLog) {
        log.debug("保存登录日志: 用户={}, IP={}, 状态={}",
                loginLog.getUsername(), loginLog.getIpAddress(), loginLog.getStatus());
        loginLogMapper.insert(loginLog);
    }

    @Override
    public IPage<LoginLogVO> queryPage(LoginLogQueryDTO queryDTO) {
        Page<LoginLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();

        // 动态添加查询条件
        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(LoginLog::getUsername, queryDTO.getUsername());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(LoginLog::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getIpAddress())) {
            wrapper.like(LoginLog::getIpAddress, queryDTO.getIpAddress());
        }
        if (StringUtils.hasText(queryDTO.getStartTime())) {
            wrapper.ge(LoginLog::getLoginTime,
                    LocalDateTime.parse(queryDTO.getStartTime() + " 00:00:00",
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (StringUtils.hasText(queryDTO.getEndTime())) {
            wrapper.le(LoginLog::getLoginTime,
                    LocalDateTime.parse(queryDTO.getEndTime() + " 23:59:59",
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        wrapper.orderByDesc(LoginLog::getLoginTime);

        IPage<LoginLog> resultPage = loginLogMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<LoginLogVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<LoginLogVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public LoginLogVO getById(Long id) {
        LoginLog loginLog = loginLogMapper.selectById(id);
        return loginLog != null ? convertToVO(loginLog) : null;
    }

    @Override
    public void clearAll() {
        loginLogMapper.delete(null);
        log.info("清空所有登录日志");
    }

    @Override
    public void deleteById(Long id) {
        loginLogMapper.deleteById(id);
        log.info("删除登录日志, id={}", id);
    }

    @Override
    public LoginLogVO getLastLogin(String username) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoginLog::getUsername, username)
               .orderByDesc(LoginLog::getLoginTime)
               .last("LIMIT 1");

        LoginLog loginLog = loginLogMapper.selectOne(wrapper);
        return loginLog != null ? convertToVO(loginLog) : null;
    }

    /**
     * 转换为VO
     */
    private LoginLogVO convertToVO(LoginLog loginLog) {
        LoginLogVO vo = new LoginLogVO();
        BeanUtils.copyProperties(loginLog, vo);
        return vo;
    }
}
