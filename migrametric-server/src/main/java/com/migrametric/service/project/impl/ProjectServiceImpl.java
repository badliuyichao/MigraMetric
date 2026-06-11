package com.migrametric.service.project.impl;

import com.migrametric.service.project.ProjectEvent;
import com.migrametric.service.project.ProjectStateMachine;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.migrametric.common.BusinessException;
import com.migrametric.common.PageResult;
import com.migrametric.common.ResultCode;
import com.migrametric.dto.project.ProjectCreateDTO;
import com.migrametric.dto.project.ProjectQueryDTO;
import com.migrametric.entity.project.Project;
import com.migrametric.entity.system.SystemType;
import com.migrametric.mapper.project.ProjectMapper;
import com.migrametric.mapper.system.SystemTypeMapper;
import com.migrametric.service.evaluation.EvaluationService;
import com.migrametric.service.project.ProjectService;
import com.migrametric.vo.evaluation.EvaluationVO;
import com.migrametric.vo.project.ProjectDetailVO;
import com.migrametric.vo.project.ProjectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.migrametric.context.UserContext;
import com.migrametric.dto.project.ProjectUpdateDTO;

/**
 * 项目服务实现类
 *
 * @author MigraMetric Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final SystemTypeMapper systemTypeMapper;
    private final EvaluationService evaluationService;
    private final ProjectStateMachine projectStateMachine;

    /**
     * 项目状态常量
     */
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_ARCHIVED = "ARCHIVED";

    @Override
    @Transactional
    public Long create(ProjectCreateDTO createDTO) {
        // 校验源系统和目标系统不能相同
        if (createDTO.getSourceSystemId().equals(createDTO.getTargetSystemId())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "源系统和目标系统不能相同");
        }

        // 校验源系统是否存在且启用
        SystemType sourceSystem = systemTypeMapper.selectById(createDTO.getSourceSystemId());
        if (sourceSystem == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "源系统不存在");
        }
        if (sourceSystem.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "源系统已被禁用");
        }

        // 校验目标系统是否存在且启用
        SystemType targetSystem = systemTypeMapper.selectById(createDTO.getTargetSystemId());
        if (targetSystem == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "目标系统不存在");
        }
        if (targetSystem.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "目标系统已被禁用");
        }

        // 创建项目实体
        Project project = new Project();
        project.setProjectName(createDTO.getProjectName());
        project.setCustomerName(createDTO.getCustomerName());
        project.setSourceSystemId(createDTO.getSourceSystemId());
        project.setTargetSystemId(createDTO.getTargetSystemId());
        project.setProjectLeader(createDTO.getProjectLeader());
        project.setContact(createDTO.getContact());
        project.setDescription(createDTO.getDescription());
        project.setEvaluationDate(createDTO.getEvaluationDate());
        project.setStatus(STATUS_DRAFT);
        project.setUserId(UserContext.getCurrentUserId());
        project.setCreateTime(LocalDateTime.now());
        project.setCreateBy(UserContext.getCurrentUsername());

        projectMapper.insert(project);
        // 写首条 CREATE 历史（§3.2.4）
        projectStateMachine.recordHistory(project, null, com.migrametric.entity.project.ProjectStatus.DRAFT,
                com.migrametric.service.project.ProjectEvent.CREATE, false);
        log.info("创建项目成功: id={}, name={}", project.getId(), project.getProjectName());

        return project.getId();
    }

    @Override
    public PageResult<ProjectVO> queryPage(ProjectQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();

        // 项目名称模糊查询
        if (StringUtils.hasText(queryDTO.getProjectName())) {
            wrapper.like(Project::getProjectName, queryDTO.getProjectName());
        }

        // 客户名称模糊查询
        if (StringUtils.hasText(queryDTO.getCustomerName())) {
            wrapper.like(Project::getCustomerName, queryDTO.getCustomerName());
        }

        // 源系统ID筛选
        if (queryDTO.getSourceSystemId() != null) {
            wrapper.eq(Project::getSourceSystemId, queryDTO.getSourceSystemId());
        }

        // 目标系统ID筛选
        if (queryDTO.getTargetSystemId() != null) {
            wrapper.eq(Project::getTargetSystemId, queryDTO.getTargetSystemId());
        }

        // 状态筛选
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(Project::getStatus, queryDTO.getStatus());
        }

        // 按创建时间降序排序
        wrapper.orderByDesc(Project::getCreateTime);

        // 分页查询
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        IPage<Project> page = new Page<>(pageNum, pageSize);
        IPage<Project> result = projectMapper.selectPage(page, wrapper);

        // 批量查询系统信息（解决N+1问题）
        Set<Long> systemIds = result.getRecords().stream()
                .flatMap(p -> Stream.of(p.getSourceSystemId(), p.getTargetSystemId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, SystemType> systemMap = systemIds.isEmpty()
                ? Collections.emptyMap()
                : systemTypeMapper.selectBatchIds(systemIds).stream()
                        .collect(Collectors.toMap(SystemType::getId, s -> s));

        // 转换为VO
        List<ProjectVO> voList = result.getRecords().stream()
                .map(p -> convertToVO(p, systemMap))
                .toList();

        return new PageResult<>(voList, result.getTotal(), pageNum, pageSize);
    }

    @Override
    public ProjectVO getById(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        return convertToVO(project);
    }

    @Override
    public ProjectDetailVO getProjectDetail(Long id) {
        // 查询项目基本信息
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }

        // 构建项目详情VO
        ProjectDetailVO detailVO = new ProjectDetailVO();
        detailVO.setId(project.getId());
        detailVO.setProjectName(project.getProjectName());
        detailVO.setCustomerName(project.getCustomerName());
        detailVO.setSourceSystemId(project.getSourceSystemId());
        detailVO.setTargetSystemId(project.getTargetSystemId());
        detailVO.setProjectLeader(project.getProjectLeader());
        detailVO.setContact(project.getContact());
        detailVO.setDescription(project.getDescription());
        detailVO.setEvaluationDate(project.getEvaluationDate() != null ? project.getEvaluationDate().toString() : null);
        detailVO.setStatus(project.getStatus());
        detailVO.setStatusText(getStatusText(project.getStatus()));
        detailVO.setUserId(project.getUserId());
        detailVO.setCreateByName(project.getCreateBy());
        detailVO.setCreateTime(project.getCreateTime() != null ? project.getCreateTime().toString() : null);
        detailVO.setUpdateTime(project.getUpdateTime() != null ? project.getUpdateTime().toString() : null);

        // 查询源系统名称
        if (project.getSourceSystemId() != null) {
            SystemType sourceSystem = systemTypeMapper.selectById(project.getSourceSystemId());
            if (sourceSystem != null) {
                detailVO.setSourceSystemName(sourceSystem.getSystemName());
            }
        }

        // 查询目标系统名称
        if (project.getTargetSystemId() != null) {
            SystemType targetSystem = systemTypeMapper.selectById(project.getTargetSystemId());
            if (targetSystem != null) {
                detailVO.setTargetSystemName(targetSystem.getSystemName());
            }
        }

        // 查询评估概况
        EvaluationVO evaluationVO = evaluationService.getByProjectId(id);
        if (evaluationVO != null) {
            detailVO.setHasEvaluation(true);
            detailVO.setSelectedModuleCount(evaluationService.countSelectedModules(id));
            detailVO.setTotalWorkload(evaluationVO.getTotalWorkload());
            detailVO.setCoreWorkload(evaluationVO.getCoreWorkload());
            detailVO.setReportWorkload(evaluationVO.getReportWorkload());
            detailVO.setCustomDevWorkload(evaluationVO.getCustomDevWorkload());
            detailVO.setDataVolume(evaluationVO.getDataVolume());
            detailVO.setUserCount(evaluationVO.getUserCount());
            detailVO.setReportCount(evaluationVO.getReportCount());
            detailVO.setTableCount(evaluationVO.getTableCount());
            detailVO.setEvaluationStatus(evaluationVO.getEvaluationStatus());
            detailVO.setEvaluationStatusText(evaluationVO.getEvaluationStatusText());
            detailVO.setEvaluationTime(evaluationVO.getEvaluationTime() != null ? evaluationVO.getEvaluationTime().toString() : null);
        } else {
            detailVO.setHasEvaluation(false);
            detailVO.setSelectedModuleCount(0);
            detailVO.setTotalWorkload(null);
            detailVO.setCoreWorkload(null);
            detailVO.setReportWorkload(null);
            detailVO.setCustomDevWorkload(null);
            detailVO.setDataVolume(null);
            detailVO.setUserCount(null);
            detailVO.setReportCount(null);
            detailVO.setTableCount(null);
            detailVO.setEvaluationStatus(null);
            detailVO.setEvaluationStatusText(null);
            detailVO.setEvaluationTime(null);
        }

        return detailVO;
    }

    /**
     * 转换为VO（使用预查询的Map，解决N+1问题）
     */
    private ProjectVO convertToVO(Project project, Map<Long, SystemType> systemMap) {
        ProjectVO vo = new ProjectVO();
        vo.setId(project.getId());
        vo.setProjectName(project.getProjectName());
        vo.setCustomerName(project.getCustomerName());
        vo.setSourceSystemId(project.getSourceSystemId());
        vo.setTargetSystemId(project.getTargetSystemId());
        vo.setProjectLeader(project.getProjectLeader());
        vo.setContact(project.getContact());
        vo.setDescription(project.getDescription());
        vo.setEvaluationDate(project.getEvaluationDate());
        vo.setStatus(project.getStatus());
        vo.setStatusText(getStatusText(project.getStatus()));
        vo.setUserId(project.getUserId());
        vo.setCreateTime(project.getCreateTime());
        vo.setUpdateTime(project.getUpdateTime());
        vo.setCreateByName(project.getCreateBy());

        // 从Map获取系统名称
        if (project.getSourceSystemId() != null) {
            SystemType sourceSystem = systemMap.get(project.getSourceSystemId());
            if (sourceSystem != null) {
                vo.setSourceSystemName(sourceSystem.getSystemName());
            }
        }

        if (project.getTargetSystemId() != null) {
            SystemType targetSystem = systemMap.get(project.getTargetSystemId());
            if (targetSystem != null) {
                vo.setTargetSystemName(targetSystem.getSystemName());
            }
        }

        return vo;
    }

    /**
     * 转换为VO（单条查询使用）
     */
    private ProjectVO convertToVO(Project project) {
        ProjectVO vo = new ProjectVO();
        vo.setId(project.getId());
        vo.setProjectName(project.getProjectName());
        vo.setCustomerName(project.getCustomerName());
        vo.setSourceSystemId(project.getSourceSystemId());
        vo.setTargetSystemId(project.getTargetSystemId());
        vo.setProjectLeader(project.getProjectLeader());
        vo.setContact(project.getContact());
        vo.setDescription(project.getDescription());
        vo.setEvaluationDate(project.getEvaluationDate());
        vo.setStatus(project.getStatus());
        vo.setStatusText(getStatusText(project.getStatus()));
        vo.setUserId(project.getUserId());
        vo.setCreateTime(project.getCreateTime());
        vo.setUpdateTime(project.getUpdateTime());
        vo.setCreateByName(project.getCreateBy());

        // 查询源系统名称
        if (project.getSourceSystemId() != null) {
            SystemType sourceSystem = systemTypeMapper.selectById(project.getSourceSystemId());
            if (sourceSystem != null) {
                vo.setSourceSystemName(sourceSystem.getSystemName());
            }
        }

        // 查询目标系统名称
        if (project.getTargetSystemId() != null) {
            SystemType targetSystem = systemTypeMapper.selectById(project.getTargetSystemId());
            if (targetSystem != null) {
                vo.setTargetSystemName(targetSystem.getSystemName());
            }
        }

        return vo;
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(String status) {
        if (status == null) {
            return "";
        }
        try {
            return com.migrametric.entity.project.ProjectStatus.fromCode(status).getText();
        } catch (IllegalArgumentException e) {
            return "未知";
        }
    }

    @Override
    @Transactional
    public void update(Long id, ProjectUpdateDTO updateDTO) {
        // 查询项目
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }

        // 已归档的项目不可编辑
        if (STATUS_ARCHIVED.equals(project.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "已归档的项目不可编辑");
        }

        // 校验源系统和目标系统不能相同
        if (updateDTO.getSourceSystemId() != null && updateDTO.getTargetSystemId() != null
                && updateDTO.getSourceSystemId().equals(updateDTO.getTargetSystemId())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "源系统和目标系统不能相同");
        }

        // 校验源系统
        if (updateDTO.getSourceSystemId() != null) {
            SystemType sourceSystem = systemTypeMapper.selectById(updateDTO.getSourceSystemId());
            if (sourceSystem == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "源系统不存在");
            }
            if (sourceSystem.getStatus() != 1) {
                throw new BusinessException(ResultCode.PARAM_INVALID, "源系统已被禁用");
            }
        }

        // 校验目标系统
        if (updateDTO.getTargetSystemId() != null) {
            SystemType targetSystem = systemTypeMapper.selectById(updateDTO.getTargetSystemId());
            if (targetSystem == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "目标系统不存在");
            }
            if (targetSystem.getStatus() != 1) {
                throw new BusinessException(ResultCode.PARAM_INVALID, "目标系统已被禁用");
            }
        }

        // 更新字段
        if (updateDTO.getProjectName() != null) {
            project.setProjectName(updateDTO.getProjectName());
        }
        if (updateDTO.getCustomerName() != null) {
            project.setCustomerName(updateDTO.getCustomerName());
        }
        if (updateDTO.getSourceSystemId() != null) {
            project.setSourceSystemId(updateDTO.getSourceSystemId());
        }
        if (updateDTO.getTargetSystemId() != null) {
            project.setTargetSystemId(updateDTO.getTargetSystemId());
        }
        if (updateDTO.getProjectLeader() != null) {
            project.setProjectLeader(updateDTO.getProjectLeader());
        }
        if (updateDTO.getContact() != null) {
            project.setContact(updateDTO.getContact());
        }
        if (updateDTO.getDescription() != null) {
            project.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getEvaluationDate() != null) {
            project.setEvaluationDate(updateDTO.getEvaluationDate());
        }

        project.setUpdateTime(LocalDateTime.now());
        project.setUpdateBy(UserContext.getCurrentUsername());

        projectMapper.updateById(project);
        log.info("更新项目成功: id={}", id);
    }

    @Override
    @Transactional
    public Long copy(Long id) {
        // 查询原项目
        Project original = projectMapper.selectById(id);
        if (original == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }

        // 创建副本
        Project copy = new Project();
        copy.setProjectName(original.getProjectName() + "_副本");
        copy.setCustomerName(original.getCustomerName());
        copy.setSourceSystemId(original.getSourceSystemId());
        copy.setTargetSystemId(original.getTargetSystemId());
        copy.setProjectLeader(original.getProjectLeader());
        copy.setContact(original.getContact());
        copy.setDescription(original.getDescription());
        copy.setEvaluationDate(original.getEvaluationDate());
        copy.setStatus(STATUS_DRAFT);
        copy.setUserId(UserContext.getCurrentUserId());
        copy.setCreateTime(LocalDateTime.now());
        copy.setCreateBy(UserContext.getCurrentUsername());

        projectMapper.insert(copy);
        log.info("复制项目成功: 原项目id={}, 新项目id={}, name={}", id, copy.getId(), copy.getProjectName());

        return copy.getId();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 查询项目
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }

        // 只有草稿状态可以删除
        if (!STATUS_DRAFT.equals(project.getStatus())) {
            throw new BusinessException(ResultCode.PROJECT_NOT_DRAFT);
        }

        // 软删除（MyBatis-Plus自动处理）
        projectMapper.deleteById(id);
        log.info("删除项目成功: id={}, name={}", id, project.getProjectName());
    }

    @Override
    @Transactional
    public void archive(Long id) {
        // 走状态机：自动校验"只有 COMPLETED 可归档" + 更新
        projectStateMachine.transition(id, ProjectEvent.ARCHIVE);
        // 状态机不更新 updateBy，这里补一下
        Project project = projectMapper.selectById(id);
        if (project != null) {
            project.setUpdateBy(UserContext.getCurrentUsername());
            projectMapper.updateById(project);
        }

        log.info("归档项目成功: id={}, name={}", id, project != null ? project.getProjectName() : "未知");
    }
}
