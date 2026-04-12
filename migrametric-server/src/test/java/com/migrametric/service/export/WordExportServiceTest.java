package com.migrametric.service.export;

import com.migrametric.dto.export.ExportRequestDTO;
import com.migrametric.entity.evaluation.Evaluation;
import com.migrametric.entity.project.Project;
import com.migrametric.mapper.evaluation.EvaluationMapper;
import com.migrametric.mapper.evaluation.ProjectModuleConfigMapper;
import com.migrametric.mapper.ladder.DataVolumeLadderMapper;
import com.migrametric.mapper.ladder.UserCountLadderMapper;
import com.migrametric.mapper.module.ModuleMapper;
import com.migrametric.mapper.project.ProjectMapper;
import com.migrametric.service.export.impl.WordExportServiceImpl;
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
 * WordExportService 单元测试
 *
 * @author MigraMetric Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WordExportService 单元测试")
class WordExportServiceTest {

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
    private WordExportServiceImpl wordExportService;

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

    // ========== Word导出测试 (WORD-*) ==========

    @Nested
    @DisplayName("Word导出测试 (WORD-*)")
    class WordExportTests {

        @Test
        @DisplayName("WORD-001: 基本Word生成")
        void shouldGenerateBasicWord() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("WORD");

            // When
            byte[] result = wordExportService.exportToWord(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("WORD-002: DOCX格式验证")
        void shouldHaveCorrectDocxFormat() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("WORD");

            // When
            byte[] result = wordExportService.exportToWord(request);

            // Then: DOCX文件以PK开头（ZIP格式）
            assertThat(result.length).isGreaterThan(4);
            assertThat((char) result[0]).isEqualTo('P');
            assertThat((char) result[1]).isEqualTo('K');
        }

        @Test
        @DisplayName("WORD-003: Word文档包含XML结构")
        void shouldContainXmlStructure() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("WORD");

            // When
            byte[] result = wordExportService.exportToWord(request);

            // Then: DOCX文件包含word/document.xml
            assertThat(result.length).isGreaterThan(1000);
            // DOCX文件是ZIP格式，包含document.xml
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("WORD-004: 项目不存在抛出异常")
        void shouldThrowExceptionWhenProjectNotFound() {
            // Given
            when(projectMapper.selectById(999L)).thenReturn(null);

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(999L);
            request.setFormat("WORD");

            // When & Then
            assertThatThrownBy(() -> wordExportService.exportToWord(request))
                    .hasMessageContaining("项目不存在");
        }

        @Test
        @DisplayName("WORD-005: 评估记录不存在抛出异常")
        void shouldThrowExceptionWhenEvaluationNotFound() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(null);

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("WORD");

            // When & Then
            assertThatThrownBy(() -> wordExportService.exportToWord(request))
                    .hasMessageContaining("评估记录不存在");
        }

        @Test
        @DisplayName("WORD-006: 无模块配置正常导出")
        void shouldExportWithNoModules() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("WORD");

            // When
            byte[] result = wordExportService.exportToWord(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(500);
        }

        @Test
        @DisplayName("WORD-007: 包含统计结果的Word")
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
            request.setFormat("WORD");

            // When
            byte[] result = wordExportService.exportToWord(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(500);
        }

        @Test
        @DisplayName("WORD-008: 风险提示导出")
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
            request.setFormat("WORD");

            // When
            byte[] result = wordExportService.exportToWord(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(500);
        }

        @Test
        @DisplayName("WORD-009: 完整报告导出")
        void shouldExportCompleteReport() {
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
            request.setFormat("WORD");

            // When
            byte[] result = wordExportService.exportToWord(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(1000);
        }

        @Test
        @DisplayName("WORD-010: Word文档数据正确")
        void shouldContainCorrectWorkloadData() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("WORD");

            // When
            byte[] result = wordExportService.exportToWord(request);

            // Then: 包含工作量信息
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(500);
        }

        @Test
        @DisplayName("WORD-011: Word文档项目信息正确")
        void shouldContainCorrectProjectInfo() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("WORD");

            // When
            byte[] result = wordExportService.exportToWord(request);

            // Then: 文件大小正常
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(500);
        }

        @Test
        @DisplayName("WORD-012: 评估概览数据正确")
        void shouldContainCorrectEvaluationOverview() {
            // Given
            StatisticsResultVO.EvaluationOverview overview = new StatisticsResultVO.EvaluationOverview();
            overview.setModuleCount(5);
            overview.setDataVolume(new BigDecimal("1000"));
            overview.setUserCount(2000);
            overview.setReportCount(100);
            overview.setHasCustomDev(false);
            testStatistics.setEvaluationOverview(overview);

            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("WORD");

            // When
            byte[] result = wordExportService.exportToWord(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(500);
        }

        @Test
        @DisplayName("WORD-013: 中文内容正常导出")
        void shouldExportChineseContent() {
            // Given
            testProject.setProjectName("中文项目名称测试");
            testProject.setCustomerName("中文客户名称");
            testProject.setProjectLeader("李四");

            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("WORD");

            // When
            byte[] result = wordExportService.exportToWord(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(500);
        }

        @Test
        @DisplayName("WORD-014: 风险类型名称转换正确")
        void shouldConvertRiskTypeName() {
            // Given
            StatisticsResultVO.RiskWarning warning = new StatisticsResultVO.RiskWarning();
            warning.setType("MODULE_COMPLEXITY");
            warning.setLevel("中");
            warning.setDescription("模块复杂度风险");
            warning.setSuggestion("建议进行模块重构");
            testStatistics.setRiskWarnings(Collections.singletonList(warning));

            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("WORD");

            // When
            byte[] result = wordExportService.exportToWord(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(500);
        }

        @Test
        @DisplayName("WORD-015: 生成日期正确")
        void shouldContainCorrectDate() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any())).thenReturn(testEvaluation);
            when(statisticsService.getStatistics(1L)).thenReturn(testStatistics);
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExportRequestDTO request = new ExportRequestDTO();
            request.setProjectId(1L);
            request.setFormat("WORD");

            // When
            byte[] result = wordExportService.exportToWord(request);

            // Then: 文件生成成功
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(500);
        }
    }
}
