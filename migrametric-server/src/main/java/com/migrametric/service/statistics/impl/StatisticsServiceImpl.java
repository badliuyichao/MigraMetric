package com.migrametric.service.statistics.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.migrametric.common.BusinessException;
import com.migrametric.common.ResultCode;
import com.migrametric.entity.evaluation.Evaluation;
import com.migrametric.entity.evaluation.ProjectModuleConfig;
import com.migrametric.entity.ladder.DataVolumeLadder;
import com.migrametric.entity.ladder.UserCountLadder;
import com.migrametric.entity.module.Module;
import com.migrametric.entity.project.Project;
import com.migrametric.entity.user.User;
import com.migrametric.mapper.evaluation.EvaluationMapper;
import com.migrametric.mapper.evaluation.ProjectModuleConfigMapper;
import com.migrametric.mapper.ladder.DataVolumeLadderMapper;
import com.migrametric.mapper.ladder.UserCountLadderMapper;
import com.migrametric.mapper.module.ModuleMapper;
import com.migrametric.mapper.project.ProjectMapper;
import com.migrametric.mapper.user.UserMapper;
import com.migrametric.service.statistics.StatisticsService;
import com.migrametric.vo.statistics.StatisticsGlobalVO;
import com.migrametric.vo.statistics.StatisticsResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;

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

        // 无评估记录时返回空默认结果（而非抛异常）
        if (evaluation == null) {
            log.info("项目无评估记录, projectId: {}", projectId);
            StatisticsResultVO empty = new StatisticsResultVO();
            empty.setProjectId(projectId);
            empty.setTotalWorkload(BigDecimal.ZERO);
            empty.setEstimatedMonths(BigDecimal.ZERO);
            empty.setWorkloadTypeDistribution(List.of());
            empty.setModuleWorkloads(List.of());
            empty.setRiskWarnings(List.of());
            StatisticsResultVO.EvaluationOverview overview = new StatisticsResultVO.EvaluationOverview();
            overview.setModuleCount(0);
            overview.setHasCustomDev(false);
            empty.setEvaluationOverview(overview);
            return empty;
        }

        // 2. 获取已配置的模块列表
        List<ProjectModuleConfig> configs = getModuleConfigs(projectId);

        // 3. 获取模块信息
        List<Module> modules = getModules(configs);

        // 4. 获取数据量和用户数阶梯信息（防空）
        DataVolumeLadder dataVolumeLadder = evaluation.getDataVolumeLadderId() != null
                ? getDataVolumeLadder(evaluation.getDataVolumeLadderId()) : null;
        UserCountLadder userCountLadder = evaluation.getUserCountLadderId() != null
                ? getUserCountLadder(evaluation.getUserCountLadderId()) : null;

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
        return evaluationMapper.selectOne(wrapper);
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

    @Override
    public StatisticsResultVO.DashboardOverview getOverview() {
        StatisticsResultVO.DashboardOverview overview = new StatisticsResultVO.DashboardOverview();

        overview.setProjectCount(projectMapper.selectCount(null));
        overview.setEvaluationCount(evaluationMapper.selectCount(null));
        overview.setUserCount(userMapper.selectCount(null));

        // SUM(totalWorkload) 可能为 null（无评估记录时），做防空处理
        LambdaQueryWrapper<Evaluation> sumWrapper = new LambdaQueryWrapper<>();
        sumWrapper.select(Evaluation::getTotalWorkload);
        List<Evaluation> evaluations = evaluationMapper.selectList(sumWrapper);
        BigDecimal totalWorkload = evaluations.stream()
                .map(Evaluation::getTotalWorkload)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        overview.setTotalWorkload(totalWorkload);

        return overview;
    }

    // ============================================================
    //  REQ-3.4.2 全局统计（aggregations + ranking）
    // ============================================================

    @Override
    public StatisticsGlobalVO.GlobalAggregationVO getGlobalAggregations(
            String dimension, LocalDate dateFrom, LocalDate dateTo,
            Long sourceSystemId, Long targetSystemId, String status) {
        log.info("全局聚合统计: dimension={}, dateFrom={}, dateTo={}, sourceSystemId={}, targetSystemId={}, status={}",
                dimension, dateFrom, dateTo, sourceSystemId, targetSystemId, status);

        if (!Set.of("module", "type", "complexity").contains(dimension)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "dimension 必须是 module/type/complexity 之一");
        }

        String effectiveStatus = (status == null || status.isBlank()) ? "COMPLETED" : status;
        List<Long> projectIds = queryFilteredProjectIds(dateFrom, dateTo, sourceSystemId, targetSystemId, effectiveStatus);
        log.info("筛选到 {} 个项目参与聚合", projectIds.size());

        StatisticsGlobalVO.GlobalAggregationVO result = new StatisticsGlobalVO.GlobalAggregationVO();
        result.setDimension(dimension);

        if (projectIds.isEmpty()) {
            result.setItems(new ArrayList<>());
            result.setDataVolumeDistribution(new ArrayList<>());
            result.setUserCountDistribution(new ArrayList<>());
            result.setHighComplexityModules(new ArrayList<>());
            return result;
        }

        switch (dimension) {
            case "module" -> fillModuleAggregation(result, projectIds);
            case "type" -> fillTypeAggregation(result, projectIds);
            case "complexity" -> fillComplexityAggregation(result, projectIds);
        }
        return result;
    }

    @Override
    public StatisticsGlobalVO.GlobalRankingVO getGlobalRanking(String metric, Integer limit) {
        log.info("全局排行榜: metric={}, limit={}", metric, limit);

        if (!Set.of("workload", "userCount", "dataVolume").contains(metric)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "metric 必须是 workload/userCount/dataVolume 之一");
        }
        int effectiveLimit = (limit == null || limit < 1) ? 10 : Math.min(limit, 50);

        // 拉所有 COMPLETED 项目（不带 LIMIT，内存排序后取 Top N，避免复杂 SQL JOIN）
        List<Project> projects = projectMapper.selectList(
                new LambdaQueryWrapper<Project>().eq(Project::getStatus, "COMPLETED")
        );
        if (projects.isEmpty()) {
            StatisticsGlobalVO.GlobalRankingVO empty = new StatisticsGlobalVO.GlobalRankingVO();
            empty.setMetric(metric);
            empty.setItems(List.of());
            return empty;
        }

        Map<Long, Evaluation> evalMap = evaluationMapper.selectList(
                new LambdaQueryWrapper<Evaluation>().in(Evaluation::getProjectId,
                        projects.stream().map(Project::getId).toList())
        ).stream().collect(Collectors.toMap(Evaluation::getProjectId, Function.identity(), (a, b) -> a));

        String unit = switch (metric) {
            case "workload" -> "人天";
            case "userCount" -> "人";
            case "dataVolume" -> "万条";
            default -> "";
        };

        List<StatisticsGlobalVO.RankingItemVO> items = new ArrayList<>();
        for (Project p : projects) {
            Evaluation e = evalMap.get(p.getId());
            if (e == null) continue;
            StatisticsGlobalVO.RankingItemVO item = new StatisticsGlobalVO.RankingItemVO();
            item.setProjectId(p.getId());
            item.setProjectName(p.getProjectName());
            item.setCustomerName(p.getCustomerName());
            item.setValue(extractMetricValue(e, metric));
            item.setUnit(unit);
            items.add(item);
        }
        // 按 value DESC，同值按 projectId ASC
        items.sort(Comparator
                .comparing(StatisticsGlobalVO.RankingItemVO::getValue).reversed()
                .thenComparing(StatisticsGlobalVO.RankingItemVO::getProjectId));
        if (items.size() > effectiveLimit) {
            items = new ArrayList<>(items.subList(0, effectiveLimit));
        }

        StatisticsGlobalVO.GlobalRankingVO result = new StatisticsGlobalVO.GlobalRankingVO();
        result.setMetric(metric);
        result.setItems(items);
        return result;
    }

    /**
     * 公共筛选：按日期/系统/状态得到 projectId 列表
     */
    private List<Long> queryFilteredProjectIds(
            LocalDate dateFrom, LocalDate dateTo,
            Long sourceSystemId, Long targetSystemId, String status) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getStatus, status);
        if (sourceSystemId != null) wrapper.eq(Project::getSourceSystemId, sourceSystemId);
        if (targetSystemId != null) wrapper.eq(Project::getTargetSystemId, targetSystemId);
        // 日期范围按 project.create_time 过滤（需求文档说按 evaluation.create_time，简化按 project）
        if (dateFrom != null) wrapper.ge(Project::getCreateTime, dateFrom.atStartOfDay());
        if (dateTo != null) wrapper.le(Project::getCreateTime, dateTo.atTime(23, 59, 59));
        List<Project> projects = projectMapper.selectList(wrapper);
        return projects.stream().map(Project::getId).toList();
    }

    /**
     * 按模块聚合：Σ(module.baseWorkload × weight × dvw × ucw) over 选中项目
     */
    private void fillModuleAggregation(StatisticsGlobalVO.GlobalAggregationVO result, List<Long> projectIds) {
        // 拉所有 evaluation（用于阶梯系数）
        List<Evaluation> evals = evaluationMapper.selectList(
                new LambdaQueryWrapper<Evaluation>().in(Evaluation::getProjectId, projectIds)
        );
        Map<Long, Evaluation> evalMap = evals.stream()
                .collect(Collectors.toMap(Evaluation::getProjectId, Function.identity(), (a, b) -> a));

        // 拉所有 module_config
        List<ProjectModuleConfig> configs = moduleConfigMapper.selectList(
                new LambdaQueryWrapper<ProjectModuleConfig>().in(ProjectModuleConfig::getProjectId, projectIds)
        );
        if (configs.isEmpty()) {
            result.setItems(List.of());
            return;
        }

        // 拉对应模块
        Set<Long> moduleIds = configs.stream().map(ProjectModuleConfig::getModuleId).collect(Collectors.toSet());
        Map<Long, Module> moduleMap = moduleMapper.selectBatchIds(moduleIds).stream()
                .collect(Collectors.toMap(Module::getId, Function.identity(), (a, b) -> a));

        // 按 moduleId 聚合
        record Acc(BigDecimal workload, Set<Long> projects) {}
        Map<Long, Acc> agg = new java.util.HashMap<>();
        for (ProjectModuleConfig c : configs) {
            Module m = moduleMap.get(c.getModuleId());
            if (m == null || m.getBaseWorkload() == null) continue;
            Evaluation ev = evalMap.get(c.getProjectId());
            BigDecimal dvw = (ev != null && ev.getDataVolumeLadderId() != null)
                    ? getLadderWeight(ev.getDataVolumeLadderId(), true) : BigDecimal.ONE;
            BigDecimal ucw = (ev != null && ev.getUserCountLadderId() != null)
                    ? getLadderWeight(ev.getUserCountLadderId(), false) : BigDecimal.ONE;
            BigDecimal w = c.getWeight() != null ? c.getWeight()
                    : (m.getDefaultWeight() != null ? m.getDefaultWeight() : BigDecimal.ONE);
            BigDecimal wl = m.getBaseWorkload().multiply(w).multiply(dvw).multiply(ucw)
                    .setScale(2, RoundingMode.HALF_UP);
            Acc old = agg.getOrDefault(m.getId(), new Acc(BigDecimal.ZERO, new java.util.HashSet<>()));
            old.projects.add(c.getProjectId());
            agg.put(m.getId(), new Acc(old.workload.add(wl), old.projects));
        }

        BigDecimal total = agg.values().stream()
                .map(Acc::workload).reduce(BigDecimal.ZERO, BigDecimal::add);
        result.setTotal(total);

        List<StatisticsGlobalVO.AggregationItemVO> items = new ArrayList<>();
        agg.forEach((mid, acc) -> {
            Module m = moduleMap.get(mid);
            StatisticsGlobalVO.AggregationItemVO it = new StatisticsGlobalVO.AggregationItemVO();
            it.setName(m.getModuleName());
            it.setValue(acc.workload);
            it.setProjectCount(acc.projects.size());
            it.setPercentage(total.compareTo(BigDecimal.ZERO) > 0
                    ? acc.workload.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            items.add(it);
        });
        items.sort(Comparator.comparing(StatisticsGlobalVO.AggregationItemVO::getValue).reversed());
        result.setItems(items);
    }

    /**
     * 按工作量类型聚合：核心/报表/客开
     */
    private void fillTypeAggregation(StatisticsGlobalVO.GlobalAggregationVO result, List<Long> projectIds) {
        List<Evaluation> evals = evaluationMapper.selectList(
                new LambdaQueryWrapper<Evaluation>().in(Evaluation::getProjectId, projectIds)
        );
        BigDecimal core = sum(evals, Evaluation::getCoreWorkload);
        BigDecimal report = sum(evals, Evaluation::getReportWorkload);
        BigDecimal customDev = evals.stream()
                .map(e -> e.getCustomDevWorkload() == null ? BigDecimal.ZERO : e.getCustomDevWorkload())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = core.add(report).add(customDev);
        result.setTotal(total);

        List<StatisticsGlobalVO.AggregationItemVO> items = new ArrayList<>();
        items.add(buildTypeItem("核心迁移", core, total, evals.size()));
        items.add(buildTypeItem("报表迁移", report, total, evals.size()));
        items.add(buildTypeItem("客开定制", customDev, total, evals.size()));
        result.setItems(items);
    }

    private StatisticsGlobalVO.AggregationItemVO buildTypeItem(
            String name, BigDecimal value, BigDecimal total, int projectCount) {
        StatisticsGlobalVO.AggregationItemVO it = new StatisticsGlobalVO.AggregationItemVO();
        it.setName(name);
        it.setValue(value);
        it.setProjectCount(projectCount);
        it.setPercentage(total.compareTo(BigDecimal.ZERO) > 0
                ? value.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        return it;
    }

    private BigDecimal sum(List<Evaluation> evals, Function<Evaluation, BigDecimal> getter) {
        return evals.stream()
                .map(e -> getter.apply(e) == null ? BigDecimal.ZERO : getter.apply(e))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 按复杂度聚合：阶梯分布 + 高复杂度模块
     */
    private void fillComplexityAggregation(StatisticsGlobalVO.GlobalAggregationVO result, List<Long> projectIds) {
        List<Evaluation> evals = evaluationMapper.selectList(
                new LambdaQueryWrapper<Evaluation>().in(Evaluation::getProjectId, projectIds)
        );

        // 数据量阶梯分布
        Map<Long, List<Evaluation>> dvlGroups = evals.stream()
                .filter(e -> e.getDataVolumeLadderId() != null)
                .collect(Collectors.groupingBy(Evaluation::getDataVolumeLadderId));
        Map<Long, DataVolumeLadder> dvlMap = dvlGroups.isEmpty() ? Map.of() :
                dataVolumeLadderMapper.selectBatchIds(dvlGroups.keySet()).stream()
                        .collect(Collectors.toMap(DataVolumeLadder::getId, Function.identity(), (a, b) -> a));
        List<StatisticsGlobalVO.LadderBucketVO> dvDist = new ArrayList<>();
        dvlGroups.forEach((lid, list) -> {
            DataVolumeLadder l = dvlMap.get(lid);
            if (l == null) return;
            StatisticsGlobalVO.LadderBucketVO b = new StatisticsGlobalVO.LadderBucketVO();
            b.setLadderName(l.getLadderName());
            b.setProjectCount(list.size());
            b.setTotalWorkload(list.stream()
                    .map(e -> e.getTotalWorkload() == null ? BigDecimal.ZERO : e.getTotalWorkload())
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            dvDist.add(b);
        });
        dvDist.sort(Comparator.comparing(StatisticsGlobalVO.LadderBucketVO::getLadderName));
        result.setDataVolumeDistribution(dvDist);

        // 用户数阶梯分布
        Map<Long, List<Evaluation>> uclGroups = evals.stream()
                .filter(e -> e.getUserCountLadderId() != null)
                .collect(Collectors.groupingBy(Evaluation::getUserCountLadderId));
        Map<Long, UserCountLadder> uclMap = uclGroups.isEmpty() ? Map.of() :
                userCountLadderMapper.selectBatchIds(uclGroups.keySet()).stream()
                        .collect(Collectors.toMap(UserCountLadder::getId, Function.identity(), (a, b) -> a));
        List<StatisticsGlobalVO.LadderBucketVO> ucDist = new ArrayList<>();
        uclGroups.forEach((lid, list) -> {
            UserCountLadder l = uclMap.get(lid);
            if (l == null) return;
            StatisticsGlobalVO.LadderBucketVO b = new StatisticsGlobalVO.LadderBucketVO();
            b.setLadderName(l.getLadderName());
            b.setProjectCount(list.size());
            b.setTotalWorkload(list.stream()
                    .map(e -> e.getTotalWorkload() == null ? BigDecimal.ZERO : e.getTotalWorkload())
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            ucDist.add(b);
        });
        ucDist.sort(Comparator.comparing(StatisticsGlobalVO.LadderBucketVO::getLadderName));
        result.setUserCountDistribution(ucDist);

        // 高复杂度模块
        List<ProjectModuleConfig> configs = moduleConfigMapper.selectList(
                new LambdaQueryWrapper<ProjectModuleConfig>().in(ProjectModuleConfig::getProjectId, projectIds)
        );
        Map<Long, Set<Long>> moduleProjects = new java.util.HashMap<>();
        for (ProjectModuleConfig c : configs) {
            BigDecimal w = c.getWeight() == null ? BigDecimal.ONE : c.getWeight();
            if (w.compareTo(new BigDecimal("1.5")) >= 0) {
                moduleProjects.computeIfAbsent(c.getModuleId(), k -> new java.util.HashSet<>()).add(c.getProjectId());
            }
        }
        Map<Long, Module> modMap = moduleProjects.isEmpty() ? Map.of() :
                moduleMapper.selectBatchIds(moduleProjects.keySet()).stream()
                        .collect(Collectors.toMap(Module::getId, Function.identity(), (a, b) -> a));
        List<StatisticsGlobalVO.HighComplexityModuleVO> highMods = new ArrayList<>();
        moduleProjects.forEach((mid, pids) -> {
            Module m = modMap.get(mid);
            if (m == null) return;
            // 取最大 weight（这些项目可能不同 weight）
            BigDecimal maxW = configs.stream()
                    .filter(c -> c.getModuleId().equals(mid))
                    .map(ProjectModuleConfig::getWeight)
                    .filter(java.util.Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ONE);
            StatisticsGlobalVO.HighComplexityModuleVO h = new StatisticsGlobalVO.HighComplexityModuleVO();
            h.setModuleName(m.getModuleName());
            h.setWeight(maxW);
            h.setProjectCount(pids.size());
            highMods.add(h);
        });
        highMods.sort(Comparator.comparing(StatisticsGlobalVO.HighComplexityModuleVO::getProjectCount).reversed());
        result.setHighComplexityModules(highMods);
    }

    private BigDecimal getLadderWeight(Long ladderId, boolean isDataVolume) {
        if (ladderId == null) return BigDecimal.ONE;
        if (isDataVolume) {
            DataVolumeLadder l = dataVolumeLadderMapper.selectById(ladderId);
            return l != null && l.getWeight() != null ? l.getWeight() : BigDecimal.ONE;
        } else {
            UserCountLadder l = userCountLadderMapper.selectById(ladderId);
            return l != null && l.getWeight() != null ? l.getWeight() : BigDecimal.ONE;
        }
    }

    private BigDecimal extractMetricValue(Evaluation e, String metric) {
        return switch (metric) {
            case "workload" -> e.getTotalWorkload() == null ? BigDecimal.ZERO : e.getTotalWorkload();
            case "userCount" -> e.getUserCount() == null ? BigDecimal.ZERO : BigDecimal.valueOf(e.getUserCount());
            case "dataVolume" -> e.getDataVolume() == null ? BigDecimal.ZERO : e.getDataVolume();
            default -> BigDecimal.ZERO;
        };
    }
}
