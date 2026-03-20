package com.migrametric.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.migrametric.entity.system.SystemType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统类型Mapper接口
 *
 * @author MigraMetric Team
 */
@Mapper
public interface SystemTypeMapper extends BaseMapper<SystemType> {

    /**
     * 检查系统名称是否存在（排除指定ID）
     *
     * @param systemName 系统名称
     * @param excludeId  排除的ID
     * @return 数量
     */
    int countBySystemNameExcludeId(@Param("systemName") String systemName, @Param("excludeId") Long excludeId);

    /**
     * 检查系统名称是否存在
     *
     * @param systemName 系统名称
     * @return 数量
     */
    int countBySystemName(@Param("systemName") String systemName);

    /**
     * 检查是否被模块引用
     *
     * @param systemTypeId 系统类型ID
     * @return 引用数量
     */
    int countByModuleReference(@Param("systemTypeId") Long systemTypeId);

    /**
     * 检查是否被项目引用
     *
     * @param systemTypeId 系统类型ID
     * @return 引用数量
     */
    int countByProjectSourceReference(@Param("systemTypeId") Long systemTypeId);

    /**
     * 检查是否被项目引用（目标系统）
     *
     * @param systemTypeId 系统类型ID
     * @return 引用数量
     */
    int countByProjectTargetReference(@Param("systemTypeId") Long systemTypeId);
}
