package com.migrametric.service.statistics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.migrametric.common.BusinessException;
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
import com.migrametric.service.statistics.impl.StatisticsServiceImpl;
import com.migrametric.vo.statistics.StatisticsResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * StatisticsService 单元测试
 * <p>
 * 测试统计服务的核心功能，包括：
 * - 工作量类型分布统计
 * - 模块工作量对比统计
 * - 多维度评估指标计算
 * - 风险识别与提示
 * </p>
 *
 * @author MigraMetric Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsService 单元测试")
class StatisticsServiceTest {

    @Mock
    private EvaluationMapper evaluationMapper;

    @Mock
    private ProjectModuleConfigMapper moduleConfigMapper;

    @Mock
    private ModuleMapper moduleMapper;

    @Mock
    private DataVolumeLadderMapper dataVolumeLadderMapper;

    @Mock
    private UserCountLadderMapper userCountLadderMapper;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    // ========== 测试数据 ==========

    private Evaluation testEvaluation;
    private List<ProjectModuleConfig> testModuleConfigs;
    private List<Module> testModules;
    private DataVolumeLadder testDataVolumeLadder;
    private UserCountLadder testUserCountLadder;

    @BeforeEach
    void setUp() {
        // 测试评估记录
        testEvaluation = new Evaluation();
        testEvaluation.setId(1L);
        testEvaluation.setProjectId(1L);
        testEvaluation.setTableCount(150);
        testEvaluation.setDataVolume(new BigDecimal("500"));
        testEvaluation.setDataVolumeLadderId(2L);
        testEvaluation.setUserCount(600);
        testEvaluation.setUserCountLadderId(2L);
        testEvaluation.setReportCount(50);
        testEvaluation.setHasCustomDev(1);
        testEvaluation.setCustomDevWorkload(new BigDecimal("20"));
        testEvaluation.setCoreWorkload(new BigDecimal("111.38"));
        testEvaluation.setReportWorkload(new BigDecimal("25.00"));
        testEvaluation.setTotalWorkload(new BigDecimal("156.38"));

        // 测试数据量阶梯
        testDataVolumeLadder = new DataVolumeLadder();
        testDataVolumeLadder.setId(2L);
        testDataVolumeLadder.setLadderName("中型");
        testDataVolumeLadder.setWeight(new BigDecimal("1.0"));

        // 测试用户数阶梯
        testUserCountLadder = new UserCountLadder();
        testUserCountLadder.setId(2L);
        testUserCountLadder.setLadderName("中规模");
        testUserCountLadder.setWeight(new BigDecimal("1.2"));

        // 测试模块1：财务管理
        Module module1 = new Module();
        module1.setId(1L);
        module1.setModuleName("财务管理");
        module1.setCategory("财务");
        module1.setBaseWorkload(new BigDecimal("15"));
        module1.setDefaultWeight(new BigDecimal("1.3"));

        // 测试模块2：供应链管理
        Module module2 = new Module();
        module2.setId(2L);
        module2.setModuleName("供应链管理");
        module2.setCategory("采购");
        module2.setBaseWorkload(new BigDecimal("20"));
        module2.setDefaultWeight(new BigDecimal("1.5"));

        testModules = Arrays.asList(module1, module2);

        // 测试模块配置
        ProjectModuleConfig config1 = new ProjectModuleConfig();
        config1.setId(1L);
        config1.setProjectId(1L);
        config1.setModuleId(1L);
        config1.setWeight(new BigDecimal("1.3"));

        ProjectModuleConfig config2 = new ProjectModuleConfig();
        config2.setId(2L);
        config2.setProjectId(1L);
        config2.setModuleId(2L);
        config2.setWeight(new BigDecimal("1.5"));

        testModuleConfigs = Arrays.asList(config1, config2);
    }

    // ========== 工作量类型分布测试 (STAT-TYPE-*) ==========

    @Nested
    @DisplayName("工作量类型分布测试 (STAT-TYPE-*)")
    class WorkloadTypeDistributionTests {

        @Test
        @DisplayName("STAT-TYPE-001: 返回三种工作量类型")
        void shouldReturnThreeWorkloadTypes() {
            // Given
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then
            assertThat(result.getWorkloadTypeDistribution()).hasSize(3);
        }

        @Test
        @DisplayName("STAT-TYPE-002: 占比计算精度")
        void shouldCalculatePercentageCorrectly() {
            // Given
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then
            BigDecimal totalPercentage = result.getWorkloadTypeDistribution().stream()
                    .map(StatisticsResultVO.WorkloadTypeDistribution::getPercentage)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 允许舍入误差，总和接近100%
            assertThat(totalPercentage.doubleValue()).isCloseTo(100.0, org.assertj.core.data.Offset.offset(0.5));
        }

        @Test
        @DisplayName("STAT-TYPE-003: 工作量值正确")
        void shouldReturnCorrectWorkloadValues() {
            // Given
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then
            StatisticsResultVO.WorkloadTypeDistribution core = result.getWorkloadTypeDistribution().stream()
                    .filter(t -> "核心迁移".equals(t.getType()))
                    .findFirst().orElseThrow();

            assertThat(core.getWorkload()).isEqualByComparingTo("111.38");
        }
    }

    // ========== 模块工作量对比测试 (STAT-MODULE-*) ==========

    @Nested
    @DisplayName("模块工作量对比测试 (STAT-MODULE-*)")
    class ModuleWorkloadTests {

        @Test
        @DisplayName("STAT-MODULE-001: 模块数量正确")
        void shouldReturnCorrectModuleCount() {
            // Given
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then
            assertThat(result.getModuleWorkloads()).hasSize(2);
        }

        @Test
        @DisplayName("STAT-MODULE-002: 模块按工作量降序排列")
        void shouldSortModulesByWorkloadDescending() {
            // Given
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then
            List<StatisticsResultVO.ModuleWorkloadVO> modules = result.getModuleWorkloads();
            for (int i = 0; i < modules.size() - 1; i++) {
                assertThat(modules.get(i).getWorkload().doubleValue())
                        .isGreaterThanOrEqualTo(modules.get(i + 1).getWorkload().doubleValue());
            }
        }

        @Test
        @DisplayName("STAT-MODULE-003: 空数据返回空列表")
        void shouldReturnEmptyListForNoModules() {
            // Given
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
            when(dataVolumeLadderMapper.selectById(any())).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(any())).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then
            assertThat(result.getModuleWorkloads()).isEmpty();
        }
    }

    // ========== 多维度评估指标测试 (STAT-MULTI-*) ==========

    @Nested
    @DisplayName("多维度评估指标测试 (STAT-MULTI-*)")
    class MultiDimensionIndicatorsTests {

        @Test
        @DisplayName("STAT-MULTI-001: 返回5个维度指标")
        void shouldReturnFiveDimensionIndicators() {
            // Given
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then
            StatisticsResultVO.MultiDimensionIndicators indicators = result.getMultiDimensionIndicators();
            assertThat(indicators).isNotNull();
            assertThat(indicators.getDataVolumeValue()).isNotNull();
            assertThat(indicators.getUserCountValue()).isNotNull();
            assertThat(indicators.getModuleCountValue()).isNotNull();
            assertThat(indicators.getReportCountValue()).isNotNull();
            assertThat(indicators.getCustomDevValue()).isNotNull();
        }

        @Test
        @DisplayName("STAT-MULTI-002: 数据量归一化正确")
        void shouldNormalizeDataVolumeCorrectly() {
            // Given
            testEvaluation.setDataVolume(new BigDecimal("5000"));
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then: 5000 / 10000 * 100 = 50
            assertThat(result.getMultiDimensionIndicators().getDataVolumeValue())
                    .isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("STAT-MULTI-003: 用户数归一化正确")
        void shouldNormalizeUserCountCorrectly() {
            // Given
            testEvaluation.setUserCount(500);
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then: 500 / 1000 * 100 = 50
            assertThat(result.getMultiDimensionIndicators().getUserCountValue())
                    .isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("STAT-MULTI-004: 客开情况归一化正确")
        void shouldNormalizeCustomDevCorrectly() {
            // Given: 有客开
            testEvaluation.setHasCustomDev(1);
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then: 有客开 = 80
            assertThat(result.getMultiDimensionIndicators().getCustomDevValue())
                    .isEqualByComparingTo("80");
            assertThat(result.getMultiDimensionIndicators().getHasCustomDev()).isTrue();
        }

        @Test
        @DisplayName("STAT-MULTI-005: 无客开归一化正确")
        void shouldNormalizeNoCustomDevCorrectly() {
            // Given: 无客开
            testEvaluation.setHasCustomDev(0);
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then: 无客开 = 20
            assertThat(result.getMultiDimensionIndicators().getCustomDevValue())
                    .isEqualByComparingTo("20");
            assertThat(result.getMultiDimensionIndicators().getHasCustomDev()).isFalse();
        }
    }

    // ========== 风险提示测试 (STAT-RISK-*) ==========

    @Nested
    @DisplayName("风险提示测试 (STAT-RISK-*)")
    class RiskWarningTests {

        @Test
        @DisplayName("STAT-RISK-001: 高复杂度模块风险识别")
        void shouldIdentifyHighComplexityModules() {
            // Given: 配置权重为2.0（>=1.5，为高复杂度）
            testModuleConfigs.get(0).setWeight(new BigDecimal("2.0"));
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then
            assertThat(result.getRiskWarnings()).isNotEmpty();
            assertThat(result.getRiskWarnings()).anyMatch(w -> "MODULE_COMPLEXITY".equals(w.getType()));
        }

        @Test
        @DisplayName("STAT-RISK-002: 大数据量风险识别")
        void shouldIdentifyLargeDataVolumeRisk() {
            // Given: 大型数据量阶梯
            testDataVolumeLadder.setWeight(new BigDecimal("1.5"));
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then
            assertThat(result.getRiskWarnings()).anyMatch(w -> "DATA_VOLUME".equals(w.getType()));
        }

        @Test
        @DisplayName("STAT-RISK-003: 大规模用户风险识别")
        void shouldIdentifyLargeUserScaleRisk() {
            // Given: 大规模用户阶梯
            testUserCountLadder.setWeight(new BigDecimal("1.5"));
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then
            assertThat(result.getRiskWarnings()).anyMatch(w -> "USER_SCALE".equals(w.getType()));
        }

        @Test
        @DisplayName("STAT-RISK-004: 客开风险识别")
        void shouldIdentifyCustomDevRisk() {
            // Given: 有客开
            testEvaluation.setHasCustomDev(1);
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then
            assertThat(result.getRiskWarnings()).anyMatch(w -> "CUSTOM_DEV".equals(w.getType()));
        }
    }

    // ========== 总体汇总测试 (STAT-SUMMARY-*) ==========

    @Nested
    @DisplayName("总体汇总测试 (STAT-SUMMARY-*)")
    class SummaryTests {

        @Test
        @DisplayName("STAT-SUMMARY-001: 总工作量正确")
        void shouldReturnCorrectTotalWorkload() {
            // Given
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then
            assertThat(result.getTotalWorkload()).isEqualByComparingTo("156.38");
        }

        @Test
        @DisplayName("STAT-SUMMARY-002: 预估工期计算正确")
        void shouldCalculateEstimatedMonthsCorrectly() {
            // Given
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then: 156.38 / 22 = 7.11
            assertThat(result.getEstimatedMonths()).isEqualByComparingTo("7.11");
        }

        @Test
        @DisplayName("STAT-SUMMARY-003: 评估概览数据正确")
        void shouldReturnCorrectEvaluationOverview() {
            // Given
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            StatisticsResultVO result = statisticsService.getStatistics(1L);

            // Then
            StatisticsResultVO.EvaluationOverview overview = result.getEvaluationOverview();
            assertThat(overview.getModuleCount()).isEqualTo(2);
            assertThat(overview.getDataVolume()).isEqualByComparingTo("500");
            assertThat(overview.getUserCount()).isEqualTo(600);
            assertThat(overview.getReportCount()).isEqualTo(50);
            assertThat(overview.getHasCustomDev()).isTrue();
        }
    }

    // ========== 异常场景测试 ==========

    @Nested
    @DisplayName("异常场景测试")
    class ErrorTests {

        @Test
        @DisplayName("STAT-ERROR-001: 评估不存在抛出异常")
        void shouldThrowExceptionWhenEvaluationNotFound() {
            // Given
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> statisticsService.getStatistics(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("评估记录不存在");
        }
    }
}
