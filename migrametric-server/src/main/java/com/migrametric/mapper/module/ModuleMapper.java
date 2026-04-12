package com.migrametric.mapper.module;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.migrametric.entity.module.Module;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模块Mapper接口
 *
 * @author MigraMetric Team
 */
@Mapper
public interface ModuleMapper extends BaseMapper<Module> {

    /**
     * 根据模块名称统计数量（排除指定ID）
     *
     * @param moduleName 模块名称
     * @param excludeId 排除的模块ID
     * @return 数量
     */
    int countByModuleNameExcludeId(String moduleName, Long excludeId);

    /**
     * 根据项目ID统计模块引用数量
     *
     * @param moduleId 模块ID
     * @return 引用数量
     */
    int countByProjectReference(Long moduleId);
}
