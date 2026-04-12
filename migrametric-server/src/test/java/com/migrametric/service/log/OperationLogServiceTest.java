package com.migrametric.service.log;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.migrametric.dto.log.OperationLogQueryDTO;
import com.migrametric.entity.log.OperationLog;
import com.migrametric.mapper.log.OperationLogMapper;
import com.migrametric.service.log.impl.OperationLogServiceImpl;
import com.migrametric.vo.log.OperationLogVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OperationLogService 单元测试
 *
 * @author MigraMetric Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OperationLogService 单元测试")
class OperationLogServiceTest {

    @Mock
    private OperationLogMapper operationLogMapper;

    @InjectMocks
    private OperationLogServiceImpl operationLogService;

    // ========== 测试数据 ==========

    private OperationLog operationLog1;
    private OperationLog operationLog2;

    @BeforeEach
    void setUp() {
        // 操作日志1
        operationLog1 = new OperationLog();
        operationLog1.setId(1L);
        operationLog1.setModule("项目管理");
        operationLog1.setOperationType("CREATE");
        operationLog1.setDescription("创建项目");
        operationLog1.setHttpMethod("POST");
        operationLog1.setRequestUrl("/api/projects");
        operationLog1.setUserId(1L);
        operationLog1.setUsername("admin");
        operationLog1.setIpAddress("127.0.0.1");
        operationLog1.setStatus(1);
        operationLog1.setExecutionTime(150L);
        operationLog1.setCreateTime(LocalDateTime.now());

        // 操作日志2
        operationLog2 = new OperationLog();
        operationLog2.setId(2L);
        operationLog2.setModule("项目管理");
        operationLog2.setOperationType("UPDATE");
        operationLog2.setDescription("更新项目");
        operationLog2.setHttpMethod("PUT");
        operationLog2.setRequestUrl("/api/projects/1");
        operationLog2.setUserId(1L);
        operationLog2.setUsername("admin");
        operationLog2.setIpAddress("127.0.0.1");
        operationLog2.setStatus(1);
        operationLog2.setExecutionTime(100L);
        operationLog2.setCreateTime(LocalDateTime.now());
    }

    // ========== 日志保存测试 (LOG-SAVE-*) ==========

    @Nested
    @DisplayName("日志保存测试")
    class SaveLogTests {

        @Test
        @DisplayName("LOG-SAVE-001: 成功保存操作日志")
        void shouldSaveOperationLog() {
            // Given
            when(operationLogMapper.insert(any(OperationLog.class))).thenReturn(1);

            // When
            operationLogService.saveLog(operationLog1);

            // Then
            verify(operationLogMapper, times(1)).insert(operationLog1);
        }

        @Test
        @DisplayName("LOG-SAVE-002: 保存多个操作日志")
        void shouldSaveMultipleLogs() {
            // Given
            when(operationLogMapper.insert(any(OperationLog.class))).thenReturn(1);

            // When
            operationLogService.saveLog(operationLog1);
            operationLogService.saveLog(operationLog2);

            // Then
            verify(operationLogMapper, times(2)).insert(any(OperationLog.class));
        }
    }

    // ========== 日志查询测试 (LOG-QUERY-*) ==========

    @Nested
    @DisplayName("日志查询测试")
    class QueryLogTests {

        @Test
        @DisplayName("LOG-QUERY-001: 查询所有操作日志")
        void shouldQueryAllLogs() {
            // Given
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<OperationLog> mockPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(operationLog1, operationLog2));
            mockPage.setTotal(2);

            when(operationLogMapper.selectPage(any(), any())).thenReturn(mockPage);

            // When
            OperationLogQueryDTO queryDTO = new OperationLogQueryDTO();
            queryDTO.setPageNum(1);
            queryDTO.setPageSize(10);
            IPage<OperationLogVO> result = operationLogService.queryPage(queryDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRecords()).hasSize(2);
            assertThat(result.getTotal()).isEqualTo(2);
        }

        @Test
        @DisplayName("LOG-QUERY-002: 按用户名查询")
        void shouldQueryByUsername() {
            // Given
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<OperationLog> mockPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            mockPage.setRecords(List.of(operationLog1));
            mockPage.setTotal(1);

            when(operationLogMapper.selectPage(any(), any())).thenReturn(mockPage);

            // When
            OperationLogQueryDTO queryDTO = new OperationLogQueryDTO();
            queryDTO.setUsername("admin");
            queryDTO.setPageNum(1);
            queryDTO.setPageSize(10);
            IPage<OperationLogVO> result = operationLogService.queryPage(queryDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getRecords().get(0).getUsername()).isEqualTo("admin");
        }

        @Test
        @DisplayName("LOG-QUERY-003: 按模块查询")
        void shouldQueryByModule() {
            // Given
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<OperationLog> mockPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(operationLog1, operationLog2));
            mockPage.setTotal(2);

            when(operationLogMapper.selectPage(any(), any())).thenReturn(mockPage);

            // When
            OperationLogQueryDTO queryDTO = new OperationLogQueryDTO();
            queryDTO.setModule("项目管理");
            queryDTO.setPageNum(1);
            queryDTO.setPageSize(10);
            IPage<OperationLogVO> result = operationLogService.queryPage(queryDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRecords()).hasSize(2);
        }

        @Test
        @DisplayName("LOG-QUERY-004: 按状态查询")
        void shouldQueryByStatus() {
            // Given
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<OperationLog> mockPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            mockPage.setRecords(List.of(operationLog1));
            mockPage.setTotal(1);

            when(operationLogMapper.selectPage(any(), any())).thenReturn(mockPage);

            // When
            OperationLogQueryDTO queryDTO = new OperationLogQueryDTO();
            queryDTO.setStatus(1); // 成功
            queryDTO.setPageNum(1);
            queryDTO.setPageSize(10);
            IPage<OperationLogVO> result = operationLogService.queryPage(queryDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getRecords().get(0).getStatus()).isEqualTo(1);
        }
    }

    // ========== 日志详情测试 (LOG-DETAIL-*) ==========

    @Nested
    @DisplayName("日志详情测试")
    class DetailTests {

        @Test
        @DisplayName("LOG-DETAIL-001: 获取日志详情")
        void shouldGetLogDetail() {
            // Given
            when(operationLogMapper.selectById(1L)).thenReturn(operationLog1);

            // When
            OperationLogVO result = operationLogService.getById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getModule()).isEqualTo("项目管理");
            assertThat(result.getOperationType()).isEqualTo("CREATE");
        }

        @Test
        @DisplayName("LOG-DETAIL-002: 日志不存在返回null")
        void shouldReturnNullWhenLogNotFound() {
            // Given
            when(operationLogMapper.selectById(999L)).thenReturn(null);

            // When
            OperationLogVO result = operationLogService.getById(999L);

            // Then
            assertThat(result).isNull();
        }
    }

    // ========== 日志删除测试 (LOG-DELETE-*) ==========

    @Nested
    @DisplayName("日志删除测试")
    class DeleteLogTests {

        @Test
        @DisplayName("LOG-DELETE-001: 删除指定日志")
        void shouldDeleteLogById() {
            // Given
            when(operationLogMapper.deleteById(1L)).thenReturn(1);

            // When
            operationLogService.deleteById(1L);

            // Then
            verify(operationLogMapper, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("LOG-DELETE-002: 清空所有日志")
        void shouldClearAllLogs() {
            // Given
            when(operationLogMapper.delete(null)).thenReturn(2);

            // When
            operationLogService.clearAll();

            // Then
            verify(operationLogMapper, times(1)).delete(null);
        }
    }

    // ========== 分页参数测试 (LOG-PAGE-*) ==========

    @Nested
    @DisplayName("分页参数测试")
    class PaginationTests {

        @Test
        @DisplayName("LOG-PAGE-001: 默认分页参数")
        void shouldUseDefaultPagination() {
            // Given
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<OperationLog> mockPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            mockPage.setRecords(List.of(operationLog1));
            mockPage.setTotal(1);

            when(operationLogMapper.selectPage(any(), any())).thenReturn(mockPage);

            // When
            OperationLogQueryDTO queryDTO = new OperationLogQueryDTO();
            IPage<OperationLogVO> result = operationLogService.queryPage(queryDTO);

            // Then
            assertThat(result.getCurrent()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("LOG-PAGE-002: 自定义分页参数")
        void shouldUseCustomPagination() {
            // Given
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<OperationLog> mockPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(2, 20);
            mockPage.setRecords(List.of(operationLog2));
            mockPage.setTotal(21);

            when(operationLogMapper.selectPage(any(), any())).thenReturn(mockPage);

            // When
            OperationLogQueryDTO queryDTO = new OperationLogQueryDTO();
            queryDTO.setPageNum(2);
            queryDTO.setPageSize(20);
            IPage<OperationLogVO> result = operationLogService.queryPage(queryDTO);

            // Then
            assertThat(result.getCurrent()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getTotal()).isEqualTo(21);
        }
    }
}
