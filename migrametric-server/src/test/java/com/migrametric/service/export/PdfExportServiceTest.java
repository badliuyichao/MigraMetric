package com.migrametric.service.export;

import com.migrametric.dto.export.ExportRequestDTO;
import com.migrametric.entity.evaluation.Evaluation;
import com.migrametric.entity.ladder.DataVolumeLadder;
import com.migrametric.entity.ladder.UserCountLadder;
import com.migrametric.entity.project.Project;
import com.migrametric.mapper.evaluation.EvaluationMapper;
import com.migrametric.mapper.evaluation.ProjectModuleConfigMapper;
import com.migrametric.mapper.ladder.DataVolumeLadderMapper;
import com.migrametric.mapper.ladder.UserCountLadderMapper;
import com.migrametric.mapper.module.ModuleMapper;
import com.migrametric.mapper.project.ProjectMapper;
import com.migrametric.service.export.impl.PdfExportServiceImpl;
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
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * PdfExportService 单元测试
 *
 * @author MigraMetric Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PdfExportService 单元测试")
class PdfExportServiceTest {

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
    private PdfExportServiceImpl pdfExportService;

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

    // ========== PDF导出测试 (PDF-*) ==========

    @Nested
    @DisplayName("PDF导出测试 (PDF-*)")
    class PdfExportTests {

        @Test
        @DisplayName("PDF-001: 基本PDF生成")
        void shouldGenerateBasicPdf() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("PDF");

            // When
            byte[] result = pdfExportService.exportToPdf(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("PDF-002: PDF文件头验证")
        void shouldHaveCorrectPdfHeader() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("PDF");

            // When
            byte[] result = pdfExportService.exportToPdf(request);

            // Then: PDF文件以%PDF开头
            String header = new String(result, 0, Math.min(4, result.length));
            assertThat(header).startsWith("%PDF");
        }

        @Test
        @DisplayName("PDF-003: PDF版本验证")
        void shouldHaveCorrectPdfVersion() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("PDF");

            // When
            byte[] result = pdfExportService.exportToPdf(request);

            // Then: PDF文件包含版本信息
            String content = new String(result);
            assertThat(content).contains("PDF-");
        }

        @Test
        @DisplayName("PDF-004: 项目不存在抛出异常")
        void shouldThrowExceptionWhenProjectNotFound() {
            // Given
            when(projectMapper.selectById(999L)).thenReturn(null);

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(999L);
            request.setFormat("PDF");

            // When & Then
            assertThatThrownBy(() -> pdfExportService.exportToPdf(request))
                    .hasMessageContaining("项目不存在");
        }

        @Test
        @DisplayName("PDF-005: 评估记录不存在抛出异常")
        void shouldThrowExceptionWhenEvaluationNotFound() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(null);

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("PDF");

            // When & Then
            assertThatThrownBy(() -> pdfExportService.exportToPdf(request))
                    .hasMessageContaining("评估记录不存在");
        }

        @Test
        @DisplayName("PDF-006: 无模块配置正常导出")
        void shouldExportWithNoModules() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("PDF");

            // When
            byte[] result = pdfExportService.exportToPdf(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(1000);
        }

        @Test
        @DisplayName("PDF-007: 包含统计结果的PDF")
        void shouldExportWithStatisticsResult() {
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

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("PDF");

            // When
            byte[] result = pdfExportService.exportToPdf(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(1000);
        }

        @Test
        @DisplayName("PDF-008: 风险提示导出")
        void shouldExportWithRiskWarnings() {
            // Given
            StatisticsResultVO.RiskWarning warning = new StatisticsResultVO.RiskWarning();
            warning.setType("CUSTOM_DEV");
            warning.setLevel("高");
            warning.setDescription("客开风险");
            warning.setSuggestion("建议...");
            testStatistics.setRiskWarnings(Collections.singletonList(warning));

            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("PDF");

            // When
            byte[] result = pdfExportService.exportToPdf(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(1000);
        }
    }
}
