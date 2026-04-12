package com.migrametric.mapper.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.migrametric.entity.permission.Permission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限Mapper接口
 *
 * @author MigraMetric Team
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}
