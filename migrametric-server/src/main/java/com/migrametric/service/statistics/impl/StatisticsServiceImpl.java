package com.migrametric.service.statistics.impl;

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
import com.migrametric.service.statistics.StatisticsService;
import com.migrametric.vo.statistics.StatisticsResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 统计服务实现类
 * <p>
 * 实现统计分析的核心业务逻辑，包括：
 * - 工作量类型分布统计
 * - 模块工作量对比统计
 * - 多维度评估指标计算
 * - 风险识别与提示
 * </p>
 *
 * @author MigraMetric Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final EvaluationMapper evaluationMapper;
    private final ProjectModuleConfigMapper moduleConfigMapper;
    private final ModuleMapper moduleMapper;
    private final DataVolumeLadderMapper dataVolumeLadderMapper;
    private final UserCountLadderMapper userCountLadderMapper;

    /**
     * 工期计算常量（人天/人月）
     */
    private static final BigDecimal DAYS_PER_MONTH = new BigDecimal("22");

    /**
     * 最大数据量（用于归一化）
     */
    private static final BigDecimal MAX_DATA_VOLUME = new BigDecimal("10000");

    /**
     * 最大用户数（用于归一化）
     */
    private static final BigDecimal MAX_USER_COUNT = new BigDecimal("1000");

    /**
     * 最大模块数（用于归一化）
     */
    private static final int MAX_MODULE_COUNT = 20;

    /**
     * 最大报表数（用于归一化）
     */
    private static final int MAX_REPORT_COUNT = 100;

    /**
     * 高复杂度系数阈值
     */
    private static final BigDecimal HIGH_COMPLEXITY_THRESHOLD = new BigDecimal("1.5");

    @Override
    public StatisticsResultVO getStatistics(Long projectId) {
        log.info("获取项目统计信息, projectId: {}", projectId);

        // 1. 查询评估记录
        Evaluation evaluation = getEvaluationByProjectId(projectId);

        // 2. 获取已配置的模块列表
        List<ProjectModuleConfig> configs = getModuleConfigs(projectId);

        // 3. 获取模块信息
        List<Module> modules = getModules(configs);

        // 4. 获取数据量和用户数阶梯信息
        DataVolumeLadder dataVolumeLadder = getDataVolumeLadder(evaluation.getDataVolumeLadderId());
        UserCountLadder userCountLadder = getUserCountLadder(evaluation.getUserCountLadderId());

        // 5. 构建统计结果
        StatisticsResultVO result = new StatisticsResultVO();
        result.setProjectId(projectId);

        // 5.1 设置总工作量和预估工期
        BigDecimal totalWorkload = evaluation.getTotalWorkload() != null
                ? evaluation.getTotalWorkload() : BigDecimal.ZERO;
        result.setTotalWorkload(totalWorkload);
        BigDecimal estimatedMonths = totalWorkload.divide(DAYS_PER_MONTH, 2, RoundingMode.HALF_UP);
        result.setEstimatedMonths(estimatedMonths);

        // 5.2 设置工作量类型分布
        result.setWorkloadTypeDistribution(buildWorkloadTypeDistribution(evaluation, totalWorkload));

        // 5.3 设置模块工作量对比
        result.setModuleWorkloads(buildModuleWorkloads(configs, modules, evaluation));

        // 5.4 设置多维度评估指标
        result.setMultiDimensionIndicators(buildMultiDimensionIndicators(evaluation, configs.size(), dataVolumeLadder, userCountLadder));

        // 5.5 设置风险提示
        result.setRiskWarnings(buildRiskWarnings(evaluation, configs, modules, dataVolumeLadder, userCountLadder));

        // 5.6 设置评估指标概览
        result.setEvaluationOverview(buildEvaluationOverview(evaluation, configs.size()));

        log.info("统计信息获取完成, projectId: {}, 总工作量: {} 人天", projectId, totalWorkload);

        return result;
    }

    /**
     * 查询评估记录
     */
    private Evaluation getEvaluationByProjectId(Long projectId) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getProjectId, projectId);
        Evaluation evaluation = evaluationMapper.selectOne(wrapper);
        if (evaluation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "评估记录不存在");
        }
        return evaluation;
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
     * 获取模块信息
     */
    private List<Module> getModules(List<ProjectModuleConfig> configs) {
        if (configs.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> moduleIds = configs.stream()
                .map(ProjectModuleConfig::getModuleId)
                .collect(Collectors.toList());
        return moduleMapper.selectBatchIds(moduleIds);
    }

    /**
     * 获取数据量阶梯
     */
    private DataVolumeLadder getDataVolumeLadder(Long ladderId) {
        if (ladderId == null) {
            return null;
        }
        return dataVolumeLadderMapper.selectById(ladderId);
    }

    /**
     * 获取用户数阶梯
     */
    private UserCountLadder getUserCountLadder(Long ladderId) {
        if (ladderId == null) {
            return null;
        }
        return userCountLadderMapper.selectById(ladderId);
    }

    /**
     * 构建工作量类型分布
     */
    private List<StatisticsResultVO.WorkloadTypeDistribution> buildWorkloadTypeDistribution(
            Evaluation evaluation, BigDecimal totalWorkload) {
        List<StatisticsResultVO.WorkloadTypeDistribution> distribution = new ArrayList<>();

        BigDecimal coreWorkload = evaluation.getCoreWorkload() != null
                ? evaluation.getCoreWorkload() : BigDecimal.ZERO;
        BigDecimal reportWorkload = evaluation.getReportWorkload() != null
                ? evaluation.getReportWorkload() : BigDecimal.ZERO;
        BigDecimal customDevWorkload = evaluation.getCustomDevWorkload() != null
                ? evaluation.getCustomDevWorkload() : BigDecimal.ZERO;

        // 计算占比
        BigDecimal total = totalWorkload.compareTo(BigDecimal.ZERO) > 0 ? totalWorkload : BigDecimal.ONE;

        // 核心迁移
        StatisticsResultVO.WorkloadTypeDistribution core = new StatisticsResultVO.WorkloadTypeDistribution();
        core.setType("核心迁移");
        core.setWorkload(coreWorkload);
        core.setPercentage(coreWorkload.multiply(new BigDecimal("100"))
                .divide(total, 2, RoundingMode.HALF_UP));
        distribution.add(core);

        // 报表迁移
        StatisticsResultVO.WorkloadTypeDistribution report = new StatisticsResultVO.WorkloadTypeDistribution();
        report.setType("报表迁移");
        report.setWorkload(reportWorkload);
        report.setPercentage(reportWorkload.multiply(new BigDecimal("100"))
                .divide(total, 2, RoundingMode.HALF_UP));
        distribution.add(report);

        // 客开定制
        StatisticsResultVO.WorkloadTypeDistribution customDev = new StatisticsResultVO.WorkloadTypeDistribution();
        customDev.setType("客开定制");
        customDev.setWorkload(customDevWorkload);
        customDev.setPercentage(customDevWorkload.multiply(new BigDecimal("100"))
                .divide(total, 2, RoundingMode.HALF_UP));
        distribution.add(customDev);

        return distribution;
    }

    /**
     * 构建模块工作量对比列表
     */
    private List<StatisticsResultVO.ModuleWorkloadVO> buildModuleWorkloads(
            List<ProjectModuleConfig> configs, List<Module> modules, Evaluation evaluation) {
        if (configs.isEmpty() || modules.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建模块映射
        Map<Long, Module> moduleMap = modules.stream()
                .collect(Collectors.toMap(Module::getId, Function.identity()));

        // 构建模块工作量列表
        List<StatisticsResultVO.ModuleWorkloadVO> moduleWorkloads = new ArrayList<>();
        BigDecimal dataVolumeWeight = getWeightOrOne(evaluation.getDataVolumeLadderId(),
                dataVolumeLadderMapper);
        BigDecimal userCountWeight = getWeightOrOne(evaluation.getUserCountLadderId(),
                userCountLadderMapper);

        for (ProjectModuleConfig config : configs) {
            Module module = moduleMap.get(config.getModuleId());
            if (module == null) {
                continue;
            }

            BigDecimal baseWorkload = module.getBaseWorkload() != null
                    ? module.getBaseWorkload() : BigDecimal.ZERO;
            BigDecimal weight = config.getWeight() != null
                    ? config.getWeight() : (module.getDefaultWeight() != null
                    ? module.getDefaultWeight() : BigDecimal.ONE);

            // 计算模块工作量
            BigDecimal workload = baseWorkload.multiply(weight)
                    .multiply(dataVolumeWeight)
                    .multiply(userCountWeight)
                    .setScale(2, RoundingMode.HALF_UP);

            StatisticsResultVO.ModuleWorkloadVO vo = new StatisticsResultVO.ModuleWorkloadVO();
            vo.setModuleName(module.getModuleName());
            vo.setCategory(module.getCategory());
            vo.setBaseWorkload(baseWorkload);
            vo.setWeight(weight);
            vo.setWorkload(workload);
            moduleWorkloads.add(vo);
        }

        // 计算占比
        BigDecimal totalCoreWorkload = evaluation.getCoreWorkload() != null
                ? evaluation.getCoreWorkload() : BigDecimal.ZERO;
        BigDecimal total = totalCoreWorkload.compareTo(BigDecimal.ZERO) > 0 ? totalCoreWorkload : BigDecimal.ONE;

        for (StatisticsResultVO.ModuleWorkloadVO vo : moduleWorkloads) {
            vo.setPercentage(vo.getWorkload().multiply(new BigDecimal("100"))
                    .divide(total, 2, RoundingMode.HALF_UP));
        }

        // 按工作量降序排序
        moduleWorkloads.sort(Comparator.comparing(StatisticsResultVO.ModuleWorkloadVO::getWorkload,
                Comparator.reverseOrder()));

        return moduleWorkloads;
    }

    /**
     * 获取阶梯权重，默认返回1
     */
    private BigDecimal getWeightOrOne(Long ladderId, DataVolumeLadderMapper mapper) {
        if (ladderId == null) {
            return BigDecimal.ONE;
        }
        DataVolumeLadder ladder = mapper.selectById(ladderId);
        return ladder != null && ladder.getWeight() != null ? ladder.getWeight() : BigDecimal.ONE;
    }

    /**
     * 获取阶梯权重，默认返回1
     */
    private BigDecimal getWeightOrOne(Long ladderId, UserCountLadderMapper mapper) {
        if (ladderId == null) {
            return BigDecimal.ONE;
        }
        UserCountLadder ladder = mapper.selectById(ladderId);
        return ladder != null && ladder.getWeight() != null ? ladder.getWeight() : BigDecimal.ONE;
    }

    /**
     * 构建多维度评估指标
     */
    private StatisticsResultVO.MultiDimensionIndicators buildMultiDimensionIndicators(
            Evaluation evaluation, int moduleCount,
            DataVolumeLadder dataVolumeLadder, UserCountLadder userCountLadder) {

        StatisticsResultVO.MultiDimensionIndicators indicators = new StatisticsResultVO.MultiDimensionIndicators();

        // 数据量维度
        BigDecimal dataVolume = evaluation.getDataVolume() != null
                ? evaluation.getDataVolume() : BigDecimal.ZERO;
        indicators.setDataVolumeValue(dataVolume.divide(MAX_DATA_VOLUME, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        indicators.setDataVolumeActual(dataVolume.toPlainString() + "万条");
        indicators.setDataVolumeLadder(dataVolumeLadder != null
                ? dataVolumeLadder.getLadderName() : "未知");

        // 用户数维度
        Integer userCount = evaluation.getUserCount() != null ? evaluation.getUserCount() : 0;
        indicators.setUserCountValue(new BigDecimal(userCount).divide(MAX_USER_COUNT, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        indicators.setUserCountActual(userCount + "人");
        indicators.setUserCountLadder(userCountLadder != null
                ? userCountLadder.getLadderName() : "未知");

        // 模块数维度
        indicators.setModuleCountValue(new BigDecimal(moduleCount)
                .divide(new BigDecimal(MAX_MODULE_COUNT), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        indicators.setModuleCountActual(moduleCount);

        // 报表数维度
        Integer reportCount = evaluation.getReportCount() != null ? evaluation.getReportCount() : 0;
        indicators.setReportCountValue(new BigDecimal(reportCount)
                .divide(new BigDecimal(MAX_REPORT_COUNT), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        indicators.setReportCountActual(reportCount);

        // 客开维度
        boolean hasCustomDev = evaluation.getHasCustomDev() != null && evaluation.getHasCustomDev() == 1;
        indicators.setHasCustomDev(hasCustomDev);
        indicators.setCustomDevValue(hasCustomDev ? new BigDecimal("80") : new BigDecimal("20"));
        indicators.setCustomDevWorkload(evaluation.getCustomDevWorkload());

        return indicators;
    }

    /**
     * 构建风险提示列表
     */
    private List<StatisticsResultVO.RiskWarning> buildRiskWarnings(
            Evaluation evaluation, List<ProjectModuleConfig> configs,
            List<Module> modules, DataVolumeLadder dataVolumeLadder, UserCountLadder userCountLadder) {
        List<StatisticsResultVO.RiskWarning> warnings = new ArrayList<>();

        // 构建模块映射
        Map<Long, Module> moduleMap = modules.stream()
                .collect(Collectors.toMap(Module::getId, Function.identity()));

        // 1. 高复杂度模块风险
        for (ProjectModuleConfig config : configs) {
            BigDecimal weight = config.getWeight() != null
                    ? config.getWeight() : (moduleMap.get(config.getModuleId()) != null
                    ? moduleMap.get(config.getModuleId()).getDefaultWeight() : BigDecimal.ONE);

            if (weight.compareTo(HIGH_COMPLEXITY_THRESHOLD) >= 0) {
                Module module = moduleMap.get(config.getModuleId());
                if (module != null) {
                    StatisticsResultVO.RiskWarning warning = new StatisticsResultVO.RiskWarning();
                    warning.setType("MODULE_COMPLEXITY");
                    warning.setLevel("高");
                    warning.setDescription("模块【" + module.getModuleName() + "】复杂度较高（系数：" + weight + "）");
                    warning.setSuggestion("建议增加该模块的测试时间和数据验证工作量");
                    warnings.add(warning);
                }
            }
        }

        // 2. 大数据量风险
        if (dataVolumeLadder != null && dataVolumeLadder.getWeight() != null
                && dataVolumeLadder.getWeight().compareTo(new BigDecimal("1.5")) >= 0) {
            StatisticsResultVO.RiskWarning warning = new StatisticsResultVO.RiskWarning();
            warning.setType("DATA_VOLUME");
            warning.setLevel("中");
            warning.setDescription("数据量较大（" + evaluation.getDataVolume() + "万条），处于【"
                    + dataVolumeLadder.getLadderName() + "】级别");
            warning.setSuggestion("建议进行数据清洗和分区处理，制定详细的数据迁移方案");
            warnings.add(warning);
        }

        // 3. 大规模用户风险
        if (userCountLadder != null && userCountLadder.getWeight() != null
                && userCountLadder.getWeight().compareTo(new BigDecimal("1.5")) >= 0) {
            StatisticsResultVO.RiskWarning warning = new StatisticsResultVO.RiskWarning();
            warning.setType("USER_SCALE");
            warning.setLevel("中");
            warning.setDescription("用户规模较大（" + evaluation.getUserCount() + "人），处于【"
                    + userCountLadder.getLadderName() + "】级别");
            warning.setSuggestion("建议进行权限迁移方案设计，考虑并行切换策略");
            warnings.add(warning);
        }

        // 4. 客开风险
        if (evaluation.getHasCustomDev() != null && evaluation.getHasCustomDev() == 1) {
            StatisticsResultVO.RiskWarning warning = new StatisticsResultVO.RiskWarning();
            warning.setType("CUSTOM_DEV");
            warning.setLevel("高");
            warning.setDescription("项目包含客开定制功能（" + evaluation.getCustomDevWorkload() + "人天）");
            warning.setSuggestion("建议与客户充分沟通客开需求，确保需求文档完整准确");
            warnings.add(warning);
        }

        return warnings;
    }

    /**
     * 构建评估指标概览
     */
    private StatisticsResultVO.EvaluationOverview buildEvaluationOverview(
            Evaluation evaluation, int moduleCount) {
        StatisticsResultVO.EvaluationOverview overview = new StatisticsResultVO.EvaluationOverview();

        overview.setModuleCount(moduleCount);
        overview.setDataVolume(evaluation.getDataVolume());
        overview.setUserCount(evaluation.getUserCount());
        overview.setReportCount(evaluation.getReportCount());
        overview.setHasCustomDev(evaluation.getHasCustomDev() != null && evaluation.getHasCustomDev() == 1);

        // 获取阶梯名称
        if (evaluation.getDataVolumeLadderId() != null) {
            DataVolumeLadder ladder = dataVolumeLadderMapper.selectById(evaluation.getDataVolumeLadderId());
            if (ladder != null) {
                overview.setDataVolumeLadder(ladder.getLadderName());
            }
        }
        if (evaluation.getUserCountLadderId() != null) {
            UserCountLadder ladder = userCountLadderMapper.selectById(evaluation.getUserCountLadderId());
            if (ladder != null) {
                overview.setUserCountLadder(ladder.getLadderName());
            }
        }

        return overview;
    }
}
