package com.migrametric.controller;

import com.migrametric.common.Result;
import com.migrametric.controller.evaluation.EvaluationController;
import com.migrametric.dto.evaluation.EvaluationCreateDTO;
import com.migrametric.dto.evaluation.EvaluationUpdateDTO;
import com.migrametric.dto.evaluation.ModuleConfigDTO;
import com.migrametric.service.evaluation.EvaluationService;
import com.migrametric.service.evaluation.ModuleConfigService;
import com.migrametric.service.evaluation.WorkloadCalculationService;
import com.migrametric.vo.evaluation.EvaluationVO;
import com.migrametric.vo.evaluation.ModuleConfigVO;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * EvaluationController 集成测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EvaluationController 测试")
class EvaluationControllerTest {

    @Mock
    private EvaluationService evaluationService;

    @Mock
    private ModuleConfigService moduleConfigService;

    @Mock
    private WorkloadCalculationService workloadCalculationService;

    @InjectMocks
    private EvaluationController evaluationController;

    private EvaluationVO mockEvaluationVO;
    private WorkloadResultVO mockWorkloadResultVO;

    @BeforeEach
    void setUp() {
        mockEvaluationVO = new EvaluationVO();
        mockEvaluationVO.setId(1L);
        mockEvaluationVO.setProjectId(1L);
        mockEvaluationVO.setTableCount(100);
        mockEvaluationVO.setDataVolume(new BigDecimal("500"));
        mockEvaluationVO.setDataVolumeLadderName("中型");
        mockEvaluationVO.setDataVolumeWeight(new BigDecimal("1.0"));
        mockEvaluationVO.setUserCount(200);
        mockEvaluationVO.setUserCountLadderName("中规模");
        mockEvaluationVO.setUserCountWeight(new BigDecimal("1.2"));
        mockEvaluationVO.setReportCount(50);
        mockEvaluationVO.setHasCustomDev(true);
        mockEvaluationVO.setCustomDevCount(10);
        mockEvaluationVO.setCustomDevWorkload(new BigDecimal("50"));
        mockEvaluationVO.setTotalWorkload(new BigDecimal("231.38"));
        mockEvaluationVO.setEvaluationStatus("COMPLETED");

        mockWorkloadResultVO = new WorkloadResultVO();
        mockWorkloadResultVO.setProjectId(1L);
        mockWorkloadResultVO.setCoreWorkload(new BigDecimal("156.38"));
        mockWorkloadResultVO.setReportWorkload(new BigDecimal("25.0"));
        mockWorkloadResultVO.setCustomDevWorkload(new BigDecimal("50.0"));
        mockWorkloadResultVO.setTotalWorkload(new BigDecimal("231.38"));
        mockWorkloadResultVO.setDataVolumeWeight(new BigDecimal("1.0"));
        mockWorkloadResultVO.setUserCountWeight(new BigDecimal("1.2"));
        mockWorkloadResultVO.setReportCoefficient(new BigDecimal("0.5"));
    }

    @Nested
    @DisplayName("创建评估测试")
    class CreateEvaluationTests {

        @Test
        @DisplayName("应该成功创建评估记录")
        void shouldCreateEvaluationSuccessfully() {
            // Given
            EvaluationCreateDTO createDTO = new EvaluationCreateDTO();
            createDTO.setProjectId(1L);
            when(evaluationService.createEvaluation(any(EvaluationCreateDTO.class))).thenReturn(1L);

            // When
            Result<Long> result = evaluationController.createEvaluation(createDTO);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(1L, result.getData());
            verify(evaluationService, times(1)).createEvaluation(any(EvaluationCreateDTO.class));
        }
    }

    @Nested
    @DisplayName("获取评估详情测试")
    class GetEvaluationTests {

        @Test
        @DisplayName("应该成功获取评估详情")
        void shouldGetEvaluationSuccessfully() {
            // Given
            when(evaluationService.getByProjectId(1L)).thenReturn(mockEvaluationVO);

            // When
            Result<EvaluationVO> result = evaluationController.getEvaluation(1L);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            assertEquals(100, result.getData().getTableCount());
            verify(evaluationService, times(1)).getByProjectId(1L);
        }

        @Test
        @DisplayName("评估不存在时应该返回null")
        void shouldReturnNullWhenEvaluationNotFound() {
            // Given
            when(evaluationService.getByProjectId(999L)).thenReturn(null);

            // When
            Result<EvaluationVO> result = evaluationController.getEvaluation(999L);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
        }
    }

    @Nested
    @DisplayName("保存评估指标测试")
    class SaveIndicatorsTests {

        @Test
        @DisplayName("应该成功保存评估指标")
        void shouldSaveIndicatorsSuccessfully() {
            // Given
            EvaluationUpdateDTO updateDTO = new EvaluationUpdateDTO();
            updateDTO.setTableCount(150);
            updateDTO.setDataVolume(new BigDecimal("800"));
            updateDTO.setUserCount(300);
            updateDTO.setReportCount(60);
            updateDTO.setHasCustomDev(true);
            updateDTO.setCustomDevCount(10);
            updateDTO.setCustomDevWorkload(new BigDecimal("75"));

            doNothing().when(evaluationService).saveIndicators(eq(1L), any(EvaluationUpdateDTO.class));

            // When
            Result<Void> result = evaluationController.saveIndicators(1L, updateDTO);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(evaluationService, times(1)).saveIndicators(eq(1L), any(EvaluationUpdateDTO.class));
        }
    }

    @Nested
    @DisplayName("模块配置测试")
    class ModuleConfigTests {

        @Test
        @DisplayName("应该获取项目可选模块列表")
        void shouldGetProjectModules() {
            // Given
            ModuleConfigVO moduleVO = new ModuleConfigVO();
            moduleVO.setModuleId(1L);
            moduleVO.setModuleName("财务会计");
            moduleVO.setBaseWorkload(new BigDecimal("20"));
            moduleVO.setDefaultWeight(new BigDecimal("1.0"));

            when(moduleConfigService.getProjectModules(1L)).thenReturn(Arrays.asList(moduleVO));

            // When
            Result<List<ModuleConfigVO>> result = evaluationController.getProjectModules(1L);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(1, result.getData().size());
            verify(moduleConfigService, times(1)).getProjectModules(1L);
        }

        @Test
        @DisplayName("应该保存模块配置")
        void shouldSaveModuleConfig() {
            // Given
            ModuleConfigDTO configDTO = new ModuleConfigDTO();
            configDTO.setModuleId(1L);
            configDTO.setModuleName("财务会计");
            configDTO.setWeight(new BigDecimal("1.2"));
            configDTO.setChecked(true);

            doNothing().when(moduleConfigService).saveModuleConfig(eq(1L), anyList());

            // When
            Result<Void> result = evaluationController.saveModuleConfig(1L, Arrays.asList(configDTO));

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(moduleConfigService, times(1)).saveModuleConfig(eq(1L), anyList());
        }

        @Test
        @DisplayName("应该获取已配置的模块")
        void shouldGetConfiguredModules() {
            // Given
            ModuleConfigVO moduleVO = new ModuleConfigVO();
            moduleVO.setModuleId(1L);
            moduleVO.setModuleName("财务会计");
            moduleVO.setChecked(true);

            when(moduleConfigService.getConfiguredModules(1L)).thenReturn(Arrays.asList(moduleVO));

            // When
            Result<List<ModuleConfigVO>> result = evaluationController.getConfiguredModules(1L);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(1, result.getData().size());
            verify(moduleConfigService, times(1)).getConfiguredModules(1L);
        }

        @Test
        @DisplayName("应该统计已选模块数量")
        void shouldCountSelectedModules() {
            // Given
            when(moduleConfigService.getConfiguredModules(1L)).thenReturn(Arrays.asList(
                new ModuleConfigVO(), new ModuleConfigVO(), new ModuleConfigVO()
            ));

            // When
            Result<Integer> result = evaluationController.countSelectedModules(1L);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(3, result.getData());
        }
    }

    @Nested
    @DisplayName("工作量计算测试")
    class WorkloadCalculationTests {

        @Test
        @DisplayName("应该成功计算工作量")
        void shouldCalculateWorkloadSuccessfully() {
            // Given
            when(workloadCalculationService.calculateWorkload(1L)).thenReturn(mockWorkloadResultVO);

            // When
            Result<WorkloadResultVO> result = evaluationController.calculateWorkload(1L);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            assertEquals(new BigDecimal("231.38"), result.getData().getTotalWorkload());
            verify(workloadCalculationService, times(1)).calculateWorkload(1L);
        }

        @Test
        @DisplayName("计算工作量时应该使用正确的项目ID")
        void shouldUseCorrectProjectIdForCalculation() {
            // Given
            when(workloadCalculationService.calculateWorkload(5L)).thenReturn(mockWorkloadResultVO);

            // When
            evaluationController.calculateWorkload(5L);

            // Then
            verify(workloadCalculationService, times(1)).calculateWorkload(5L);
            verify(workloadCalculationService, never()).calculateWorkload(1L);
        }
    }

    @Nested
    @DisplayName("完成评估测试")
    class CompleteEvaluationTests {

        @Test
        @DisplayName("应该成功完成评估")
        void shouldCompleteEvaluationSuccessfully() {
            // Given
            doNothing().when(evaluationService).completeEvaluation(1L);

            // When
            Result<Void> result = evaluationController.completeEvaluation(1L);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(evaluationService, times(1)).completeEvaluation(1L);
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("空模块列表应该被处理")
        void shouldHandleEmptyModuleList() {
            // Given
            when(moduleConfigService.getConfiguredModules(1L)).thenReturn(Arrays.asList());

            // When
            Result<Integer> result = evaluationController.countSelectedModules(1L);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(0, result.getData());
        }
    }
}
