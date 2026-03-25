package com.migrametric.service.evaluation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.migrametric.common.BusinessException;
import com.migrametric.dto.evaluation.EvaluationCreateDTO;
import com.migrametric.dto.evaluation.EvaluationUpdateDTO;
import com.migrametric.entity.evaluation.Evaluation;
import com.migrametric.entity.ladder.DataVolumeLadder;
import com.migrametric.entity.ladder.UserCountLadder;
import com.migrametric.entity.project.Project;
import com.migrametric.mapper.evaluation.EvaluationMapper;
import com.migrametric.mapper.evaluation.ProjectModuleConfigMapper;
import com.migrametric.mapper.ladder.DataVolumeLadderMapper;
import com.migrametric.mapper.ladder.UserCountLadderMapper;
import com.migrametric.mapper.project.ProjectMapper;
import com.migrametric.service.evaluation.impl.EvaluationServiceImpl;
import com.migrametric.vo.evaluation.EvaluationVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * EvaluationService 单元测试
 * <p>
 * 测试评估服务的核心功能，包括：
 * - 评估记录查询
 * - 评估创建
 * - 评估指标保存
 * - 业务规则验证
 * </p>
 *
 * @author MigraMetric Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EvaluationService 单元测试")
class EvaluationServiceTest {

    @Mock
    private EvaluationMapper evaluationMapper;

    @Mock
    private ProjectModuleConfigMapper moduleConfigMapper;

    @Mock
    private DataVolumeLadderMapper dataVolumeLadderMapper;

    @Mock
    private UserCountLadderMapper userCountLadderMapper;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private EvaluationServiceImpl evaluationService;

    // ========== 测试数据 ==========

    private Project testProject;
    private Evaluation testEvaluation;
    private DataVolumeLadder testDataVolumeLadder;
    private UserCountLadder testUserCountLadder;

    @BeforeEach
    void setUp() {
        // 测试项目
        testProject = new Project();
        testProject.setId(1L);
        testProject.setProjectName("测试项目");
        testProject.setSourceSystemId(1L);
        testProject.setTargetSystemId(2L);

        // 测试评估记录
        testEvaluation = new Evaluation();
        testEvaluation.setId(1L);
        testEvaluation.setProjectId(1L);
        testEvaluation.setTableCount(100);
        testEvaluation.setDataVolume(new BigDecimal("500"));
        testEvaluation.setDataVolumeLadderId(2L);
        testEvaluation.setUserCount(500);
        testEvaluation.setUserCountLadderId(2L);
        testEvaluation.setReportCount(50);
        testEvaluation.setHasCustomDev(1);
        testEvaluation.setCustomDevCount(2);
        testEvaluation.setCustomDevWorkload(new BigDecimal("20"));
        testEvaluation.setEvaluationStatus("DRAFT");
        testEvaluation.setCreateTime(LocalDateTime.now());
        testEvaluation.setUpdateTime(LocalDateTime.now());

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
    }

    // ========== getByProjectId 测试 (EVAL-QR-*) ==========

    @Nested
    @DisplayName("getByProjectId 测试 (EVAL-QR-*)")
    class GetByProjectIdTests {

        @Test
        @DisplayName("EVAL-QR-001: 查询存在的评估详情")
        void shouldReturnEvaluationWhenExists() {
            // Given
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(dataVolumeLadderMapper.selectById(2L)).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(2L)).thenReturn(testUserCountLadder);

            // When
            EvaluationVO result = evaluationService.getByProjectId(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getProjectId()).isEqualTo(1L);
            assertThat(result.getTableCount()).isEqualTo(100);
            assertThat(result.getDataVolume()).isEqualByComparingTo("500");
            assertThat(result.getUserCount()).isEqualTo(500);
            assertThat(result.getReportCount()).isEqualTo(50);
            assertThat(result.getHasCustomDev()).isTrue();
            assertThat(result.getCustomDevCount()).isEqualTo(2);
            assertThat(result.getCustomDevWorkload()).isEqualByComparingTo("20");
            assertThat(result.getDataVolumeLadderName()).isEqualTo("中型");
            assertThat(result.getUserCountLadderName()).isEqualTo("中规模");
            assertThat(result.getEvaluationStatus()).isEqualTo("DRAFT");
            assertThat(result.getEvaluationStatusText()).isEqualTo("草稿");
        }

        @Test
        @DisplayName("EVAL-QR-002: 查询不存在的评估返回null")
        void shouldReturnNullWhenNotExists() {
            // Given
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // When
            EvaluationVO result = evaluationService.getByProjectId(999L);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("EVAL-QR-003: 评估状态文本正确转换")
        void shouldReturnCorrectStatusText() {
            // Given
            testEvaluation.setEvaluationStatus("IN_PROGRESS");
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(dataVolumeLadderMapper.selectById(any())).thenReturn(testDataVolumeLadder);
            when(userCountLadderMapper.selectById(any())).thenReturn(testUserCountLadder);

            // When
            EvaluationVO result = evaluationService.getByProjectId(1L);

            // Then
            assertThat(result.getEvaluationStatusText()).isEqualTo("进行中");
        }
    }

    // ========== createEvaluation 测试 (EVAL-CR-*) ==========

    @Nested
    @DisplayName("createEvaluation 测试 (EVAL-CR-*)")
    class CreateEvaluationTests {

        @Test
        @DisplayName("EVAL-CR-001: 创建评估记录成功")
        void shouldCreateEvaluationSuccess() {
            // Given
            EvaluationCreateDTO dto = new EvaluationCreateDTO();
            dto.setProjectId(1L);

            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);
            doAnswer(invocation -> {
                Evaluation eval = invocation.getArgument(0);
                eval.setId(1L);
                return 1;
            }).when(evaluationMapper).insert(any(Evaluation.class));

            // When
            Long evaluationId = evaluationService.createEvaluation(dto);

            // Then
            assertThat(evaluationId).isNotNull();
            assertThat(evaluationId).isEqualTo(1L);
            verify(evaluationMapper).insert(any(Evaluation.class));
            verify(evaluationMapper).exists(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("EVAL-CR-002: 项目不存在时抛出异常")
        void shouldThrowExceptionWhenProjectNotFound() {
            // Given
            EvaluationCreateDTO dto = new EvaluationCreateDTO();
            dto.setProjectId(999L);

            when(projectMapper.selectById(999L)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> evaluationService.createEvaluation(dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("项目不存在");
        }

        @Test
        @DisplayName("EVAL-CR-003: 评估已存在时抛出异常")
        void shouldThrowExceptionWhenEvaluationExists() {
            // Given
            EvaluationCreateDTO dto = new EvaluationCreateDTO();
            dto.setProjectId(1L);

            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> evaluationService.createEvaluation(dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("数据已存在");
        }

        @Test
        @DisplayName("EVAL-CR-004: 新建评估状态为DRAFT")
        void shouldSetDraftStatusForNewEvaluation() {
            // Given
            EvaluationCreateDTO dto = new EvaluationCreateDTO();
            dto.setProjectId(1L);

            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);

            // Capture the inserted evaluation
            doAnswer(invocation -> {
                Evaluation eval = invocation.getArgument(0);
                eval.setId(1L);
                return 1;
            }).when(evaluationMapper).insert(any(Evaluation.class));

            // When
            evaluationService.createEvaluation(dto);

            // Then
            verify(evaluationMapper).insert(argThat(eval ->
                    "DRAFT".equals(eval.getEvaluationStatus())
            ));
        }
    }

    // ========== saveIndicators 测试 (EVAL-UP-*) ==========

    @Nested
    @DisplayName("saveIndicators 测试 (EVAL-UP-*)")
    class SaveIndicatorsTests {

        @Test
        @DisplayName("EVAL-UP-001: 保存评估指标成功")
        void shouldSaveIndicatorsSuccess() {
            // Given
            EvaluationUpdateDTO dto = createTestIndicatorDTO();

            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(evaluationMapper.updateById(any(Evaluation.class))).thenReturn(1);

            // When
            evaluationService.saveIndicators(1L, dto);

            // Then
            verify(evaluationMapper).updateById(argThat(eval ->
                    eval.getTableCount().equals(dto.getTableCount()) &&
                            eval.getDataVolume().equals(dto.getDataVolume()) &&
                            eval.getUserCount().equals(dto.getUserCount()) &&
                            eval.getReportCount().equals(dto.getReportCount())
            ));
        }

        @Test
        @DisplayName("EVAL-UP-002: 项目不存在时抛出异常")
        void shouldThrowExceptionWhenProjectNotFound() {
            // Given
            EvaluationUpdateDTO dto = createTestIndicatorDTO();

            when(projectMapper.selectById(999L)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> evaluationService.saveIndicators(999L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("项目不存在");
        }

        @Test
        @DisplayName("EVAL-UP-003: 保存指标时状态变为IN_PROGRESS")
        void shouldSetInProgressStatusWhenSavingIndicators() {
            // Given
            EvaluationUpdateDTO dto = createTestIndicatorDTO();
            testEvaluation.setEvaluationStatus("DRAFT");

            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(evaluationMapper.updateById(any(Evaluation.class))).thenReturn(1);

            // When
            evaluationService.saveIndicators(1L, dto);

            // Then
            verify(evaluationMapper).updateById(argThat(eval ->
                    "IN_PROGRESS".equals(eval.getEvaluationStatus())
            ));
        }

        @Test
        @DisplayName("EVAL-UP-004: 保存指标时正确更新客开信息")
        void shouldSaveCustomDevInfoCorrectly() {
            // Given
            EvaluationUpdateDTO dto = createTestIndicatorDTO();
            dto.setHasCustomDev(1);
            dto.setCustomDevCount(3);
            dto.setCustomDevWorkload(new BigDecimal("30"));

            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(evaluationMapper.updateById(any(Evaluation.class))).thenReturn(1);

            // When
            evaluationService.saveIndicators(1L, dto);

            // Then
            verify(evaluationMapper).updateById(argThat(eval ->
                    eval.getHasCustomDev() == 1 &&
                            eval.getCustomDevCount() == 3 &&
                            eval.getCustomDevWorkload().compareTo(new BigDecimal("30")) == 0
            ));
        }

        @Test
        @DisplayName("EVAL-UP-005: 保存指标时正确更新数据清洗信息")
        void shouldSaveDataCleanInfoCorrectly() {
            // Given
            EvaluationUpdateDTO dto = createTestIndicatorDTO();
            dto.setDataCleanDesc("需要清洗历史重复数据");
            dto.setDataCleanComplexity(2);

            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testEvaluation);
            when(evaluationMapper.updateById(any(Evaluation.class))).thenReturn(1);

            // When
            evaluationService.saveIndicators(1L, dto);

            // Then
            verify(evaluationMapper).updateById(argThat(eval ->
                    "需要清洗历史重复数据".equals(eval.getDataCleanDesc()) &&
                            eval.getDataCleanComplexity() == 2
            ));
        }

        @Test
        @DisplayName("EVAL-UP-006: 评估不存在时创建新评估并保存指标")
        void shouldCreateNewEvaluationWhenNotExists() {
            // Given
            EvaluationUpdateDTO dto = createTestIndicatorDTO();

            when(projectMapper.selectById(1L)).thenReturn(testProject);
            when(evaluationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(evaluationMapper.insert(any(Evaluation.class))).thenReturn(1);

            // When
            evaluationService.saveIndicators(1L, dto);

            // Then
            verify(evaluationMapper).insert(argThat(eval ->
                    eval.getProjectId().equals(1L) &&
                            "IN_PROGRESS".equals(eval.getEvaluationStatus())
            ));
        }
    }

    // ========== existsByProjectId 测试 (EVAL-EX-*) ==========

    @Nested
    @DisplayName("existsByProjectId 测试 (EVAL-EX-*)")
    class ExistsByProjectIdTests {

        @Test
        @DisplayName("EVAL-EX-001: 评估存在时返回true")
        void shouldReturnTrueWhenEvaluationExists() {
            // Given
            when(evaluationMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(true);

            // When
            boolean result = evaluationService.existsByProjectId(1L);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("EVAL-EX-002: 评估不存在时返回false")
        void shouldReturnFalseWhenEvaluationNotExists() {
            // Given
            when(evaluationMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);

            // When
            boolean result = evaluationService.existsByProjectId(999L);

            // Then
            assertThat(result).isFalse();
        }
    }

    // ========== countSelectedModules 测试 ==========

    @Nested
    @DisplayName("countSelectedModules 测试")
    class CountSelectedModulesTests {

        @Test
        @DisplayName("EVAL-COUNT-001: 统计已选模块数量")
        void shouldCountSelectedModules() {
            // Given
            when(moduleConfigMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            // When
            int result = evaluationService.countSelectedModules(1L);

            // Then
            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("EVAL-COUNT-002: 无已选模块时返回0")
        void shouldReturnZeroWhenNoModules() {
            // Given
            when(moduleConfigMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            // When
            int result = evaluationService.countSelectedModules(1L);

            // Then
            assertThat(result).isEqualTo(0);
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 创建测试用指标DTO
     */
    private EvaluationUpdateDTO createTestIndicatorDTO() {
        EvaluationUpdateDTO dto = new EvaluationUpdateDTO();
        dto.setTableCount(150);
        dto.setDataVolume(new BigDecimal("500"));
        dto.setDataVolumeLadderId(2L);
        dto.setUserCount(600);
        dto.setUserCountLadderId(2L);
        dto.setReportCount(50);
        dto.setHasCustomDev(1);
        dto.setCustomDevCount(2);
        dto.setCustomDevWorkload(new BigDecimal("20"));
        dto.setDataCleanDesc("测试清洗需求");
        dto.setDataCleanComplexity(2);
        return dto;
    }
}
