package com.migrametric.service.evaluation;

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
import com.migrametric.service.config.ReportConfigService;
import com.migrametric.service.evaluation.impl.WorkloadCalculationServiceImpl;
import com.migrametric.vo.evaluation.ModuleWorkloadVO;
import com.migrametric.vo.evaluation.WorkloadResultVO;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * WorkloadCalculationService 单元测试
 * <p>
 * 测试工作量计算服务的核心功能，包括：
 * - 核心迁移工作量计算
 * - 报表工作量计算
 * - 客开工作量计算
 * - 总工作量计算
 * - 工作量明细生成
 * </p>
 *
 * @author MigraMetric Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadCalculationService 单元测试")
class WorkloadCalculationServiceTest {

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

    @Mock
    private ReportConfigService reportConfigService;

    @Mock
    private ModuleConfigService moduleConfigService;

    @InjectMocks
    private WorkloadCalculationServiceImpl workloadCalculationService;

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
        testEvaluation.setDataVolumeLadderId(2L);
        testEvaluation.setUserCountLadderId(2L);
        testEvaluation.setReportCount(50);
        testEvaluation.setHasCustomDev(1);
        testEvaluation.setCustomDevWorkload(new BigDecimal("20"));

        // 测试数据量阶梯
        testDataVolumeLadder = new DataVolumeLadder();
        testDataVolumeLadder.setId(2L);
        testDataVolumeLadder.setWeight(new BigDecimal("1.5"));

        // 测试用户数阶梯
        testUserCountLadder = new UserCountLadder();
        testUserCountLadder.setId(2L);
        testUserCountLadder.setWeight(new BigDecimal("1.5"));

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

    // ========== calculateCoreWorkload 测试 (WC-CORE-*) ==========

    @Nested
    @DisplayName("calculateCoreWorkload 测试 (WC-CORE-*)")
    class CalculateCoreWorkloadTests {

        @Test
        @DisplayName("WC-CORE-001: 核心工作量-单模块")
        void shouldCalculateSingleModuleCoreWorkload() {
            // Given: 财务管理模块 15天×1.3×1.5×1.5=43.875
            ModuleWorkloadVO module = createModuleWorkload(1L, "财务管理", new BigDecimal("15"),
                    new BigDecimal("1.3"), new BigDecimal("1.5"), new BigDecimal("1.5"));

            // When
            BigDecimal result = workloadCalculationService.calculateCoreWorkload(Collections.singletonList(module));

            // Then: 15 × 1.3 × 1.5 × 1.5 = 43.875
            assertThat(result).isEqualByComparingTo("43.88");
        }

        @Test
        @DisplayName("WC-CORE-002: 核心工作量-多模块")
        void shouldCalculateMultipleModulesCoreWorkload() {
            // Given: 财务管理 15×1.3×1.5×1.5=43.875 + 供应链管理 20×1.5×1.5×1.5=67.5 = 111.375
            ModuleWorkloadVO module1 = createModuleWorkload(1L, "财务管理", new BigDecimal("15"),
                    new BigDecimal("1.3"), new BigDecimal("1.5"), new BigDecimal("1.5"));
            ModuleWorkloadVO module2 = createModuleWorkload(2L, "供应链管理", new BigDecimal("20"),
                    new BigDecimal("1.5"), new BigDecimal("1.5"), new BigDecimal("1.5"));

            // When
            BigDecimal result = workloadCalculationService.calculateCoreWorkload(Arrays.asList(module1, module2));

            // Then: 43.88 + 67.50 = 111.38
            assertThat(result).isEqualByComparingTo("111.38");
        }

        @Test
        @DisplayName("WC-CORE-003: 核心工作量-所有系数1.0")
        void shouldCalculateWithAllCoefficientsOne() {
            // Given: 所有系数为1.0，结果=基础人天之和
            ModuleWorkloadVO module = createModuleWorkload(1L, "测试模块", new BigDecimal("20"),
                    BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);

            // When
            BigDecimal result = workloadCalculationService.calculateCoreWorkload(Collections.singletonList(module));

            // Then: 20 × 1 × 1 × 1 = 20
            assertThat(result).isEqualByComparingTo("20.00");
        }

        @Test
        @DisplayName("WC-CORE-004: 核心工作量-小数系数")
        void shouldCalculateWithDecimalCoefficients() {
            // Given: 小数系数
            // 10 × 1.25 × 1.35 × 1.45 = 24.47
            ModuleWorkloadVO module = createModuleWorkload(1L, "测试模块", new BigDecimal("10"),
                    new BigDecimal("1.25"), new BigDecimal("1.35"), new BigDecimal("1.45"));

            // When
            BigDecimal result = workloadCalculationService.calculateCoreWorkload(Collections.singletonList(module));

            // Then: 10 × 1.25 × 1.35 × 1.45 = 24.47
            assertThat(result).isEqualByComparingTo("24.47");
        }

        @Test
        @DisplayName("WC-CORE-005: 核心工作量-空列表")
        void shouldReturnZeroForEmptyList() {
            // When
            BigDecimal result = workloadCalculationService.calculateCoreWorkload(Collections.emptyList());

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("WC-CORE-006: 核心工作量-阶梯系数边界")
        void shouldCalculateWithLadderBoundary() {
            // Given: 100万匹配中型阶梯，数据量系数1.0
            ModuleWorkloadVO module = createModuleWorkload(1L, "测试模块", new BigDecimal("10"),
                    new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0"));

            // When
            BigDecimal result = workloadCalculationService.calculateCoreWorkload(Collections.singletonList(module));

            // Then: 10 × 1.0 × 1.0 × 1.0 = 10.00
            assertThat(result).isEqualByComparingTo("10.00");
        }
    }

    // ========== calculateReportWorkload 测试 (WC-RPT-*) ==========

    @Nested
    @DisplayName("calculateReportWorkload 测试 (WC-RPT-*)")
    class CalculateReportWorkloadTests {

        @Test
        @DisplayName("WC-RPT-001: 报表工作量-正常计算")
        void shouldCalculateReportWorkload() {
            // Given: 报表数量50，报表系数0.5
            when(reportConfigService.getValueAsBigDecimal(eq("report_workload_per_unit"), any()))
                    .thenReturn(new BigDecimal("0.5"));

            // When
            BigDecimal result = workloadCalculationService.calculateReportWorkload(50);

            // Then: 50 × 0.5 = 25.00
            assertThat(result).isEqualByComparingTo("25.00");
        }

        @Test
        @DisplayName("WC-RPT-002: 报表工作量-0报表")
        void shouldReturnZeroForZeroReport() {
            // When
            BigDecimal result = workloadCalculationService.calculateReportWorkload(0);

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("WC-RPT-003: 报表工作量-null报表")
        void shouldReturnZeroForNullReport() {
            // When
            BigDecimal result = workloadCalculationService.calculateReportWorkload(null);

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("WC-RPT-004: 报表工作量-小数结果")
        void shouldCalculateReportWorkloadWithDecimalResult() {
            // Given: 报表数量33，报表系数0.5
            when(reportConfigService.getValueAsBigDecimal(eq("report_workload_per_unit"), any()))
                    .thenReturn(new BigDecimal("0.5"));

            // When
            BigDecimal result = workloadCalculationService.calculateReportWorkload(33);

            // Then: 33 × 0.5 = 16.50
            assertThat(result).isEqualByComparingTo("16.50");
        }

        @Test
        @DisplayName("WC-RPT-005: 报表工作量-使用默认系数")
        void shouldUseDefaultCoefficient() {
            // Given: 配置服务返回null，使用默认系数0.5
            when(reportConfigService.getValueAsBigDecimal(eq("report_workload_per_unit"), any()))
                    .thenReturn(new BigDecimal("0.5"));

            // When
            BigDecimal result = workloadCalculationService.calculateReportWorkload(100);

            // Then: 100 × 0.5 = 50.00
            assertThat(result).isEqualByComparingTo("50.00");
        }
    }

    // ========== calculateCustomDevWorkload 测试 (WC-CUS-*) ==========

    @Nested
    @DisplayName("calculateCustomDevWorkload 测试 (WC-CUS-*)")
    class CalculateCustomDevWorkloadTests {

        @Test
        @DisplayName("WC-CUS-001: 客开工作量-有客开")
        void shouldCalculateCustomDevWorkload() {
            // When
            BigDecimal result = workloadCalculationService.calculateCustomDevWorkload(true, new BigDecimal("20"));

            // Then
            assertThat(result).isEqualByComparingTo("20.00");
        }

        @Test
        @DisplayName("WC-CUS-002: 客开工作量-无客开")
        void shouldReturnZeroForNoCustomDev() {
            // When
            BigDecimal result = workloadCalculationService.calculateCustomDevWorkload(false, null);

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("WC-CUS-003: 客开工作量-null客开人天")
        void shouldReturnZeroForNullWorkload() {
            // When
            BigDecimal result = workloadCalculationService.calculateCustomDevWorkload(true, null);

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("WC-CUS-004: 客开工作量-小数输入")
        void shouldHandleDecimalInput() {
            // When
            BigDecimal result = workloadCalculationService.calculateCustomDevWorkload(true, new BigDecimal("15.75"));

            // Then
            assertThat(result).isEqualByComparingTo("15.75");
        }
    }

    // ========== calculateTotalWorkload 测试 (WC-TOTAL-*) ==========

    @Nested
    @DisplayName("calculateTotalWorkload 测试 (WC-TOTAL-*)")
    class CalculateTotalWorkloadTests {

        @Test
        @DisplayName("WC-TOTAL-001: 总工作量-完整计算")
        void shouldCalculateTotalWorkload() {
            // Given: 核心111.38 + 报表25.00 + 客开20.00 = 156.38
            // When
            BigDecimal result = workloadCalculationService.calculateTotalWorkload(
                    new BigDecimal("111.38"),
                    new BigDecimal("25.00"),
                    new BigDecimal("20.00"));

            // Then
            assertThat(result).isEqualByComparingTo("156.38");
        }

        @Test
        @DisplayName("WC-TOTAL-002: 总工作量-无客开")
        void shouldCalculateTotalWorkloadWithoutCustomDev() {
            // Given: 核心111.38 + 报表25.00 + 客开0 = 136.38
            // When
            BigDecimal result = workloadCalculationService.calculateTotalWorkload(
                    new BigDecimal("111.38"),
                    new BigDecimal("25.00"),
                    BigDecimal.ZERO);

            // Then
            assertThat(result).isEqualByComparingTo("136.38");
        }

        @Test
        @DisplayName("WC-TOTAL-003: 总工作量-无报表")
        void shouldCalculateTotalWorkloadWithoutReport() {
            // Given: 核心111.38 + 报表0 + 客开20.00 = 131.38
            // When
            BigDecimal result = workloadCalculationService.calculateTotalWorkload(
                    new BigDecimal("111.38"),
                    BigDecimal.ZERO,
                    new BigDecimal("20.00"));

            // Then
            assertThat(result).isEqualByComparingTo("131.38");
        }

        @Test
        @DisplayName("WC-TOTAL-004: 总工作量-无客开无报表")
        void shouldCalculateTotalWorkloadWithoutCustomDevAndReport() {
            // Given: 核心111.38 + 报表0 + 客开0 = 111.38
            // When
            BigDecimal result = workloadCalculationService.calculateTotalWorkload(
                    new BigDecimal("111.38"),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO);

            // Then
            assertThat(result).isEqualByComparingTo("111.38");
        }

        @Test
        @DisplayName("WC-TOTAL-005: 总工作量-精度验证")
        void shouldVerifyDecimalPrecision() {
            // Given: 测试精度保留
            // When
            BigDecimal result = workloadCalculationService.calculateTotalWorkload(
                    new BigDecimal("10.111"),
                    new BigDecimal("5.555"),
                    new BigDecimal("3.333"));

            // Then: 10.111 + 5.555 + 3.333 = 19.00
            assertThat(result).isEqualByComparingTo("19.00");
        }

        @Test
        @DisplayName("WC-TOTAL-006: 总工作量-null处理")
        void shouldHandleNullValues() {
            // When
            BigDecimal result = workloadCalculationService.calculateTotalWorkload(null, null, null);

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ========== calculateModuleWorkloads 测试 (WC-MOD-*) ==========

    @Nested
    @DisplayName("calculateModuleWorkloads 测试 (WC-MOD-*)")
    class CalculateModuleWorkloadsTests {

        @Test
        @DisplayName("WC-MOD-001: 模块工作量计算-完整参数")
        void shouldCalculateModuleWorkloadsWithAllParams() {
            // Given
            ModuleWorkloadVO module = createModuleWorkloadInput(1L, "财务管理", new BigDecimal("15"), new BigDecimal("1.3"));

            // When
            List<ModuleWorkloadVO> result = workloadCalculationService.calculateModuleWorkloads(
                    Collections.singletonList(module), new BigDecimal("1.5"), new BigDecimal("1.5"));

            // Then
            assertThat(result).hasSize(1);
            ModuleWorkloadVO calculated = result.get(0);
            assertThat(calculated.getModuleWorkload()).isEqualByComparingTo("43.88");
            assertThat(calculated.getDataVolumeWeight()).isEqualByComparingTo("1.5");
            assertThat(calculated.getUserCountWeight()).isEqualByComparingTo("1.5");
        }

        @Test
        @DisplayName("WC-MOD-002: 模块工作量计算-null系数")
        void shouldUseOneForNullWeights() {
            // Given
            ModuleWorkloadVO module = createModuleWorkloadInput(1L, "测试模块", new BigDecimal("10"), new BigDecimal("2.0"));

            // When
            List<ModuleWorkloadVO> result = workloadCalculationService.calculateModuleWorkloads(
                    Collections.singletonList(module), null, null);

            // Then: 10 × 2.0 × 1 × 1 = 20.00
            assertThat(result.get(0).getModuleWorkload()).isEqualByComparingTo("20.00");
        }

        @Test
        @DisplayName("WC-MOD-003: 模块工作量计算-空列表")
        void shouldReturnEmptyListForEmptyInput() {
            // When
            List<ModuleWorkloadVO> result = workloadCalculationService.calculateModuleWorkloads(
                    Collections.emptyList(), new BigDecimal("1.5"), new BigDecimal("1.5"));

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ========== calculateWorkload 集成测试 (WC-FULL-*) ==========

    @Nested
    @DisplayName("calculateWorkload 集成测试 (WC-FULL-*)")
    class CalculateWorkloadIntegrationTests {

        @Test
        @DisplayName("WC-FULL-001: 完整评估流程")
        void shouldCalculateFullWorkload() {
            // Given
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);
            when(reportConfigService.getValueAsBigDecimal(eq("report_workload_per_unit"), any()))
                    .thenReturn(new BigDecimal("0.5"));
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);

            // When
            WorkloadResultVO result = workloadCalculationService.calculateWorkload(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getProjectId()).isEqualTo(1L);
            assertThat(result.getDataVolumeWeight()).isEqualByComparingTo("1.5");
            assertThat(result.getUserCountWeight()).isEqualByComparingTo("1.5");
            assertThat(result.getReportCoefficient()).isEqualByComparingTo("0.5");
            assertThat(result.getModuleWorkloads()).hasSize(2);
            assertThat(result.getCoreWorkload()).isEqualByComparingTo("111.38"); // 43.88 + 67.50
            assertThat(result.getReportWorkload()).isEqualByComparingTo("25.00"); // 50 × 0.5
            assertThat(result.getCustomDevWorkload()).isEqualByComparingTo("20.00");
            assertThat(result.getTotalWorkload()).isEqualByComparingTo("156.38"); // 111.38 + 25.00 + 20.00
        }

        @Test
        @DisplayName("WC-FULL-002: 评估不存在抛出异常")
        void shouldThrowExceptionWhenEvaluationNotFound() {
            // Given
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> workloadCalculationService.calculateWorkload(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("评估记录不存在");
        }

        @Test
        @DisplayName("WC-FULL-003: 无模块配置抛出异常")
        void shouldThrowExceptionWhenNoModules() {
            // Given
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(dataVolumeLadderMapper.selectById(any())).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(any())).thenReturn(testUserCountLadder);
            when(reportConfigService.getValueAsBigDecimal(eq("report_workload_per_unit"), any()))
                    .thenReturn(new BigDecimal("0.5"));
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            // When & Then
            assertThatThrownBy(() -> workloadCalculationService.calculateWorkload(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("请至少选择一个模块");
        }

        @Test
        @DisplayName("WC-FULL-004: 无客开情况")
        void shouldCalculateWithoutCustomDev() {
            // Given
            testEvaluation.setHasCustomDev(0);
            testEvaluation.setCustomDevWorkload(null);

            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(dataVolumeLadderMapper.selectById(any())).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(any())).thenReturn(testUserCountLadder);
            when(reportConfigService.getValueAsBigDecimal(eq("report_workload_per_unit"), any()))
                    .thenReturn(new BigDecimal("0.5"));
            when(moduleConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testModuleConfigs);
            when(moduleMapper.selectBatchIds(any())).thenReturn(testModules);

            // When
            WorkloadResultVO result = workloadCalculationService.calculateWorkload(1L);

            // Then
            assertThat(result.getCustomDevWorkload()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getTotalWorkload()).isEqualByComparingTo("136.38"); // 111.38 + 25.00 + 0
        }
    }

    // ========== 积分计算测试 (INT-*) ==========

    @Nested
    @DisplayName("积分计算测试 (INT-*)")
    class IntegrationCalculationTests {

        @Test
        @DisplayName("INT-001: 财务管理模块(15天,1.3)")
        void shouldCalculateFinanceModuleWorkload() {
            // Given: 15天×1.3×1.5×1.5=43.875
            ModuleWorkloadVO module = createModuleWorkload(1L, "财务管理", new BigDecimal("15"),
                    new BigDecimal("1.3"), new BigDecimal("1.5"), new BigDecimal("1.5"));

            // When
            BigDecimal result = workloadCalculationService.calculateCoreWorkload(Collections.singletonList(module));

            // Then: 43.88
            assertThat(result).isEqualByComparingTo("43.88");
        }

        @Test
        @DisplayName("INT-002: 供应链管理模块(20天,1.5)")
        void shouldCalculateSupplyModuleWorkload() {
            // Given: 20天×1.5×1.5×1.5=67.5
            ModuleWorkloadVO module = createModuleWorkload(2L, "供应链管理", new BigDecimal("20"),
                    new BigDecimal("1.5"), new BigDecimal("1.5"), new BigDecimal("1.5"));

            // When
            BigDecimal result = workloadCalculationService.calculateCoreWorkload(Collections.singletonList(module));

            // Then: 67.50
            assertThat(result).isEqualByComparingTo("67.50");
        }

        @Test
        @DisplayName("INT-003: 完整核心工作量")
        void shouldCalculateTotalCoreWorkload() {
            // Given: 43.88 + 67.50 = 111.38
            ModuleWorkloadVO module1 = createModuleWorkload(1L, "财务管理", new BigDecimal("15"),
                    new BigDecimal("1.3"), new BigDecimal("1.5"), new BigDecimal("1.5"));
            ModuleWorkloadVO module2 = createModuleWorkload(2L, "供应链管理", new BigDecimal("20"),
                    new BigDecimal("1.5"), new BigDecimal("1.5"), new BigDecimal("1.5"));

            // When
            BigDecimal result = workloadCalculationService.calculateCoreWorkload(Arrays.asList(module1, module2));

            // Then: 111.38
            assertThat(result).isEqualByComparingTo("111.38");
        }

        @Test
        @DisplayName("INT-004: 报表工作量")
        void shouldCalculateReportWorkload() {
            // Given: 50×0.5=25
            when(reportConfigService.getValueAsBigDecimal(eq("report_workload_per_unit"), any()))
                    .thenReturn(new BigDecimal("0.5"));

            // When
            BigDecimal result = workloadCalculationService.calculateReportWorkload(50);

            // Then: 25.00
            assertThat(result).isEqualByComparingTo("25.00");
        }

        @Test
        @DisplayName("INT-005: 客开工作量")
        void shouldCalculateCustomDevWorkload() {
            // When
            BigDecimal result = workloadCalculationService.calculateCustomDevWorkload(true, new BigDecimal("20"));

            // Then: 20.00
            assertThat(result).isEqualByComparingTo("20.00");
        }

        @Test
        @DisplayName("INT-006: 总工作量")
        void shouldCalculateTotalWorkload() {
            // Given: 111.38 + 25 + 20 = 156.38
            // When
            BigDecimal result = workloadCalculationService.calculateTotalWorkload(
                    new BigDecimal("111.38"),
                    new BigDecimal("25.00"),
                    new BigDecimal("20.00"));

            // Then: 156.38
            assertThat(result).isEqualByComparingTo("156.38");
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 创建已计算的模块工作量（用于calculateCoreWorkload测试）
     */
    private ModuleWorkloadVO createModuleWorkload(Long moduleId, String moduleName,
                                                   BigDecimal baseWorkload, BigDecimal weight,
                                                   BigDecimal dataVolumeWeight, BigDecimal userCountWeight) {
        // 先创建输入对象
        ModuleWorkloadVO module = createModuleWorkloadInput(moduleId, moduleName, baseWorkload, weight);

        // 计算模块工作量
        BigDecimal moduleWorkload = baseWorkload
                .multiply(weight)
                .multiply(dataVolumeWeight)
                .multiply(userCountWeight)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        module.setDataVolumeWeight(dataVolumeWeight);
        module.setUserCountWeight(userCountWeight);
        module.setModuleWorkload(moduleWorkload);

        return module;
    }

    /**
     * 创建模块工作量输入对象（未计算moduleWorkload）
     */
    private ModuleWorkloadVO createModuleWorkloadInput(Long moduleId, String moduleName,
                                                        BigDecimal baseWorkload, BigDecimal weight) {
        ModuleWorkloadVO module = new ModuleWorkloadVO();
        module.setModuleId(moduleId);
        module.setModuleName(moduleName);
        module.setBaseWorkload(baseWorkload);
        module.setWeight(weight);
        return module;
    }
}
