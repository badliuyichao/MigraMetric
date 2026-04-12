package com.migrametric.service.evaluation.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.migrametric.common.BusinessException;
import com.migrametric.common.ResultCode;
import com.migrametric.dto.evaluation.EvaluationCreateDTO;
import com.migrametric.dto.evaluation.EvaluationUpdateDTO;
import com.migrametric.entity.evaluation.Evaluation;
import com.migrametric.entity.evaluation.ProjectModuleConfig;
import com.migrametric.entity.ladder.DataVolumeLadder;
import com.migrametric.entity.ladder.UserCountLadder;
import com.migrametric.entity.project.Project;
import com.migrametric.mapper.evaluation.EvaluationMapper;
import com.migrametric.mapper.evaluation.ProjectModuleConfigMapper;
import com.migrametric.mapper.ladder.DataVolumeLadderMapper;
import com.migrametric.mapper.ladder.UserCountLadderMapper;
import com.migrametric.mapper.project.ProjectMapper;
import com.migrametric.service.evaluation.EvaluationService;
import com.migrametric.vo.evaluation.EvaluationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 评估服务实现类
 *
 * @author MigraMetric Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationMapper evaluationMapper;
    private final ProjectModuleConfigMapper moduleConfigMapper;
    private final DataVolumeLadderMapper dataVolumeLadderMapper;
    private final UserCountLadderMapper userCountLadderMapper;
    private final ProjectMapper projectMapper;

    /**
     * 评估状态常量
     */
    private static final String EVAL_STATUS_DRAFT = "DRAFT";
    private static final String EVAL_STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String EVAL_STATUS_COMPLETED = "COMPLETED";

    @Override
    public EvaluationVO getByProjectId(Long projectId) {
        // 查询评估记录
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getProjectId, projectId);
        Evaluation evaluation = evaluationMapper.selectOne(wrapper);

        if (evaluation == null) {
            return null;
        }

        return convertToVO(evaluation);
    }

    @Override
    public int countSelectedModules(Long projectId) {
        LambdaQueryWrapper<ProjectModuleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectModuleConfig::getProjectId, projectId);
        return moduleConfigMapper.selectCount(wrapper).intValue();
    }

    /**
     * 转换为VO
     */
    private EvaluationVO convertToVO(Evaluation evaluation) {
        EvaluationVO vo = new EvaluationVO();
        vo.setId(evaluation.getId());
        vo.setProjectId(evaluation.getProjectId());
        vo.setTableCount(evaluation.getTableCount());
        vo.setDataVolume(evaluation.getDataVolume());
        vo.setUserCount(evaluation.getUserCount());
        vo.setReportCount(evaluation.getReportCount());
        vo.setHasCustomDev(evaluation.getHasCustomDev() != null && evaluation.getHasCustomDev() == 1);
        vo.setCustomDevCount(evaluation.getCustomDevCount());
        vo.setCustomDevWorkload(evaluation.getCustomDevWorkload());
        vo.setDataCleanDesc(evaluation.getDataCleanDesc());
        vo.setCoreWorkload(evaluation.getCoreWorkload());
        vo.setReportWorkload(evaluation.getReportWorkload());
        vo.setTotalWorkload(evaluation.getTotalWorkload());
        vo.setEvaluationStatus(evaluation.getEvaluationStatus());
        vo.setEvaluationStatusText(getEvaluationStatusText(evaluation.getEvaluationStatus()));
        vo.setEvaluationTime(evaluation.getEvaluationTime());
        vo.setCreateTime(evaluation.getCreateTime());
        vo.setUpdateTime(evaluation.getUpdateTime());

        // 查询数据量阶梯名称和系数
        if (evaluation.getDataVolumeLadderId() != null) {
            DataVolumeLadder ladder = dataVolumeLadderMapper.selectById(evaluation.getDataVolumeLadderId());
            if (ladder != null) {
                vo.setDataVolumeLadderName(ladder.getLadderName());
                vo.setDataVolumeWeight(ladder.getWeight());
            }
        }

        // 查询用户数阶梯名称和系数
        if (evaluation.getUserCountLadderId() != null) {
            UserCountLadder ladder = userCountLadderMapper.selectById(evaluation.getUserCountLadderId());
            if (ladder != null) {
                vo.setUserCountLadderName(ladder.getLadderName());
                vo.setUserCountWeight(ladder.getWeight());
            }
        }

        // 数据清洗复杂度文本
        vo.setDataCleanComplexityText(getDataCleanComplexityText(evaluation.getDataCleanComplexity()));

        return vo;
    }

    /**
     * 获取评估状态文本
     */
    private String getEvaluationStatusText(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case EVAL_STATUS_DRAFT -> "草稿";
            case EVAL_STATUS_IN_PROGRESS -> "进行中";
            case EVAL_STATUS_COMPLETED -> "已完成";
            default -> "未知";
        };
    }

    /**
     * 获取数据清洗复杂度文本
     */
    private String getDataCleanComplexityText(Integer complexity) {
        if (complexity == null) {
            return "";
        }
        return switch (complexity) {
            case 1 -> "简单";
            case 2 -> "中等";
            case 3 -> "复杂";
            default -> "未知";
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createEvaluation(EvaluationCreateDTO dto) {
        // 校验项目是否存在
        Project project = projectMapper.selectById(dto.getProjectId());
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        // 检查评估是否已存在
        if (existsByProjectId(dto.getProjectId())) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS);
        }

        // 创建评估记录
        Evaluation evaluation = new Evaluation();
        evaluation.setProjectId(dto.getProjectId());
        evaluation.setEvaluationStatus(EVAL_STATUS_DRAFT);
        evaluationMapper.insert(evaluation);

        log.info("创建评估记录成功, projectId: {}, evaluationId: {}", dto.getProjectId(), evaluation.getId());
        return evaluation.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveIndicators(Long projectId, EvaluationUpdateDTO dto) {
        // 校验项目是否存在
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        // 查询或创建评估记录
        Evaluation evaluation = getEvaluationByProjectId(projectId);
        if (evaluation == null) {
            // 创建新的评估记录
            evaluation = new Evaluation();
            evaluation.setProjectId(projectId);
        }

        // 更新评估指标
        evaluation.setTableCount(dto.getTableCount());
        evaluation.setDataVolume(dto.getDataVolume());
        evaluation.setDataVolumeLadderId(dto.getDataVolumeLadderId());
        evaluation.setUserCount(dto.getUserCount());
        evaluation.setUserCountLadderId(dto.getUserCountLadderId());
        evaluation.setReportCount(dto.getReportCount());
        evaluation.setHasCustomDev(Boolean.TRUE.equals(dto.getHasCustomDev()) ? 1 : 0);
        evaluation.setCustomDevCount(dto.getCustomDevCount());
        evaluation.setCustomDevWorkload(dto.getCustomDevWorkload());
        evaluation.setDataCleanDesc(dto.getDataCleanDesc());
        evaluation.setDataCleanComplexity(dto.getDataCleanComplexity());

        // 如果评估不存在，设置为进行中状态
        if (evaluation.getId() == null) {
            evaluation.setEvaluationStatus(EVAL_STATUS_IN_PROGRESS);
            evaluationMapper.insert(evaluation);
            log.info("创建评估记录并保存指标, projectId: {}, evaluationId: {}", projectId, evaluation.getId());
        } else {
            evaluation.setEvaluationStatus(EVAL_STATUS_IN_PROGRESS);
            evaluationMapper.updateById(evaluation);
            log.info("更新评估指标, projectId: {}, evaluationId: {}", projectId, evaluation.getId());
        }
    }

    @Override
    public boolean existsByProjectId(Long projectId) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getProjectId, projectId);
        return evaluationMapper.exists(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeEvaluation(Long projectId) {
        Evaluation evaluation = getEvaluationByProjectId(projectId);
        if (evaluation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "评估记录不存在");
        }

        // 检查是否有工作量
        if (evaluation.getTotalWorkload() == null) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "请先计算工作量");
        }

        // 更新评估状态为已完成
        evaluation.setEvaluationStatus(EVAL_STATUS_COMPLETED);
        evaluation.setEvaluationTime(LocalDateTime.now());
        evaluation.setUpdateTime(LocalDateTime.now());
        evaluationMapper.updateById(evaluation);

        log.info("评估完成, projectId: {}, evaluationId: {}", projectId, evaluation.getId());
    }

    /**
     * 获取评估记录（内部方法）
     */
    private Evaluation getEvaluationByProjectId(Long projectId) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getProjectId, projectId);
        return evaluationMapper.selectOne(wrapper);
    }
}
