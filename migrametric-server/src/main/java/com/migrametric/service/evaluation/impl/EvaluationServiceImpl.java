package com.migrametric.service.evaluation.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.migrametric.entity.evaluation.Evaluation;
import com.migrametric.entity.evaluation.ProjectModuleConfig;
import com.migrametric.entity.ladder.DataVolumeLadder;
import com.migrametric.entity.ladder.UserCountLadder;
import com.migrametric.mapper.evaluation.EvaluationMapper;
import com.migrametric.mapper.evaluation.ProjectModuleConfigMapper;
import com.migrametric.mapper.ladder.DataVolumeLadderMapper;
import com.migrametric.mapper.ladder.UserCountLadderMapper;
import com.migrametric.service.evaluation.EvaluationService;
import com.migrametric.vo.evaluation.EvaluationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        // 查询数据量阶梯名称
        if (evaluation.getDataVolumeLadderId() != null) {
            DataVolumeLadder ladder = dataVolumeLadderMapper.selectById(evaluation.getDataVolumeLadderId());
            if (ladder != null) {
                vo.setDataVolumeLadderName(ladder.getLadderName());
            }
        }

        // 查询用户数阶梯名称
        if (evaluation.getUserCountLadderId() != null) {
            UserCountLadder ladder = userCountLadderMapper.selectById(evaluation.getUserCountLadderId());
            if (ladder != null) {
                vo.setUserCountLadderName(ladder.getLadderName());
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
}
