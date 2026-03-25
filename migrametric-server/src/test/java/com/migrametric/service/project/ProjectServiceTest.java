package com.migrametric.service.project;

import com.migrametric.common.BusinessException;
import com.migrametric.common.PageResult;
import com.migrametric.common.ResultCode;
import com.migrametric.dto.project.ProjectCreateDTO;
import com.migrametric.dto.project.ProjectQueryDTO;
import com.migrametric.dto.project.ProjectUpdateDTO;
import com.migrametric.entity.evaluation.Evaluation;
import com.migrametric.entity.project.Project;
import com.migrametric.entity.system.SystemType;
import com.migrametric.mapper.project.ProjectMapper;
import com.migrametric.mapper.system.SystemTypeMapper;
import com.migrametric.service.evaluation.EvaluationService;
import com.migrametric.service.project.impl.ProjectServiceImpl;
import com.migrametric.vo.evaluation.EvaluationVO;
import com.migrametric.vo.project.ProjectDetailVO;
import com.migrametric.vo.project.ProjectVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 项目服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("项目服务测试")
class ProjectServiceTest {

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private SystemTypeMapper systemTypeMapper;

    @Mock
    private EvaluationService evaluationService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project sampleProject;

    @BeforeEach
    void setUp() {
        sampleProject = new Project();
        sampleProject.setId(1L);
        sampleProject.setProjectName("ERP迁移项目");
        sampleProject.setCustomerName("XX公司");
        sampleProject.setSourceSystemId(1L);
        sampleProject.setTargetSystemId(2L);
        sampleProject.setProjectLeader("张三");
        sampleProject.setContact("13800138000");
        sampleProject.setDescription("SAP到用友的ERP迁移");
        sampleProject.setEvaluationDate(LocalDate.of(2026, 3, 20));
        sampleProject.setStatus("DRAFT");
        sampleProject.setUserId(1L);
        sampleProject.setCreateTime(LocalDateTime.now());
        sampleProject.setCreateBy("admin");
    }

    private Project createProject(Long id, String name, String status) {
        Project project = new Project();
        project.setId(id);
        project.setProjectName(name);
        project.setCustomerName("客户");
        project.setSourceSystemId(1L);
        project.setTargetSystemId(2L);
        project.setStatus(status);
        project.setEvaluationDate(LocalDate.now());
        project.setUserId(1L);
        project.setCreateTime(LocalDateTime.now());
        project.setCreateBy("admin");
        return project;
    }

    private SystemType createSystemType(Long id, String name) {
        SystemType systemType = new SystemType();
        systemType.setId(id);
        systemType.setSystemName(name);
        systemType.setSystemCategory(1);
        systemType.setStatus(1);
        return systemType;
    }

    private EvaluationVO createEvaluationVO(Long projectId) {
        EvaluationVO vo = new EvaluationVO();
        vo.setId(1L);
        vo.setProjectId(projectId);
        vo.setTableCount(200);
        vo.setDataVolume(new BigDecimal("500.00"));
        vo.setUserCount(600);
        vo.setReportCount(50);
        vo.setHasCustomDev(true);
        vo.setCustomDevCount(3);
        vo.setCustomDevWorkload(new BigDecimal("20.00"));
        vo.setCoreWorkload(new BigDecimal("120.50"));
        vo.setReportWorkload(new BigDecimal("15.88"));
        vo.setTotalWorkload(new BigDecimal("156.38"));
        vo.setEvaluationStatus("COMPLETED");
        vo.setEvaluationStatusText("已完成");
        vo.setEvaluationTime(LocalDateTime.now());
        return vo;
    }

    @Nested
    @DisplayName("queryPage 测试")
    class QueryPageTests {

        @Test
        @DisplayName("应返回分页结果")
        void shouldReturnPageResult() {
            ProjectQueryDTO queryDTO = new ProjectQueryDTO();
            queryDTO.setPageNum(1);
            queryDTO.setPageSize(10);

            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Project> mockPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10, 1);
            mockPage.setRecords(List.of(sampleProject));

            when(projectMapper.selectPage(any(), any())).thenReturn(mockPage);
            when(systemTypeMapper.selectById(1L)).thenReturn(createSystemType(1L, "SAP"));
            when(systemTypeMapper.selectById(2L)).thenReturn(createSystemType(2L, "用友"));

            PageResult<ProjectVO> result = projectService.queryPage(queryDTO);

            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getRecords().get(0).getProjectName()).isEqualTo("ERP迁移项目");
        }

        @Test
        @DisplayName("应包含系统名称")
        void shouldIncludeSystemNames() {
            ProjectQueryDTO queryDTO = new ProjectQueryDTO();

            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Project> mockPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10, 1);
            mockPage.setRecords(List.of(sampleProject));

            when(projectMapper.selectPage(any(), any())).thenReturn(mockPage);
            when(systemTypeMapper.selectById(1L)).thenReturn(createSystemType(1L, "SAP S/4HANA"));
            when(systemTypeMapper.selectById(2L)).thenReturn(createSystemType(2L, "用友NC"));

            PageResult<ProjectVO> result = projectService.queryPage(queryDTO);

            assertThat(result.getRecords().get(0).getSourceSystemName()).isEqualTo("SAP S/4HANA");
            assertThat(result.getRecords().get(0).getTargetSystemName()).isEqualTo("用友NC");
        }

        @Test
        @DisplayName("应正确设置状态文本")
        void shouldSetStatusText() {
            ProjectQueryDTO queryDTO = new ProjectQueryDTO();

            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Project> mockPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10, 1);
            mockPage.setRecords(List.of(sampleProject));

            when(projectMapper.selectPage(any(), any())).thenReturn(mockPage);

            PageResult<ProjectVO> result = projectService.queryPage(queryDTO);

            assertThat(result.getRecords().get(0).getStatusText()).isEqualTo("草稿");
        }

        @Test
        @DisplayName("空结果应返回空列表")
        void shouldReturnEmptyList() {
            ProjectQueryDTO queryDTO = new ProjectQueryDTO();

            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Project> mockPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10, 0);
            mockPage.setRecords(Collections.emptyList());

            when(projectMapper.selectPage(any(), any())).thenReturn(mockPage);

            PageResult<ProjectVO> result = projectService.queryPage(queryDTO);

            assertThat(result.getRecords()).isEmpty();
            assertThat(result.getTotal()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getById 测试")
    class GetByIdTests {

        @Test
        @DisplayName("应返回项目详情")
        void shouldReturnProjectDetail() {
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);
            when(systemTypeMapper.selectById(1L)).thenReturn(createSystemType(1L, "SAP"));
            when(systemTypeMapper.selectById(2L)).thenReturn(createSystemType(2L, "用友"));

            ProjectVO result = projectService.getById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getProjectName()).isEqualTo("ERP迁移项目");
            assertThat(result.getSourceSystemName()).isEqualTo("SAP");
            assertThat(result.getTargetSystemName()).isEqualTo("用友");
        }

        @Test
        @DisplayName("项目不存在应抛出异常")
        void shouldThrowExceptionWhenNotFound() {
            when(projectMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> projectService.getById(999L))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("getProjectDetail 测试")
    class GetProjectDetailTests {

        @Test
        @DisplayName("应返回包含评估概况的项目详情")
        void shouldReturnProjectDetailWithEvaluation() {
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);
            when(systemTypeMapper.selectById(1L)).thenReturn(createSystemType(1L, "SAP"));
            when(systemTypeMapper.selectById(2L)).thenReturn(createSystemType(2L, "用友"));
            when(evaluationService.getByProjectId(1L)).thenReturn(createEvaluationVO(1L));
            when(evaluationService.countSelectedModules(1L)).thenReturn(5);

            ProjectDetailVO result = projectService.getProjectDetail(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getProjectName()).isEqualTo("ERP迁移项目");
            assertThat(result.getHasEvaluation()).isTrue();
            assertThat(result.getTotalWorkload()).isEqualByComparingTo(new BigDecimal("156.38"));
            assertThat(result.getSelectedModuleCount()).isEqualTo(5);
            assertThat(result.getEvaluationStatus()).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("无评估记录时应返回默认值")
        void shouldReturnDefaultsWhenNoEvaluation() {
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);
            when(systemTypeMapper.selectById(1L)).thenReturn(createSystemType(1L, "SAP"));
            when(systemTypeMapper.selectById(2L)).thenReturn(createSystemType(2L, "用友"));
            when(evaluationService.getByProjectId(1L)).thenReturn(null);

            ProjectDetailVO result = projectService.getProjectDetail(1L);

            assertThat(result.getHasEvaluation()).isFalse();
            assertThat(result.getTotalWorkload()).isNull();
            assertThat(result.getSelectedModuleCount()).isEqualTo(0);
            assertThat(result.getEvaluationStatus()).isNull();
        }

        @Test
        @DisplayName("项目不存在应抛出异常")
        void shouldThrowExceptionWhenProjectNotFound() {
            when(projectMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> projectService.getProjectDetail(999L))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("create 测试")
    class CreateTests {

        @Test
        @DisplayName("应成功创建项目")
        void shouldCreateProjectSuccessfully() {
            ProjectCreateDTO createDTO = new ProjectCreateDTO();
            createDTO.setProjectName("新项目");
            createDTO.setCustomerName("新客户");
            createDTO.setSourceSystemId(1L);
            createDTO.setTargetSystemId(2L);
            createDTO.setEvaluationDate(LocalDate.now());

            SystemType sourceSystem = createSystemType(1L, "SAP");
            sourceSystem.setStatus(1);
            SystemType targetSystem = createSystemType(2L, "用友");
            targetSystem.setStatus(1);

            when(systemTypeMapper.selectById(1L)).thenReturn(sourceSystem);
            when(systemTypeMapper.selectById(2L)).thenReturn(targetSystem);
            when(projectMapper.insert(any())).thenAnswer(invocation -> {
                Project p = invocation.getArgument(0);
                p.setId(100L);
                return 1;
            });

            Long result = projectService.create(createDTO);

            assertThat(result).isEqualTo(100L);
        }

        @Test
        @DisplayName("源系统和目标系统相同时应抛出异常")
        void shouldFailWhenSourceEqualsTarget() {
            ProjectCreateDTO createDTO = new ProjectCreateDTO();
            createDTO.setProjectName("测试项目");
            createDTO.setCustomerName("客户");
            createDTO.setSourceSystemId(1L);
            createDTO.setTargetSystemId(1L);
            createDTO.setEvaluationDate(LocalDate.now());

            assertThatThrownBy(() -> projectService.create(createDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("源系统和目标系统不能相同");
        }

        @Test
        @DisplayName("源系统不存在时应抛出异常")
        void shouldFailWhenSourceSystemNotFound() {
            ProjectCreateDTO createDTO = new ProjectCreateDTO();
            createDTO.setProjectName("测试项目");
            createDTO.setCustomerName("客户");
            createDTO.setSourceSystemId(999L);
            createDTO.setTargetSystemId(2L);
            createDTO.setEvaluationDate(LocalDate.now());

            when(systemTypeMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> projectService.create(createDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("源系统不存在");
        }

        @Test
        @DisplayName("目标系统不存在时应抛出异常")
        void shouldFailWhenTargetSystemNotFound() {
            ProjectCreateDTO createDTO = new ProjectCreateDTO();
            createDTO.setProjectName("测试项目");
            createDTO.setCustomerName("客户");
            createDTO.setSourceSystemId(1L);
            createDTO.setTargetSystemId(999L);
            createDTO.setEvaluationDate(LocalDate.now());

            SystemType sourceSystem = createSystemType(1L, "SAP");
            sourceSystem.setStatus(1);
            when(systemTypeMapper.selectById(1L)).thenReturn(sourceSystem);
            when(systemTypeMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> projectService.create(createDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("目标系统不存在");
        }

        @Test
        @DisplayName("源系统已禁用时应抛出异常")
        void shouldFailWhenSourceSystemDisabled() {
            ProjectCreateDTO createDTO = new ProjectCreateDTO();
            createDTO.setProjectName("测试项目");
            createDTO.setCustomerName("客户");
            createDTO.setSourceSystemId(1L);
            createDTO.setTargetSystemId(2L);
            createDTO.setEvaluationDate(LocalDate.now());

            SystemType sourceSystem = createSystemType(1L, "SAP");
            sourceSystem.setStatus(0); // 禁用状态
            when(systemTypeMapper.selectById(1L)).thenReturn(sourceSystem);

            assertThatThrownBy(() -> projectService.create(createDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("源系统已被禁用");
        }

        @Test
        @DisplayName("目标系统已禁用时应抛出异常")
        void shouldFailWhenTargetSystemDisabled() {
            ProjectCreateDTO createDTO = new ProjectCreateDTO();
            createDTO.setProjectName("测试项目");
            createDTO.setCustomerName("客户");
            createDTO.setSourceSystemId(1L);
            createDTO.setTargetSystemId(2L);
            createDTO.setEvaluationDate(LocalDate.now());

            SystemType sourceSystem = createSystemType(1L, "SAP");
            sourceSystem.setStatus(1);
            SystemType targetSystem = createSystemType(2L, "用友");
            targetSystem.setStatus(0); // 禁用状态

            when(systemTypeMapper.selectById(1L)).thenReturn(sourceSystem);
            when(systemTypeMapper.selectById(2L)).thenReturn(targetSystem);

            assertThatThrownBy(() -> projectService.create(createDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("目标系统已被禁用");
        }
    }

    @Nested
    @DisplayName("update 测试")
    class UpdateTests {

        @Test
        @DisplayName("应成功更新项目")
        void shouldUpdateProjectSuccessfully() {
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);
            // updateDTO只设置了projectName和projectLeader，不设置source/targetSystemId，
            // 所以不需要stub systemTypeMapper

            ProjectUpdateDTO updateDTO = new ProjectUpdateDTO();
            updateDTO.setProjectName("更新后的项目名称");
            updateDTO.setProjectLeader("李四");

            projectService.update(1L, updateDTO);
        }

        @Test
        @DisplayName("项目不存在应抛出异常")
        void shouldThrowExceptionWhenProjectNotFound() {
            when(projectMapper.selectById(999L)).thenReturn(null);

            ProjectUpdateDTO updateDTO = new ProjectUpdateDTO();
            updateDTO.setProjectName("测试");

            assertThatThrownBy(() -> projectService.update(999L, updateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("项目不存在");
        }

        @Test
        @DisplayName("已归档项目不可编辑")
        void shouldFailWhenProjectArchived() {
            sampleProject.setStatus("ARCHIVED");
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);

            ProjectUpdateDTO updateDTO = new ProjectUpdateDTO();
            updateDTO.setProjectName("测试");

            assertThatThrownBy(() -> projectService.update(1L, updateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("已归档的项目不可编辑");
        }

        @Test
        @DisplayName("更新时源系统和目标系统相同应抛出异常")
        void shouldFailWhenSourceEqualsTargetOnUpdate() {
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);

            ProjectUpdateDTO updateDTO = new ProjectUpdateDTO();
            updateDTO.setSourceSystemId(1L);
            updateDTO.setTargetSystemId(1L);

            assertThatThrownBy(() -> projectService.update(1L, updateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("源系统和目标系统不能相同");
        }

        @Test
        @DisplayName("更新源系统时应校验目标系统")
        void shouldValidateTargetSystemOnUpdate() {
            sampleProject.setSourceSystemId(1L);
            sampleProject.setTargetSystemId(2L);
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);
            // 只stub被调用的：updateDTO.getSourceSystemId()返回null（未设置），不调用
            // updateDTO.getTargetSystemId()返回null（未设置），不调用

            ProjectUpdateDTO updateDTO = new ProjectUpdateDTO();
            updateDTO.setProjectName("测试");

            projectService.update(1L, updateDTO);
        }
    }

    @Nested
    @DisplayName("copy 测试")
    class CopyTests {

        @Test
        @DisplayName("应成功复制项目")
        void shouldCopyProjectSuccessfully() {
            sampleProject.setProjectName("ERP迁移项目");
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);
            when(projectMapper.insert(any())).thenAnswer(invocation -> {
                Project p = invocation.getArgument(0);
                p.setId(200L);
                return 1;
            });

            Long newId = projectService.copy(1L);

            assertThat(newId).isEqualTo(200L);
        }

        @Test
        @DisplayName("项目不存在应抛出异常")
        void shouldThrowExceptionWhenProjectNotFound() {
            when(projectMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> projectService.copy(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("项目不存在");
        }

        @Test
        @DisplayName("复制后项目名称应添加_副本后缀")
        void shouldAddCopySuffixToProjectName() {
            sampleProject.setProjectName("原始项目");
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);
            when(projectMapper.insert(any())).thenAnswer(invocation -> {
                Project p = invocation.getArgument(0);
                assertThat(p.getProjectName()).isEqualTo("原始项目_副本");
                assertThat(p.getStatus()).isEqualTo("DRAFT");
                p.setId(200L);
                return 1;
            });

            projectService.copy(1L);
        }

        @Test
        @DisplayName("复制后项目状态应为草稿")
        void shouldSetDraftStatusAfterCopy() {
            sampleProject.setStatus("COMPLETED");
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);
            when(projectMapper.insert(any())).thenAnswer(invocation -> {
                Project p = invocation.getArgument(0);
                assertThat(p.getStatus()).isEqualTo("DRAFT");
                p.setId(200L);
                return 1;
            });

            projectService.copy(1L);
        }
    }

    @Nested
    @DisplayName("delete 测试")
    class DeleteTests {

        @Test
        @DisplayName("应成功删除草稿项目")
        void shouldDeleteDraftProjectSuccessfully() {
            sampleProject.setStatus("DRAFT");
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);

            projectService.delete(1L);
        }

        @Test
        @DisplayName("项目不存在应抛出异常")
        void shouldThrowExceptionWhenProjectNotFound() {
            when(projectMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> projectService.delete(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("项目不存在");
        }

        @Test
        @DisplayName("非草稿状态项目不可删除")
        void shouldFailWhenProjectNotDraft() {
            sampleProject.setStatus("IN_PROGRESS");
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);

            assertThatThrownBy(() -> projectService.delete(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只有草稿状态的项目可以删除");
        }

        @Test
        @DisplayName("已完成状态项目不可删除")
        void shouldFailWhenProjectCompleted() {
            sampleProject.setStatus("COMPLETED");
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);

            assertThatThrownBy(() -> projectService.delete(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只有草稿状态的项目可以删除");
        }

        @Test
        @DisplayName("已归档状态项目不可删除")
        void shouldFailWhenProjectArchived() {
            sampleProject.setStatus("ARCHIVED");
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);

            assertThatThrownBy(() -> projectService.delete(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只有草稿状态的项目可以删除");
        }
    }

    @Nested
    @DisplayName("archive 测试")
    class ArchiveTests {

        @Test
        @DisplayName("应成功归档已完成项目")
        void shouldArchiveCompletedProjectSuccessfully() {
            sampleProject.setStatus("COMPLETED");
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);

            projectService.archive(1L);
        }

        @Test
        @DisplayName("项目不存在应抛出异常")
        void shouldThrowExceptionWhenProjectNotFound() {
            when(projectMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> projectService.archive(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("项目不存在");
        }

        @Test
        @DisplayName("非完成状态项目不可归档")
        void shouldFailWhenProjectNotCompleted() {
            sampleProject.setStatus("IN_PROGRESS");
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);

            assertThatThrownBy(() -> projectService.archive(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只有已完成的项目可以归档");
        }

        @Test
        @DisplayName("草稿状态项目不可归档")
        void shouldFailWhenProjectIsDraft() {
            sampleProject.setStatus("DRAFT");
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);

            assertThatThrownBy(() -> projectService.archive(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只有已完成的项目可以归档");
        }

        @Test
        @DisplayName("已归档项目不可再次归档")
        void shouldFailWhenProjectAlreadyArchived() {
            sampleProject.setStatus("ARCHIVED");
            when(projectMapper.selectById(1L)).thenReturn(sampleProject);

            assertThatThrownBy(() -> projectService.archive(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只有已完成的项目可以归档");
        }
    }
}
