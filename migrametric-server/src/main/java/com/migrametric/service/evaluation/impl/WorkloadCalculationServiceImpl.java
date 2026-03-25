package com.migrametric.service.evaluation.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.migrametric.common.BusinessException;
import com.migrametric.common.ResultCode;
import com.migrametric.entity.evaluation.Evaluation;
import com.migrametric.entity.evaluation.ProjectModuleConfig;
import com.migrametric.entity.ladder.DataVolumeLadder;
import com.migrametric.entity.ladder.UserCountLadder;
import com.migrametric.entity.module.Module;
import com.migrametric.mapper.evaluation.EvaluationMapper;
import com.migrametric.mapper.evaluation.ProjectModuleConfigMapper;
import com.migrametric.mapper.ladder.DataVolumeLadderMapper;
import com.migrametric.mapper.ladder.UserCountLadderMapper;
import com.migrametric.mapper.module.ModuleMapper;
import com.migrametric.service.config.ReportConfigService;
import com.migrametric.service.evaluation.ModuleConfigService;
import com.migrametric.service.evaluation.WorkloadCalculationService;
import com.migrametric.vo.evaluation.ModuleWorkloadVO;
import com.migrametric.vo.evaluation.WorkloadResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 工作量计算服务实现类
 * <p>
 * 实现工作量计算的核心业务逻辑，包括：
 * - 核心迁移工作量计算：Σ(基础人天 × 加权系数 × 数据量系数 × 用户数系数)
 * - 报表工作量计算：报表数量 × 报表系数
 * - 客开工作量计算：客开人天（手动输入）
 * - 总工作量计算：核心迁移工作量 + 报表工作量 + 客开工作量
 * </p>
 *
 * @author MigraMetric Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkloadCalculationServiceImpl implements WorkloadCalculationService {

    private final EvaluationMapper evaluationMapper;
    private final ProjectModuleConfigMapper moduleConfigMapper;
    private final ModuleMapper moduleMapper;
    private final DataVolumeLadderMapper dataVolumeLadderMapper;
    private final UserCountLadderMapper userCountLadderMapper;
    private final ReportConfigService reportConfigService;
    private final ModuleConfigService moduleConfigService;

    /**
     * 报表系数配置键
     */
    private static final String REPORT_COEFFICIENT_KEY = "report_workload_coefficient";

    /**
     * 默认报表系数
     */
    private static final BigDecimal DEFAULT_REPORT_COEFFICIENT = new BigDecimal("0.5");

    /**
     * 计算精度（小数位数）
     */
    private static final int SCALE = 2;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkloadResultVO calculateWorkload(Long projectId) {
        Assert.notNull(projectId, "项目ID不能为空");

        log.info("开始计算工作量, projectId: {}", projectId);

        // 1. 查询评估记录
        Evaluation evaluation = getEvaluationByProjectId(projectId);
        if (evaluation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "评估记录不存在");
        }

        // 2. 获取数据量系数和用户数系数
        BigDecimal dataVolumeWeight = getDataVolumeWeight(evaluation.getDataVolumeLadderId());
        BigDecimal userCountWeight = getUserCountWeight(evaluation.getUserCountLadderId());

        // 3. 获取报表系数
        BigDecimal reportCoefficient = reportConfigService.getValueAsBigDecimal(
                REPORT_COEFFICIENT_KEY, DEFAULT_REPORT_COEFFICIENT);

        // 4. 获取已配置的模块列表
        List<ProjectModuleConfig> configs = getModuleConfigs(projectId);
        if (configs.isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "请至少选择一个模块");
        }

        // 5. 构建模块工作量列表
        List<ModuleWorkloadVO> moduleWorkloads = buildModuleWorkloads(configs, dataVolumeWeight, userCountWeight);

        // 6. 计算核心迁移工作量
        BigDecimal coreWorkload = calculateCoreWorkload(moduleWorkloads);

        // 7. 计算报表工作量
        BigDecimal reportWorkload = calculateReportWorkload(evaluation.getReportCount());

        // 8. 计算客开工作量
        BigDecimal customDevWorkload = calculateCustomDevWorkload(
                evaluation.getHasCustomDev() != null && evaluation.getHasCustomDev() == 1,
                evaluation.getCustomDevWorkload());

        // 9. 计算总工作量
        BigDecimal totalWorkload = calculateTotalWorkload(coreWorkload, reportWorkload, customDevWorkload);

        // 10. 构建结果
        WorkloadResultVO result = new WorkloadResultVO();
        result.setProjectId(projectId);
        result.setCoreWorkload(coreWorkload);
        result.setReportWorkload(reportWorkload);
        result.setCustomDevWorkload(customDevWorkload);
        result.setTotalWorkload(totalWorkload);
        result.setModuleWorkloads(moduleWorkloads);
        result.setDataVolumeWeight(dataVolumeWeight);
        result.setUserCountWeight(userCountWeight);
        result.setReportCoefficient(reportCoefficient);

        // 11. 更新评估记录的工作量
        updateEvaluationWorkload(evaluation, coreWorkload, reportWorkload, totalWorkload);

        log.info("工作量计算完成, projectId: {}, 总工作量: {} 人天", projectId, totalWorkload);

        return result;
    }

    @Override
    public List<ModuleWorkloadVO> calculateModuleWorkloads(List<ModuleWorkloadVO> modules,
                                                           BigDecimal dataVolumeWeight,
                                                           BigDecimal userCountWeight) {
        if (modules == null || modules.isEmpty()) {
            return new ArrayList<>();
        }

        BigDecimal dvWeight = dataVolumeWeight != null ? dataVolumeWeight : BigDecimal.ONE;
        BigDecimal ucWeight = userCountWeight != null ? userCountWeight : BigDecimal.ONE;

        for (ModuleWorkloadVO module : modules) {
            BigDecimal baseWorkload = module.getBaseWorkload() != null ? module.getBaseWorkload() : BigDecimal.ZERO;
            BigDecimal weight = module.getWeight() != null ? module.getWeight() : BigDecimal.ONE;

            // 模块工作量 = 基础人天 × 加权系数 × 数据量系数 × 用户数系数
            BigDecimal moduleWorkload = baseWorkload
                    .multiply(weight)
                    .multiply(dvWeight)
                    .multiply(ucWeight)
                    .setScale(SCALE, RoundingMode.HALF_UP);

            module.setDataVolumeWeight(dvWeight);
            module.setUserCountWeight(ucWeight);
            module.setModuleWorkload(moduleWorkload);
        }

        return modules;
    }

    @Override
    public BigDecimal calculateCoreWorkload(List<ModuleWorkloadVO> moduleWorkloads) {
        if (moduleWorkloads == null || moduleWorkloads.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return moduleWorkloads.stream()
                .map(ModuleWorkloadVO::getModuleWorkload)
                .filter(workload -> workload != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateReportWorkload(Integer reportCount) {
        if (reportCount == null || reportCount <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal coefficient = reportConfigService.getValueAsBigDecimal(
                REPORT_COEFFICIENT_KEY, DEFAULT_REPORT_COEFFICIENT);

        return BigDecimal.valueOf(reportCount)
                .multiply(coefficient)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateCustomDevWorkload(Boolean hasCustomDev, BigDecimal customDevWorkload) {
        if (!Boolean.TRUE.equals(hasCustomDev) || customDevWorkload == null) {
            return BigDecimal.ZERO;
        }
        return customDevWorkload.setScale(SCALE, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTotalWorkload(BigDecimal coreWorkload,
                                             BigDecimal reportWorkload,
                                             BigDecimal customDevWorkload) {
        BigDecimal core = coreWorkload != null ? coreWorkload : BigDecimal.ZERO;
        BigDecimal report = reportWorkload != null ? reportWorkload : BigDecimal.ZERO;
        BigDecimal custom = customDevWorkload != null ? customDevWorkload : BigDecimal.ZERO;

        return core.add(report).add(custom).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 获取评估记录
     */
    private Evaluation getEvaluationByProjectId(Long projectId) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getProjectId, projectId);
        return evaluationMapper.selectOne(wrapper);
    }

    /**
     * 获取数据量系数
     */
    private BigDecimal getDataVolumeWeight(Long ladderId) {
        if (ladderId == null) {
            return BigDecimal.ONE;
        }
        DataVolumeLadder ladder = dataVolumeLadderMapper.selectById(ladderId);
        return ladder != null ? ladder.getWeight() : BigDecimal.ONE;
    }

    /**
     * 获取用户数系数
     */
    private BigDecimal getUserCountWeight(Long ladderId) {
        if (ladderId == null) {
            return BigDecimal.ONE;
        }
        UserCountLadder ladder = userCountLadderMapper.selectById(ladderId);
        return ladder != null ? ladder.getWeight() : BigDecimal.ONE;
    }

    /**
     * 获取已配置的模块列表
     */
    private List<ProjectModuleConfig> getModuleConfigs(Long projectId) {
        LambdaQueryWrapper<ProjectModuleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectModuleConfig::getProjectId, projectId);
        return moduleConfigMapper.selectList(wrapper);
    }

    /**
     * 构建模块工作量列表
     */
    private List<ModuleWorkloadVO> buildModuleWorkloads(List<ProjectModuleConfig> configs,
                                                         BigDecimal dataVolumeWeight,
                                                         BigDecimal userCountWeight) {
        if (configs.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取模块信息
        List<Long> moduleIds = configs.stream()
                .map(ProjectModuleConfig::getModuleId)
                .collect(Collectors.toList());
        List<Module> modules = moduleMapper.selectBatchIds(moduleIds);
        Map<Long, Module> moduleMap = modules.stream()
                .collect(Collectors.toMap(Module::getId, Function.identity()));

        // 构建模块工作量列表
        List<ModuleWorkloadVO> moduleWorkloads = new ArrayList<>();
        for (ProjectModuleConfig config : configs) {
            Module module = moduleMap.get(config.getModuleId());
            if (module == null) {
                continue;
            }

            ModuleWorkloadVO vo = new ModuleWorkloadVO();
            vo.setModuleId(module.getId());
            vo.setModuleName(module.getModuleName());
            vo.setCategory(module.getCategory());
            vo.setBaseWorkload(module.getBaseWorkload());
            vo.setWeight(config.getWeight() != null ? config.getWeight() : module.getDefaultWeight());

            moduleWorkloads.add(vo);
        }

        // 计算每个模块的工作量
        return calculateModuleWorkloads(moduleWorkloads, dataVolumeWeight, userCountWeight);
    }

    /**
     * 更新评估记录的工作量
     */
    private void updateEvaluationWorkload(Evaluation evaluation,
                                          BigDecimal coreWorkload,
                                          BigDecimal reportWorkload,
                                          BigDecimal totalWorkload) {
        evaluation.setCoreWorkload(coreWorkload);
        evaluation.setReportWorkload(reportWorkload);
        evaluation.setTotalWorkload(totalWorkload);
        evaluation.setUpdateTime(LocalDateTime.now());
        evaluationMapper.updateById(evaluation);
    }
}
