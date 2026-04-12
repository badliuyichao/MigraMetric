package com.migrametric.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.migrametric.entity.user.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 *
 * @author MigraMetric Team
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
