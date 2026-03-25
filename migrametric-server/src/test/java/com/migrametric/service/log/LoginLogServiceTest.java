package com.migrametric.service.log;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.migrametric.dto.log.LoginLogQueryDTO;
import com.migrametric.entity.log.LoginLog;
import com.migrametric.mapper.log.LoginLogMapper;
import com.migrametric.service.log.impl.LoginLogServiceImpl;
import com.migrametric.vo.log.LoginLogVO;
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
 * LoginLogService 单元测试
 *
 * @author MigraMetric Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginLogService 单元测试")
class LoginLogServiceTest {

    @Mock
    private LoginLogMapper loginLogMapper;

    @InjectMocks
    private LoginLogServiceImpl loginLogService;

    // ========== 测试数据 ==========

    private LoginLog loginLog1;
    private LoginLog loginLog2;

    @BeforeEach
    void setUp() {
        // 登录日志1 - 成功
        loginLog1 = new LoginLog();
        loginLog1.setId(1L);
        loginLog1.setUsername("admin");
        loginLog1.setIpAddress("127.0.0.1");
        loginLog1.setLoginLocation("本地");
        loginLog1.setBrowser("Chrome");
        loginLog1.setOs("Windows 10");
        loginLog1.setStatus("success");
        loginLog1.setMsg("登录成功");
        loginLog1.setLoginTime(LocalDateTime.now());

        // 登录日志2 - 失败
        loginLog2 = new LoginLog();
        loginLog2.setId(2L);
        loginLog2.setUsername("user1");
        loginLog2.setIpAddress("192.168.1.100");
        loginLog2.setLoginLocation("内网");
        loginLog2.setBrowser("Firefox");
        loginLog2.setOs("macOS");
        loginLog2.setStatus("fail");
        loginLog2.setMsg("密码错误");
        loginLog2.setLoginTime(LocalDateTime.now().minusHours(1));
    }

    // ========== 日志保存测试 (LOGIN-SAVE-*) ==========

    @Nested
    @DisplayName("登录日志保存测试")
    class SaveLoginLogTests {

        @Test
        @DisplayName("LOGIN-SAVE-001: 成功保存登录日志")
        void shouldSaveLoginLog() {
            // Given
            when(loginLogMapper.insert(any(LoginLog.class))).thenReturn(1);

            // When
            loginLogService.saveLoginLog(loginLog1);

            // Then
            verify(loginLogMapper, times(1)).insert(loginLog1);
        }

        @Test
        @DisplayName("LOGIN-SAVE-002: 保存失败登录日志")
        void shouldSaveFailedLoginLog() {
            // Given
            when(loginLogMapper.insert(any(LoginLog.class))).thenReturn(1);

            // When
            loginLogService.saveLoginLog(loginLog2);

            // Then
            verify(loginLogMapper, times(1)).insert(loginLog2);
        }
    }

    // ========== 日志查询测试 (LOGIN-QUERY-*) ==========

    @Nested
    @DisplayName("登录日志查询测试")
    class QueryLoginLogTests {

        @Test
        @DisplayName("LOGIN-QUERY-001: 查询所有登录日志")
        void shouldQueryAllLoginLogs() {
            // Given
            Page<LoginLog> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(loginLog1, loginLog2));
            mockPage.setTotal(2);

            when(loginLogMapper.selectPage(any(), any())).thenReturn(mockPage);

            // When
            LoginLogQueryDTO queryDTO = new LoginLogQueryDTO();
            queryDTO.setPageNum(1);
            queryDTO.setPageSize(10);
            IPage<LoginLogVO> result = loginLogService.queryPage(queryDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRecords()).hasSize(2);
            assertThat(result.getTotal()).isEqualTo(2);
        }

        @Test
        @DisplayName("LOGIN-QUERY-002: 按用户名查询")
        void shouldQueryByUsername() {
            // Given
            Page<LoginLog> mockPage = new Page<>(1, 10);
            mockPage.setRecords(List.of(loginLog1));
            mockPage.setTotal(1);

            when(loginLogMapper.selectPage(any(), any())).thenReturn(mockPage);

            // When
            LoginLogQueryDTO queryDTO = new LoginLogQueryDTO();
            queryDTO.setUsername("admin");
            queryDTO.setPageNum(1);
            queryDTO.setPageSize(10);
            IPage<LoginLogVO> result = loginLogService.queryPage(queryDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getRecords().get(0).getUsername()).isEqualTo("admin");
        }

        @Test
        @DisplayName("LOGIN-QUERY-003: 按状态查询成功登录")
        void shouldQueryBySuccessStatus() {
            // Given
            Page<LoginLog> mockPage = new Page<>(1, 10);
            mockPage.setRecords(List.of(loginLog1));
            mockPage.setTotal(1);

            when(loginLogMapper.selectPage(any(), any())).thenReturn(mockPage);

            // When
            LoginLogQueryDTO queryDTO = new LoginLogQueryDTO();
            queryDTO.setStatus("success");
            queryDTO.setPageNum(1);
            queryDTO.setPageSize(10);
            IPage<LoginLogVO> result = loginLogService.queryPage(queryDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getRecords().get(0).getStatus()).isEqualTo("success");
        }

        @Test
        @DisplayName("LOGIN-QUERY-004: 按状态查询失败登录")
        void shouldQueryByFailStatus() {
            // Given
            Page<LoginLog> mockPage = new Page<>(1, 10);
            mockPage.setRecords(List.of(loginLog2));
            mockPage.setTotal(1);

            when(loginLogMapper.selectPage(any(), any())).thenReturn(mockPage);

            // When
            LoginLogQueryDTO queryDTO = new LoginLogQueryDTO();
            queryDTO.setStatus("fail");
            queryDTO.setPageNum(1);
            queryDTO.setPageSize(10);
            IPage<LoginLogVO> result = loginLogService.queryPage(queryDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getRecords().get(0).getStatus()).isEqualTo("fail");
        }

        @Test
        @DisplayName("LOGIN-QUERY-005: 按IP地址查询")
        void shouldQueryByIpAddress() {
            // Given
            Page<LoginLog> mockPage = new Page<>(1, 10);
            mockPage.setRecords(List.of(loginLog1));
            mockPage.setTotal(1);

            when(loginLogMapper.selectPage(any(), any())).thenReturn(mockPage);

            // When
            LoginLogQueryDTO queryDTO = new LoginLogQueryDTO();
            queryDTO.setIpAddress("127.0.0.1");
            queryDTO.setPageNum(1);
            queryDTO.setPageSize(10);
            IPage<LoginLogVO> result = loginLogService.queryPage(queryDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getRecords().get(0).getIpAddress()).isEqualTo("127.0.0.1");
        }
    }

    // ========== 日志详情测试 (LOGIN-DETAIL-*) ==========

    @Nested
    @DisplayName("登录日志详情测试")
    class LoginLogDetailTests {

        @Test
        @DisplayName("LOGIN-DETAIL-001: 获取登录日志详情")
        void shouldGetLoginLogDetail() {
            // Given
            when(loginLogMapper.selectById(1L)).thenReturn(loginLog1);

            // When
            LoginLogVO result = loginLogService.getById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("admin");
            assertThat(result.getStatus()).isEqualTo("success");
        }

        @Test
        @DisplayName("LOGIN-DETAIL-002: 日志不存在返回null")
        void shouldReturnNullWhenLoginLogNotFound() {
            // Given
            when(loginLogMapper.selectById(999L)).thenReturn(null);

            // When
            LoginLogVO result = loginLogService.getById(999L);

            // Then
            assertThat(result).isNull();
        }
    }

    // ========== 最后登录测试 (LOGIN-LAST-*) ==========

    @Nested
    @DisplayName("最后登录测试")
    class LastLoginTests {

        @Test
        @DisplayName("LOGIN-LAST-001: 获取用户最后登录信息")
        void shouldGetLastLogin() {
            // Given
            when(loginLogMapper.selectOne(any())).thenReturn(loginLog1);

            // When
            LoginLogVO result = loginLogService.getLastLogin("admin");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("admin");
            assertThat(result.getStatus()).isEqualTo("success");
        }

        @Test
        @DisplayName("LOGIN-LAST-002: 用户从未登录返回null")
        void shouldReturnNullWhenUserNeverLoggedIn() {
            // Given
            when(loginLogMapper.selectOne(any())).thenReturn(null);

            // When
            LoginLogVO result = loginLogService.getLastLogin("nonexistent");

            // Then
            assertThat(result).isNull();
        }
    }

    // ========== 日志删除测试 (LOGIN-DELETE-*) ==========

    @Nested
    @DisplayName("登录日志删除测试")
    class DeleteLoginLogTests {

        @Test
        @DisplayName("LOGIN-DELETE-001: 删除指定登录日志")
        void shouldDeleteLoginLogById() {
            // Given
            when(loginLogMapper.deleteById(1L)).thenReturn(1);

            // When
            loginLogService.deleteById(1L);

            // Then
            verify(loginLogMapper, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("LOGIN-DELETE-002: 清空所有登录日志")
        void shouldClearAllLoginLogs() {
            // Given
            when(loginLogMapper.delete(null)).thenReturn(2);

            // When
            loginLogService.clearAll();

            // Then
            verify(loginLogMapper, times(1)).delete(null);
        }
    }

    // ========== 分页参数测试 (LOGIN-PAGE-*) ==========

    @Nested
    @DisplayName("登录日志分页测试")
    class LoginLogPaginationTests {

        @Test
        @DisplayName("LOGIN-PAGE-001: 默认分页参数")
        void shouldUseDefaultPagination() {
            // Given
            Page<LoginLog> mockPage = new Page<>(1, 10);
            mockPage.setRecords(List.of(loginLog1));
            mockPage.setTotal(1);

            when(loginLogMapper.selectPage(any(), any())).thenReturn(mockPage);

            // When
            LoginLogQueryDTO queryDTO = new LoginLogQueryDTO();
            IPage<LoginLogVO> result = loginLogService.queryPage(queryDTO);

            // Then
            assertThat(result.getCurrent()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("LOGIN-PAGE-002: 自定义分页参数")
        void shouldUseCustomPagination() {
            // Given
            Page<LoginLog> mockPage = new Page<>(2, 20);
            mockPage.setRecords(List.of(loginLog2));
            mockPage.setTotal(21);

            when(loginLogMapper.selectPage(any(), any())).thenReturn(mockPage);

            // When
            LoginLogQueryDTO queryDTO = new LoginLogQueryDTO();
            queryDTO.setPageNum(2);
            queryDTO.setPageSize(20);
            IPage<LoginLogVO> result = loginLogService.queryPage(queryDTO);

            // Then
            assertThat(result.getCurrent()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getTotal()).isEqualTo(21);
        }
    }
}
