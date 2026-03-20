package com.migrametric.service.module.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.migrametric.common.BusinessException;
import com.migrametric.common.PageResult;
import com.migrametric.common.ResultCode;
import com.migrametric.dto.module.ModuleCreateDTO;
import com.migrametric.dto.module.ModuleQueryDTO;
import com.migrametric.dto.module.ModuleUpdateDTO;
import com.migrametric.entity.module.Module;
import com.migrametric.entity.system.SystemType;
import com.migrametric.mapper.module.ModuleMapper;
import com.migrametric.mapper.system.SystemTypeMapper;
import com.migrametric.service.module.ModuleService;
import com.migrametric.vo.module.ModuleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模块服务实现类
 *
 * @author MigraMetric Team
 */
@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final ModuleMapper moduleMapper;
    private final SystemTypeMapper systemTypeMapper;

    /**
     * 状态常量
     */
    private static final int STATUS_DISABLED = 0;   // 禁用
    private static final int STATUS_ENABLED = 1;   // 启用

    @Override
    public PageResult<ModuleVO> queryPage(ModuleQueryDTO queryDTO) {
        // 构建分页条件
        Page<Module> page = new Page<>(
                queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1,
                queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10
        );

        // 构建查询条件
        LambdaQueryWrapper<Module> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getModuleName())) {
            wrapper.like(Module::getModuleName, queryDTO.getModuleName());
        }
        if (queryDTO.getSystemId() != null) {
            wrapper.eq(Module::getSystemId, queryDTO.getSystemId());
        }
        if (StringUtils.hasText(queryDTO.getCategory())) {
            wrapper.eq(Module::getCategory, queryDTO.getCategory());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Module::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(Module::getCreateTime);

        // 执行分页查询
        IPage<Module> resultPage = moduleMapper.selectPage(page, wrapper);

        // 转换为VO
        List<ModuleVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, resultPage.getTotal(),
                (int) resultPage.getCurrent(), (int) resultPage.getSize());
    }

    @Override
    public ModuleVO getById(Long id) {
        Module module = moduleMapper.selectById(id);
        if (module == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "模块不存在");
        }
        return convertToVO(module);
    }

    @Override
    public Long create(ModuleCreateDTO createDTO) {
        // 检查系统是否存在
        validateSystem(createDTO.getSystemId());

        // 检查模块名称是否已存在
        validateModuleName(createDTO.getModuleName(), null);

        // 创建实体
        Module module = new Module();
        module.setModuleName(createDTO.getModuleName());
        module.setSystemId(createDTO.getSystemId());
        module.setCategory(createDTO.getCategory());
        module.setBaseWorkload(createDTO.getBaseWorkload());
        module.setDefaultWeight(createDTO.getDefaultWeight());
        module.setDescription(createDTO.getDescription());
        module.setStatus(STATUS_ENABLED);
        module.setCreateTime(LocalDateTime.now());
        module.setCreateBy("admin"); // TODO: 从上下文获取

        moduleMapper.insert(module);

        return module.getId();
    }

    @Override
    public void update(ModuleUpdateDTO updateDTO) {
        // 检查模块是否存在
        Module module = moduleMapper.selectById(updateDTO.getId());
        if (module == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "模块不存在");
        }

        // 检查系统是否存在
        validateSystem(updateDTO.getSystemId());

        // 检查模块名称是否已存在（排除自身）
        validateModuleName(updateDTO.getModuleName(), updateDTO.getId());

        // 更新实体
        module.setModuleName(updateDTO.getModuleName());
        module.setSystemId(updateDTO.getSystemId());
        module.setCategory(updateDTO.getCategory());
        module.setBaseWorkload(updateDTO.getBaseWorkload());
        module.setDefaultWeight(updateDTO.getDefaultWeight());
        module.setDescription(updateDTO.getDescription());
        module.setUpdateTime(LocalDateTime.now());
        module.setUpdateBy("admin"); // TODO: 从上下文获取

        moduleMapper.updateById(module);
    }

    @Override
    public void delete(Long id) {
        // 检查模块是否存在
        Module module = moduleMapper.selectById(id);
        if (module == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "模块不存在");
        }

        // 检查是否被项目引用
        if (moduleMapper.countByProjectReference(id) > 0) {
            throw new BusinessException(ResultCode.DATA_REFERENCE_EXISTS, "该模块已被项目引用，无法删除");
        }

        moduleMapper.deleteById(id);
    }

    @Override
    public void enable(Long id) {
        Module module = moduleMapper.selectById(id);
        if (module == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "模块不存在");
        }

        module.setStatus(STATUS_ENABLED);
        module.setUpdateTime(LocalDateTime.now());
        module.setUpdateBy("admin");

        moduleMapper.updateById(module);
    }

    @Override
    public void disable(Long id) {
        Module module = moduleMapper.selectById(id);
        if (module == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "模块不存在");
        }

        module.setStatus(STATUS_DISABLED);
        module.setUpdateTime(LocalDateTime.now());
        module.setUpdateBy("admin");

        moduleMapper.updateById(module);
    }

    @Override
    public List<ModuleVO> listEnabled(Long systemId) {
        LambdaQueryWrapper<Module> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Module::getStatus, STATUS_ENABLED);

        if (systemId != null) {
            wrapper.eq(Module::getSystemId, systemId);
        }

        wrapper.orderByAsc(Module::getCategory);
        wrapper.orderByAsc(Module::getModuleName);

        List<Module> list = moduleMapper.selectList(wrapper);

        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listCategories(Long systemId) {
        LambdaQueryWrapper<Module> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Module::getStatus, STATUS_ENABLED);
        wrapper.isNotNull(Module::getCategory);
        wrapper.ne(Module::getCategory, "");

        if (systemId != null) {
            wrapper.eq(Module::getSystemId, systemId);
        }

        wrapper.select(Module::getCategory);
        wrapper.groupBy(Module::getCategory);
        wrapper.orderByAsc(Module::getCategory);

        List<Module> list = moduleMapper.selectList(wrapper);

        return list.stream()
                .map(Module::getCategory)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 验证系统是否存在
     */
    private void validateSystem(Long systemId) {
        SystemType systemType = systemTypeMapper.selectById(systemId);
        if (systemType == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "所属系统不存在");
        }
        if (systemType.getStatus() != STATUS_ENABLED) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "所属系统已被禁用");
        }
    }

    /**
     * 验证模块名称是否已存在
     */
    private void validateModuleName(String moduleName, Long excludeId) {
        int count = moduleMapper.countByModuleNameExcludeId(moduleName, excludeId);

        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "模块名称已存在");
        }
    }

    /**
     * 转换为VO
     */
    private ModuleVO convertToVO(Module module) {
        ModuleVO vo = new ModuleVO();
        vo.setId(module.getId());
        vo.setModuleName(module.getModuleName());
        vo.setSystemId(module.getSystemId());
        vo.setCategory(module.getCategory());
        vo.setBaseWorkload(module.getBaseWorkload());
        vo.setDefaultWeight(module.getDefaultWeight());
        vo.setDescription(module.getDescription());
        vo.setStatus(module.getStatus());
        vo.setStatusText(getStatusText(module.getStatus()));
        vo.setCreateTime(module.getCreateTime());
        vo.setCreateBy(module.getCreateBy());

        // 填充系统信息
        fillSystemInfo(vo, module.getSystemId());

        return vo;
    }

    /**
     * 填充系统信息
     */
    private void fillSystemInfo(ModuleVO vo, Long systemId) {
        if (systemId != null) {
            SystemType systemType = systemTypeMapper.selectById(systemId);
            if (systemType != null) {
                vo.setSystemName(systemType.getSystemName());
                vo.setSystemCategory(systemType.getSystemCategory());
            }
        }
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case STATUS_ENABLED -> "启用";
            case STATUS_DISABLED -> "禁用";
            default -> "未知";
        };
    }
}
