package com.migrametric.service.evaluation.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.migrametric.common.BusinessException;
import com.migrametric.common.ResultCode;
import com.migrametric.dto.evaluation.ModuleConfigDTO;
import com.migrametric.entity.evaluation.ProjectModuleConfig;
import com.migrametric.entity.module.Module;
import com.migrametric.entity.project.Project;
import com.migrametric.mapper.evaluation.ProjectModuleConfigMapper;
import com.migrametric.mapper.module.ModuleMapper;
import com.migrametric.mapper.project.ProjectMapper;
import com.migrametric.service.evaluation.ModuleConfigService;
import com.migrametric.vo.evaluation.ModuleConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 模块配置服务实现类
 *
 * @author MigraMetric Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModuleConfigServiceImpl implements ModuleConfigService {

    private final ProjectMapper projectMapper;
    private final ModuleMapper moduleMapper;
    private final ProjectModuleConfigMapper moduleConfigMapper;

    @Override
    public List<ModuleConfigVO> getProjectModules(Long projectId) {
        // 查询项目信息
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }

        Long targetSystemId = project.getTargetSystemId();
        if (targetSystemId == null) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "项目未设置目标系统");
        }

        // 查询目标系统的模块列表
        LambdaQueryWrapper<Module> moduleWrapper = new LambdaQueryWrapper<>();
        moduleWrapper.eq(Module::getSystemId, targetSystemId)
                    .eq(Module::getStatus, 1) // 只查询启用的模块
                    .orderByAsc(Module::getCategory, Module::getModuleName);
        List<Module> modules = moduleMapper.selectList(moduleWrapper);

        // 查询项目已有的模块配置
        LambdaQueryWrapper<ProjectModuleConfig> configWrapper = new LambdaQueryWrapper<>();
        configWrapper.eq(ProjectModuleConfig::getProjectId, projectId);
        List<ProjectModuleConfig> configs = moduleConfigMapper.selectList(configWrapper);

        // 构建配置Map，方便查询
        Map<Long, ProjectModuleConfig> configMap = configs.stream()
                .collect(Collectors.toMap(ProjectModuleConfig::getModuleId, Function.identity()));

        // 合并数据
        List<ModuleConfigVO> result = new ArrayList<>();
        for (Module module : modules) {
            ModuleConfigVO vo = new ModuleConfigVO();
            vo.setModuleId(module.getId());
            vo.setModuleName(module.getModuleName());
            vo.setCategory(module.getCategory());
            vo.setBaseWorkload(module.getBaseWorkload());
            vo.setDefaultWeight(module.getDefaultWeight());

            // 如果已有配置，使用配置的系数
            ProjectModuleConfig existingConfig = configMap.get(module.getId());
            if (existingConfig != null) {
                vo.setId(existingConfig.getId());
                vo.setProjectId(existingConfig.getProjectId());
                vo.setWeight(existingConfig.getWeight());
                vo.setModuleWorkload(existingConfig.getModuleWorkload());
                vo.setChecked(true);
            } else {
                // 否则使用默认值
                vo.setWeight(module.getDefaultWeight());
                vo.setChecked(false);
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveModuleConfig(Long projectId, List<ModuleConfigDTO> modules) {
        Assert.notNull(projectId, "项目ID不能为空");
        Assert.notEmpty(modules, "模块配置不能为空");

        // 校验至少选一个模块
        long checkedCount = modules.stream().filter(m -> Boolean.TRUE.equals(m.getChecked())).count();
        if (checkedCount == 0) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "请至少选择一个模块");
        }

        // 查询已有的配置
        LambdaQueryWrapper<ProjectModuleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectModuleConfig::getProjectId, projectId);
        List<ProjectModuleConfig> existingConfigs = moduleConfigMapper.selectList(wrapper);
        Map<Long, ProjectModuleConfig> existingMap = existingConfigs.stream()
                .collect(Collectors.toMap(ProjectModuleConfig::getModuleId, Function.identity()));

        // 处理每个模块配置
        for (ModuleConfigDTO dto : modules) {
            if (Boolean.TRUE.equals(dto.getChecked())) {
                // 选中的模块，保存或更新配置
                ProjectModuleConfig config = existingMap.get(dto.getModuleId());
                if (config == null) {
                    // 新增
                    config = new ProjectModuleConfig();
                    config.setProjectId(projectId);
                    config.setModuleId(dto.getModuleId());
                    config.setWeight(dto.getWeight());
                    moduleConfigMapper.insert(config);
                } else {
                    // 更新
                    config.setWeight(dto.getWeight());
                    moduleConfigMapper.updateById(config);
                }
            } else {
                // 未选中的模块，删除已有配置
                ProjectModuleConfig config = existingMap.get(dto.getModuleId());
                if (config != null) {
                    moduleConfigMapper.deleteById(config.getId());
                }
            }
        }

        log.info("保存项目模块配置成功，项目ID: {}, 选中模块数: {}", projectId, checkedCount);
    }

    @Override
    public List<ModuleConfigVO> getConfiguredModules(Long projectId) {
        LambdaQueryWrapper<ProjectModuleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectModuleConfig::getProjectId, projectId);
        List<ProjectModuleConfig> configs = moduleConfigMapper.selectList(wrapper);

        if (configs.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询模块信息
        List<Long> moduleIds = configs.stream()
                .map(ProjectModuleConfig::getModuleId)
                .collect(Collectors.toList());
        List<Module> modules = moduleMapper.selectBatchIds(moduleIds);
        Map<Long, Module> moduleMap = modules.stream()
                .collect(Collectors.toMap(Module::getId, Function.identity()));

        // 合并数据
        List<ModuleConfigVO> result = new ArrayList<>();
        for (ProjectModuleConfig config : configs) {
            Module module = moduleMap.get(config.getModuleId());
            if (module != null) {
                ModuleConfigVO vo = new ModuleConfigVO();
                vo.setId(config.getId());
                vo.setProjectId(config.getProjectId());
                vo.setModuleId(config.getModuleId());
                vo.setModuleName(module.getModuleName());
                vo.setCategory(module.getCategory());
                vo.setBaseWorkload(module.getBaseWorkload());
                vo.setDefaultWeight(module.getDefaultWeight());
                vo.setWeight(config.getWeight());
                vo.setModuleWorkload(config.getModuleWorkload());
                vo.setChecked(true);
                result.add(vo);
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearModuleConfig(Long projectId) {
        LambdaQueryWrapper<ProjectModuleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectModuleConfig::getProjectId, projectId);
        moduleConfigMapper.delete(wrapper);
        log.info("清空项目模块配置成功，项目ID: {}", projectId);
    }
}
