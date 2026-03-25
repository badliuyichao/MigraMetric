package com.migrametric.mapper.log;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.migrametric.entity.log.LoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志Mapper
 *
 * @author MigraMetric Team
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
}
