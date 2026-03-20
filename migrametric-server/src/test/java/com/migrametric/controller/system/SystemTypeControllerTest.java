package com.migrametric.controller.system;

import com.migrametric.common.PageResult;
import com.migrametric.common.Result;
import com.migrametric.dto.system.SystemTypeCreateDTO;
import com.migrametric.dto.system.SystemTypeQueryDTO;
import com.migrametric.dto.system.SystemTypeUpdateDTO;
import com.migrametric.service.system.SystemTypeService;
import com.migrametric.vo.system.SystemTypeVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SystemType Controller 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SystemType Controller 单元测试")
class SystemTypeControllerTest {

    @Mock
    private SystemTypeService systemTypeService;

    @InjectMocks
    private SystemTypeController controller;

    private SystemTypeVO sampleVO;

    @BeforeEach
    void setUp() {
        sampleVO = new SystemTypeVO();
        sampleVO.setId(1L);
        sampleVO.setSystemName("SAP S/4HANA");
        sampleVO.setSystemCategory(1);
        sampleVO.setSystemCategoryText("源系统");
        sampleVO.setDescription("SAP ERP系统");
        sampleVO.setStatus(1);
        sampleVO.setStatusText("启用");
    }

    @Nested
    @DisplayName("GET /api/system/types - 分页查询")
    class QueryPageTests {

        @Test
        @DisplayName("应返回分页结果")
        void shouldReturnPageResult() {
            SystemTypeQueryDTO queryDTO = new SystemTypeQueryDTO();
            queryDTO.setPageNum(1);
            queryDTO.setPageSize(10);

            PageResult<SystemTypeVO> expectedResult = new PageResult<>();
            expectedResult.setRecords(Collections.singletonList(sampleVO));
            expectedResult.setTotal(1L);
            expectedResult.setPageNum(1);
            expectedResult.setPageSize(10);

            when(systemTypeService.queryPage(any())).thenReturn(expectedResult);

            Result<PageResult<SystemTypeVO>> result = controller.queryPage(queryDTO);

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData().getRecords()).hasSize(1);
            assertThat(result.getData().getTotal()).isEqualTo(1L);
        }

        @Test
        @DisplayName("空结果应返回空列表")
        void shouldReturnEmptyListWhenNoData() {
            SystemTypeQueryDTO queryDTO = new SystemTypeQueryDTO();
            queryDTO.setPageNum(1);
            queryDTO.setPageSize(10);

            PageResult<SystemTypeVO> emptyResult = new PageResult<>();
            emptyResult.setRecords(Collections.emptyList());
            emptyResult.setTotal(0L);

            when(systemTypeService.queryPage(any())).thenReturn(emptyResult);

            Result<PageResult<SystemTypeVO>> result = controller.queryPage(queryDTO);

            assertThat(result.getData().getRecords()).isEmpty();
        }

        @Test
        @DisplayName("分页参数应正确传递")
        void shouldPassPaginationParams() {
            SystemTypeQueryDTO queryDTO = new SystemTypeQueryDTO();
            queryDTO.setPageNum(2);
            queryDTO.setPageSize(20);

            PageResult<SystemTypeVO> result = new PageResult<>();
            result.setRecords(Collections.emptyList());
            when(systemTypeService.queryPage(queryDTO)).thenReturn(result);

            controller.queryPage(queryDTO);

            verify(systemTypeService).queryPage(queryDTO);
        }
    }

    @Nested
    @DisplayName("GET /api/system/types/enabled - 获取启用的系统类型")
    class ListEnabledTests {

        @Test
        @DisplayName("应返回所有启用的系统类型")
        void shouldReturnAllEnabledSystemTypes() {
            List<SystemTypeVO> enabledList = Arrays.asList(sampleVO);
            when(systemTypeService.listEnabled(null)).thenReturn(enabledList);

            Result<List<SystemTypeVO>> result = controller.listEnabled(null);

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData()).hasSize(1);
        }

        @Test
        @DisplayName("按类别筛选应正确传递")
        void shouldFilterByCategory() {
            when(systemTypeService.listEnabled(1)).thenReturn(Collections.singletonList(sampleVO));

            controller.listEnabled(1);

            verify(systemTypeService).listEnabled(1);
        }
    }

    @Nested
    @DisplayName("GET /api/system/types/{id} - 获取详情")
    class GetByIdTests {

        @Test
        @DisplayName("应返回指定ID的系统类型")
        void shouldReturnSystemTypeById() {
            when(systemTypeService.getById(1L)).thenReturn(sampleVO);

            Result<SystemTypeVO> result = controller.getById(1L);

            assertThat(result.getData().getId()).isEqualTo(1L);
            assertThat(result.getData().getSystemName()).isEqualTo("SAP S/4HANA");
        }
    }

    @Nested
    @DisplayName("POST /api/system/types - 创建系统类型")
    class CreateTests {

        @Test
        @DisplayName("应成功创建并返回ID")
        void shouldCreateAndReturnId() {
            SystemTypeCreateDTO createDTO = new SystemTypeCreateDTO();
            createDTO.setSystemName("Oracle");
            createDTO.setSystemCategory(1);
            createDTO.setDescription("Oracle ERP");

            when(systemTypeService.create(any())).thenReturn(2L);

            Result<Long> result = controller.create(createDTO);

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData()).isEqualTo(2L);
            verify(systemTypeService).create(any());
        }
    }

    @Nested
    @DisplayName("PUT /api/system/types/{id} - 更新系统类型")
    class UpdateTests {

        @Test
        @DisplayName("应成功更新系统类型")
        void shouldUpdateSystemType() {
            SystemTypeUpdateDTO updateDTO = new SystemTypeUpdateDTO();
            updateDTO.setSystemName("Updated SAP");
            updateDTO.setSystemCategory(1);
            updateDTO.setDescription("更新描述");

            doNothing().when(systemTypeService).update(any());

            Result<Void> result = controller.update(1L, updateDTO);

            assertThat(result.getCode()).isEqualTo(200);
            verify(systemTypeService).update(any());
        }

        @Test
        @DisplayName("路径ID应正确设置到DTO")
        void shouldSetIdFromPathToDTO() {
            SystemTypeUpdateDTO updateDTO = new SystemTypeUpdateDTO();
            updateDTO.setSystemName("Test");
            updateDTO.setSystemCategory(1);

            controller.update(5L, updateDTO);

            assertThat(updateDTO.getId()).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("DELETE /api/system/types/{id} - 删除系统类型")
    class DeleteTests {

        @Test
        @DisplayName("应成功删除系统类型")
        void shouldDeleteSystemType() {
            doNothing().when(systemTypeService).delete(1L);

            Result<Void> result = controller.delete(1L);

            assertThat(result.getCode()).isEqualTo(200);
            verify(systemTypeService).delete(1L);
        }
    }

    @Nested
    @DisplayName("PATCH /api/system/types/{id}/enable - 启用系统类型")
    class EnableTests {

        @Test
        @DisplayName("应成功启用系统类型")
        void shouldEnableSystemType() {
            doNothing().when(systemTypeService).enable(1L);

            Result<Void> result = controller.enable(1L);

            assertThat(result.getCode()).isEqualTo(200);
            verify(systemTypeService).enable(1L);
        }
    }

    @Nested
    @DisplayName("PATCH /api/system/types/{id}/disable - 禁用系统类型")
    class DisableTests {

        @Test
        @DisplayName("应成功禁用系统类型")
        void shouldDisableSystemType() {
            doNothing().when(systemTypeService).disable(1L);

            Result<Void> result = controller.disable(1L);

            assertThat(result.getCode()).isEqualTo(200);
            verify(systemTypeService).disable(1L);
        }
    }
}
