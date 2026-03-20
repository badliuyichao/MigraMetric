package com.migrametric.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.migrametric.common.BusinessException;
import com.migrametric.common.PageResult;
import com.migrametric.common.ResultCode;
import com.migrametric.dto.system.SystemTypeCreateDTO;
import com.migrametric.dto.system.SystemTypeQueryDTO;
import com.migrametric.dto.system.SystemTypeUpdateDTO;
import com.migrametric.entity.system.SystemType;
import com.migrametric.mapper.system.SystemTypeMapper;
import com.migrametric.service.system.SystemTypeService;
import com.migrametric.vo.system.SystemTypeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统类型服务实现类
 *
 * @author MigraMetric Team
 */
@Service
@RequiredArgsConstructor
public class SystemTypeServiceImpl implements SystemTypeService {

    private final SystemTypeMapper systemTypeMapper;

    /**
     * 系统类别常量
     */
    private static final int CATEGORY_SOURCE = 1;   // 源系统
    private static final int CATEGORY_TARGET = 2;   // 目标系统

    /**
     * 状态常量
     */
    private static final int STATUS_DISABLED = 0;   // 禁用
    private static final int STATUS_ENABLED = 1;   // 启用

    @Override
    public PageResult<SystemTypeVO> queryPage(SystemTypeQueryDTO queryDTO) {
        // 构建分页条件
        Page<SystemType> page = new Page<>(
                queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1,
                queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10
        );

        // 构建查询条件
        LambdaQueryWrapper<SystemType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getSystemName())) {
            wrapper.like(SystemType::getSystemName, queryDTO.getSystemName());
        }
        if (queryDTO.getSystemCategory() != null) {
            wrapper.eq(SystemType::getSystemCategory, queryDTO.getSystemCategory());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SystemType::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(SystemType::getCreateTime);

        // 执行分页查询
        IPage<SystemType> resultPage = systemTypeMapper.selectPage(page, wrapper);

        // 转换为VO
        List<SystemTypeVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, resultPage.getTotal(),
                (int) resultPage.getCurrent(), (int) resultPage.getSize());
    }

    @Override
    public SystemTypeVO getById(Long id) {
        SystemType systemType = systemTypeMapper.selectById(id);
        if (systemType == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "系统类型不存在");
        }
        return convertToVO(systemType);
    }

    @Override
    public Long create(SystemTypeCreateDTO createDTO) {
        // 检查系统名称是否已存在
        validateSystemName(createDTO.getSystemName(), null);

        // 检查系统类别是否有效
        validateSystemCategory(createDTO.getSystemCategory());

        // 创建实体
        SystemType systemType = new SystemType();
        systemType.setSystemName(createDTO.getSystemName());
        systemType.setSystemCategory(createDTO.getSystemCategory());
        systemType.setDescription(createDTO.getDescription());
        systemType.setStatus(STATUS_ENABLED);
        systemType.setCreateTime(LocalDateTime.now());
        systemType.setCreateBy("admin"); // TODO: 从上下文获取
        systemType.setRemark(createDTO.getRemark());

        systemTypeMapper.insert(systemType);

        return systemType.getId();
    }

    @Override
    public void update(SystemTypeUpdateDTO updateDTO) {
        // 检查系统类型是否存在
        SystemType systemType = systemTypeMapper.selectById(updateDTO.getId());
        if (systemType == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "系统类型不存在");
        }

        // 检查系统名称是否已存在（排除自身）
        validateSystemName(updateDTO.getSystemName(), updateDTO.getId());

        // 检查系统类别是否有效
        validateSystemCategory(updateDTO.getSystemCategory());

        // 更新实体
        systemType.setSystemName(updateDTO.getSystemName());
        systemType.setSystemCategory(updateDTO.getSystemCategory());
        systemType.setDescription(updateDTO.getDescription());
        systemType.setUpdateTime(LocalDateTime.now());
        systemType.setUpdateBy("admin"); // TODO: 从上下文获取
        systemType.setRemark(updateDTO.getRemark());

        systemTypeMapper.updateById(systemType);
    }

    @Override
    public void delete(Long id) {
        // 检查系统类型是否存在
        SystemType systemType = systemTypeMapper.selectById(id);
        if (systemType == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "系统类型不存在");
        }

        // 检查是否被模块引用
        if (systemTypeMapper.countByModuleReference(id) > 0) {
            throw new BusinessException(ResultCode.DATA_REFERENCE_EXISTS, "该系统类型已被模块引用，无法删除");
        }

        // 检查是否被项目引用（源系统）
        if (systemTypeMapper.countByProjectSourceReference(id) > 0) {
            throw new BusinessException(ResultCode.DATA_REFERENCE_EXISTS, "该系统类型已被项目引用（源系统），无法删除");
        }

        // 检查是否被项目引用（目标系统）
        if (systemTypeMapper.countByProjectTargetReference(id) > 0) {
            throw new BusinessException(ResultCode.DATA_REFERENCE_EXISTS, "该系统类型已被项目引用（目标系统），无法删除");
        }

        systemTypeMapper.deleteById(id);
    }

    @Override
    public void enable(Long id) {
        SystemType systemType = systemTypeMapper.selectById(id);
        if (systemType == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "系统类型不存在");
        }

        systemType.setStatus(STATUS_ENABLED);
        systemType.setUpdateTime(LocalDateTime.now());
        systemType.setUpdateBy("admin");

        systemTypeMapper.updateById(systemType);
    }

    @Override
    public void disable(Long id) {
        SystemType systemType = systemTypeMapper.selectById(id);
        if (systemType == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "系统类型不存在");
        }

        systemType.setStatus(STATUS_DISABLED);
        systemType.setUpdateTime(LocalDateTime.now());
        systemType.setUpdateBy("admin");

        systemTypeMapper.updateById(systemType);
    }

    @Override
    public List<SystemTypeVO> listEnabled(Integer category) {
        LambdaQueryWrapper<SystemType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemType::getStatus, STATUS_ENABLED);

        if (category != null) {
            wrapper.eq(SystemType::getSystemCategory, category);
        }

        wrapper.orderByAsc(SystemType::getSystemName);

        List<SystemType> list = systemTypeMapper.selectList(wrapper);

        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 验证系统名称是否已存在
     */
    private void validateSystemName(String systemName, Long excludeId) {
        int count;
        if (excludeId != null) {
            count = systemTypeMapper.countBySystemNameExcludeId(systemName, excludeId);
        } else {
            count = systemTypeMapper.countBySystemName(systemName);
        }

        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "系统名称已存在");
        }
    }

    /**
     * 验证系统类别是否有效
     */
    private void validateSystemCategory(Integer category) {
        List<Integer> validCategories = Arrays.asList(CATEGORY_SOURCE, CATEGORY_TARGET);
        if (!validCategories.contains(category)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "系统类别无效");
        }
    }

    /**
     * 转换为VO
     */
    private SystemTypeVO convertToVO(SystemType systemType) {
        SystemTypeVO vo = new SystemTypeVO();
        vo.setId(systemType.getId());
        vo.setSystemName(systemType.getSystemName());
        vo.setSystemCategory(systemType.getSystemCategory());
        vo.setSystemCategoryText(getCategoryText(systemType.getSystemCategory()));
        vo.setDescription(systemType.getDescription());
        vo.setStatus(systemType.getStatus());
        vo.setStatusText(getStatusText(systemType.getStatus()));
        vo.setCreateTime(systemType.getCreateTime());
        vo.setCreateBy(systemType.getCreateBy());
        vo.setRemark(systemType.getRemark());
        return vo;
    }

    /**
     * 获取系统类别文本
     */
    private String getCategoryText(Integer category) {
        if (category == null) {
            return "";
        }
        return switch (category) {
            case CATEGORY_SOURCE -> "源系统";
            case CATEGORY_TARGET -> "目标系统";
            default -> "未知";
        };
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
