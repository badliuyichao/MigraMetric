package com.migrametric.service.export;

import com.migrametric.dto.export.ExportRequestDTO;
import com.migrametric.entity.evaluation.Evaluation;
import com.migrametric.entity.evaluation.ProjectModuleConfig;
import com.migrametric.entity.ladder.DataVolumeLadder;
import com.migrametric.entity.ladder.UserCountLadder;
import com.migrametric.entity.module.Module;
import com.migrametric.entity.project.Project;
import com.migrametric.mapper.evaluation.EvaluationMapper;
import com.migrametric.mapper.evaluation.ProjectModuleConfigMapper;
import com.migrametric.mapper.ladder.DataVolumeLadderMapper;
import com.migrametric.mapper.ladder.UserCountLadderMapper;
import com.migrametric.mapper.module.ModuleMapper;
import com.migrametric.mapper.project.ProjectMapper;
import com.migrametric.service.export.impl.ExcelExportServiceImpl;
import com.migrametric.service.statistics.StatisticsService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * ExcelExportService 单元测试
 *
 * @author MigraMetric Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExcelExportService 单元测试")
class ExcelExportServiceTest {

    @Mock
    private ProjectMapper projectMapper;

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
    private StatisticsService statisticsService;

    @InjectMocks
    private ExcelExportServiceImpl excelExportService;

    // ========== 测试数据 ==========

    private Project testProject;
    private Evaluation testEvaluation;
    private StatisticsResultVO testStatistics;

    @BeforeEach
    void setUp() {
        // 测试项目
        testProject = new Project();
        testProject.setId(1L);
        testProject.setProjectName("ERP迁移项目");
        testProject.setCustomerName("XX公司");
        testProject.setProjectLeader("张三");
        testProject.setContact("13800138000");
        testProject.setSourceSystemId(1L);
        testProject.setTargetSystemId(2L);

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

        // 测试统计结果
        testStatistics = new StatisticsResultVO();
        testStatistics.setProjectId(1L);
        testStatistics.setTotalWorkload(new BigDecimal("156.38"));
        testStatistics.setEstimatedMonths(new BigDecimal("7.11"));
    }

    // ========== Excel导出测试 (EXCEL-*) ==========

    @Nested
    @DisplayName("Excel导出测试 (EXCEL-*)")
    class ExcelExportTests {

        @Test
        @DisplayName("EXCEL-001: 基本Excel生成")
        void shouldGenerateBasicExcel() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(dataVolumeLadderMapper.selectById(any())).thenReturn(null);
            when(userCountLadderMapper.selectById(any())).thenReturn(null);

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("EXCEL");

            // When
            byte[] result = excelExportService.exportToExcel(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("EXCEL-002: 包含模块数据的Excel")
        void shouldGenerateExcelWithModules() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);

            // 模块配置
            ProjectModuleConfig config = new ProjectModuleConfig();
            config.setId(1L);
            config.setProjectId(1L);
            config.setModuleId(1L);
            config.setWeight(new BigDecimal("1.3"));
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.singletonList(config));

            // 模块
            Module module = new Module();
            module.setId(1L);
            module.setModuleName("财务管理");
            module.setCategory("财务");
            module.setBaseWorkload(new BigDecimal("15"));
            module.setDefaultWeight(new BigDecimal("1.3"));
            when(moduleMapper.selectBatchIds(any())).thenReturn(Collections.singletonList(module));

            // 阶梯
            DataVolumeLadder dvLadder = new DataVolumeLadder();
            dvLadder.setId(2L);
            dvLadder.setWeight(new BigDecimal("1.0"));
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(dvLadder);

            UserCountLadder ucLadder = new UserCountLadder();
            ucLadder.setId(2L);
            ucLadder.setWeight(new BigDecimal("1.2"));
            when(userCountLadderMapper.selectById(2L)).thenReturn(ucLadder);

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("EXCEL");

            // When
            byte[] result = excelExportService.exportToExcel(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("EXCEL-003: Excel文件头验证")
        void shouldHaveCorrectExcelHeader() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(dataVolumeLadderMapper.selectById(any())).thenReturn(null);
            when(userCountLadderMapper.selectById(any())).thenReturn(null);

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("EXCEL");

            // When
            byte[] result = excelExportService.exportToExcel(request);

            // Then: Excel文件以PK开头（ZIP格式）
            assertThat(result[0]).isEqualTo((byte) 'P');
            assertThat(result[1]).isEqualTo((byte) 'K');
        }

        @Test
        @DisplayName("EXCEL-004: 项目不存在抛出异常")
        void shouldThrowExceptionWhenProjectNotFound() {
            // Given
            when(projectMapper.selectById(999L)).thenReturn(null);

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(999L);
            request.setFormat("EXCEL");

            // When & Then
            assertThatThrownBy(() -> excelExportService.exportToExcel(request))
                    .hasMessageContaining("项目不存在");
        }

        @Test
        @DisplayName("EXCEL-005: 评估记录不存在抛出异常")
        void shouldThrowExceptionWhenEvaluationNotFound() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(null);

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("EXCEL");

            // When & Then
            assertThatThrownBy(() -> excelExportService.exportToExcel(request))
                    .hasMessageContaining("评估记录不存在");
        }

        @Test
        @DisplayName("EXCEL-006: 无模块配置正常导出")
        void shouldExportWithNoModules() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(dataVolumeLadderMapper.selectById(any())).thenReturn(null);
            when(userCountLadderMapper.selectById(any())).thenReturn(null);

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("EXCEL");

            // When
            byte[] result = excelExportService.exportToExcel(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("EXCEL-007: 多Sheet生成验证")
        void shouldGenerateMultipleSheets() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(dataVolumeLadderMapper.selectById(any())).thenReturn(null);
            when(userCountLadderMapper.selectById(any())).thenReturn(null);

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("EXCEL");

            // When
            byte[] result = excelExportService.exportToExcel(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(5000); // 多Sheet应该足够大
        }

        @Test
        @DisplayName("EXCEL-008: 统计结果集成")
        void shouldIntegrateStatisticsResult() {
            // Given
            StatisticsResultVO.WorkloadTypeDistribution dist = new StatisticsResultVO.WorkloadTypeDistribution();
            dist.setType("核心迁移");
            dist.setWorkload(new BigDecimal("111.38"));
            dist.setPercentage(new BigDecimal("71.3"));
            testStatistics.setWorkloadTypeDistribution(Collections.singletonList(dist));

            StatisticsResultVO.EvaluationOverview overview = new StatisticsResultVO.EvaluationOverview();
            overview.setModuleCount(2);
            overview.setDataVolume(new BigDecimal("500"));
            overview.setUserCount(600);
            overview.setReportCount(50);
            overview.setHasCustomDev(true);
            testStatistics.setEvaluationOverview(overview);

            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(dataVolumeLadderMapper.selectById(any())).thenReturn(null);
            when(userCountLadderMapper.selectById(any())).thenReturn(null);

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("EXCEL");

            // When
            byte[] result = excelExportService.exportToExcel(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }
    }
}
