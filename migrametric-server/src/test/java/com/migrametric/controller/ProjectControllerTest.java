package com.migrametric.controller;

import com.migrametric.common.PageResult;
import com.migrametric.common.Result;
import com.migrametric.controller.project.ProjectController;
import com.migrametric.dto.project.ProjectCreateDTO;
import com.migrametric.dto.project.ProjectQueryDTO;
import com.migrametric.dto.project.ProjectUpdateDTO;
import com.migrametric.service.project.ProjectService;
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
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProjectController 集成测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectController 测试")
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    private ProjectVO mockProjectVO;
    private ProjectDetailVO mockProjectDetailVO;
    private PageResult<ProjectVO> mockPageResult;

    @BeforeEach
    void setUp() {
        mockProjectVO = new ProjectVO();
        mockProjectVO.setId(1L);
        mockProjectVO.setProjectName("测试项目");
        mockProjectVO.setCustomerName("测试客户");
        mockProjectVO.setSourceSystemName("SAP");
        mockProjectVO.setTargetSystemName("用友NC");
        mockProjectVO.setProjectLeader("张三");
        mockProjectVO.setStatus("DRAFT");
        mockProjectVO.setStatusText("草稿");

        mockProjectDetailVO = new ProjectDetailVO();
        mockProjectDetailVO.setId(1L);
        mockProjectDetailVO.setProjectName("测试项目");
        mockProjectDetailVO.setCustomerName("测试客户");
        mockProjectDetailVO.setSourceSystemName("SAP");
        mockProjectDetailVO.setTargetSystemName("用友NC");
        mockProjectDetailVO.setProjectLeader("张三");
        mockProjectDetailVO.setStatus("DRAFT");
        mockProjectDetailVO.setStatusText("草稿");
        mockProjectDetailVO.setTotalWorkload(new BigDecimal("231.38"));

        mockPageResult = new PageResult<>();
        mockPageResult.setRecords(Arrays.asList(mockProjectVO));
        mockPageResult.setTotal(1L);
        mockPageResult.setPageNum(1);
        mockPageResult.setPageSize(10);
    }

    @Nested
    @DisplayName("创建项目测试")
    class CreateProjectTests {

        @Test
        @DisplayName("应该成功创建项目")
        void shouldCreateProjectSuccessfully() {
            // Given
            ProjectCreateDTO createDTO = new ProjectCreateDTO();
            createDTO.setProjectName("新项目");
            createDTO.setCustomerName("新客户");

            when(projectService.create(any(ProjectCreateDTO.class))).thenReturn(1L);

            // When
            Result<Long> result = projectController.create(createDTO);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(1L, result.getData());
            verify(projectService, times(1)).create(any(ProjectCreateDTO.class));
        }
    }

    @Nested
    @DisplayName("查询项目测试")
    class QueryProjectTests {

        @Test
        @DisplayName("应该分页查询项目")
        void shouldQueryProjectsWithPagination() {
            // Given
            ProjectQueryDTO queryDTO = new ProjectQueryDTO();
            when(projectService.queryPage(any(ProjectQueryDTO.class))).thenReturn(mockPageResult);

            // When
            Result<PageResult<ProjectVO>> result = projectController.queryPage(queryDTO);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(1, result.getData().getRecords().size());
            verify(projectService, times(1)).queryPage(any(ProjectQueryDTO.class));
        }

        @Test
        @DisplayName("应该获取项目详情")
        void shouldGetProjectDetail() {
            // Given
            when(projectService.getById(1L)).thenReturn(mockProjectVO);

            // When
            Result<ProjectVO> result = projectController.getById(1L);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals("测试项目", result.getData().getProjectName());
            verify(projectService, times(1)).getById(1L);
        }

        @Test
        @DisplayName("应该获取包含评估概况的项目详情")
        void shouldGetProjectDetailWithEvaluation() {
            // Given
            when(projectService.getProjectDetail(1L)).thenReturn(mockProjectDetailVO);

            // When
            Result<ProjectDetailVO> result = projectController.getProjectDetail(1L);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertNotNull(result.getData().getTotalWorkload());
            verify(projectService, times(1)).getProjectDetail(1L);
        }

        @Test
        @DisplayName("获取不存在的项目应该返回null")
        void shouldReturnNullForNonExistentProject() {
            // Given
            when(projectService.getById(999L)).thenReturn(null);

            // When
            Result<ProjectVO> result = projectController.getById(999L);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
        }
    }

    @Nested
    @DisplayName("更新项目测试")
    class UpdateProjectTests {

        @Test
        @DisplayName("应该成功更新项目")
        void shouldUpdateProjectSuccessfully() {
            // Given
            ProjectUpdateDTO updateDTO = new ProjectUpdateDTO();
            updateDTO.setProjectName("更新后的项目名");
            updateDTO.setProjectLeader("李四");

            doNothing().when(projectService).update(eq(1L), any(ProjectUpdateDTO.class));

            // When
            Result<Void> result = projectController.update(1L, updateDTO);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(projectService, times(1)).update(eq(1L), any(ProjectUpdateDTO.class));
        }
    }

    @Nested
    @DisplayName("项目操作测试")
    class ProjectOperationTests {

        @Test
        @DisplayName("应该成功复制项目")
        void shouldCopyProjectSuccessfully() {
            // Given
            when(projectService.copy(1L)).thenReturn(2L);

            // When
            Result<Long> result = projectController.copy(1L);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(2L, result.getData());
            verify(projectService, times(1)).copy(1L);
        }

        @Test
        @DisplayName("应该成功删除项目")
        void shouldDeleteProjectSuccessfully() {
            // Given
            doNothing().when(projectService).delete(1L);

            // When
            Result<Void> result = projectController.delete(1L);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(projectService, times(1)).delete(1L);
        }

        @Test
        @DisplayName("应该成功归档项目")
        void shouldArchiveProjectSuccessfully() {
            // Given
            doNothing().when(projectService).archive(1L);

            // When
            Result<Void> result = projectController.archive(1L);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(projectService, times(1)).archive(1L);
        }
    }

    @Nested
    @DisplayName("参数验证测试")
    class ValidationTests {

        @Test
        @DisplayName("应该处理查询参数为空的情况")
        void shouldHandleEmptyQueryParams() {
            // Given
            ProjectQueryDTO emptyQuery = new ProjectQueryDTO();
            when(projectService.queryPage(any(ProjectQueryDTO.class))).thenReturn(mockPageResult);

            // When
            Result<PageResult<ProjectVO>> result = projectController.queryPage(emptyQuery);

            // Then
            assertNotNull(result);
            assertEquals(200, result.getCode());
        }
    }
}
