package com.migrametric.service.evaluation;

import com.migrametric.dto.evaluation.ModuleConfigDTO;
import com.migrametric.vo.evaluation.ModuleConfigVO;

import java.util.List;

/**
 * 模块配置服务接口
 *
 * @author MigraMetric Team
 */
public interface ModuleConfigService {

    /**
     * 获取项目可选模块列表（基于目标系统）
     *
     * @param projectId 项目ID
     * @return 模块配置列表
     */
    List<ModuleConfigVO> getProjectModules(Long projectId);

    /**
     * 保存项目模块配置
     *
     * @param projectId 项目ID
     * @param modules   模块配置列表
     */
    void saveModuleConfig(Long projectId, List<ModuleConfigDTO> modules);

    /**
     * 获取项目已配置的模块
     *
     * @param projectId 项目ID
     * @return 已配置的模块列表
     */
    List<ModuleConfigVO> getConfiguredModules(Long projectId);

    /**
     * 清空项目模块配置
     *
     * @param projectId 项目ID
     */
    void clearModuleConfig(Long projectId);
}
