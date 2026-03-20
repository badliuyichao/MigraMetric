package com.migrametric.mapper.config;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.migrametric.entity.config.ReportConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报表配置Mapper接口
 *
 * @author MigraMetric Team
 */
@Mapper
public interface ReportConfigMapper extends BaseMapper<ReportConfig> {

    /**
     * 根据配置键查询
     *
     * @param configKey 配置键
     * @return 配置信息
     */
    ReportConfig selectByConfigKey(String configKey);
}
