package com.migrametric.mapper.log;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.migrametric.entity.log.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 *
 * @author MigraMetric Team
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
