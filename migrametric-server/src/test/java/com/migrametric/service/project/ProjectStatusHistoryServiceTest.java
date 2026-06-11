package com.migrametric.service.project;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.migrametric.common.BusinessException;
import com.migrametric.common.PageResult;
import com.migrametric.context.UserContext;
import com.migrametric.dto.project.ProjectStatusHistoryCreateDTO;
import com.migrametric.dto.project.ProjectStatusHistoryQueryDTO;
import com.migrametric.entity.project.Project;
import com.migrametric.entity.project.ProjectStatusHistory;
import com.migrametric.mapper.project.ProjectMapper;
import com.migrametric.mapper.project.ProjectStatusHistoryMapper;
import com.migrametric.vo.project.ProjectStatusHistoryVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 项目状态历史 Service 单测（UTSH-001~010）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("项目状态历史Service测试")
class ProjectStatusHistoryServiceTest {

    @Mock
    private ProjectStatusHistoryMapper historyMapper;
    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectStatusHistoryService service;

    private Project project;
    private MockedStatic<UserContext> userContext;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setUserId(1L);
        userContext = mockStatic(UserContext.class);
    }

    @AfterEach
    void tearDown() {
        userContext.close();
    }

    @Test
    @DisplayName("UTSH-001: list 普通用户查自己项目 → 200 + 数据")
    void testListAsOwner() {
        userContext.when(UserContext::isAdmin).thenReturn(false);
        userContext.when(UserContext::getCurrentUserId).thenReturn(1L);
        when(projectMapper.selectById(1L)).thenReturn(project);
        IPage<ProjectStatusHistory> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(history(1L, 1L, "DRAFT", "CREATE")));
        mockPage.setTotal(1);
        when(historyMapper.selectPage(any(), any())).thenReturn(mockPage);

        PageResult<ProjectStatusHistoryVO> result = service.list(1L, new ProjectStatusHistoryQueryDTO());

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getEvent()).isEqualTo("CREATE");
    }

    @Test
    @DisplayName("UTSH-002: list 普通用户查别人项目 → 抛 AccessDenied")
    void testListAsOtherUser() {
        userContext.when(UserContext::isAdmin).thenReturn(false);
        userContext.when(UserContext::getCurrentUserId).thenReturn(99L);
        when(projectMapper.selectById(1L)).thenReturn(project);

        assertThatThrownBy(() -> service.list(1L, new ProjectStatusHistoryQueryDTO()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权访问");
    }

    @Test
    @DisplayName("UTSH-003: list ADMIN 查所有项目 → 200 + 数据")
    void testListAsAdmin() {
        userContext.when(UserContext::isAdmin).thenReturn(true);
        userContext.when(UserContext::getCurrentUserId).thenReturn(1L);
        when(projectMapper.selectById(1L)).thenReturn(project);
        IPage<ProjectStatusHistory> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(history(1L, 1L, "DRAFT", "CREATE")));
        mockPage.setTotal(1);
        when(historyMapper.selectPage(any(), any())).thenReturn(mockPage);

        PageResult<ProjectStatusHistoryVO> result = service.list(1L, new ProjectStatusHistoryQueryDTO());

        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("UTSH-004: manualCreate 普通用户 → 抛 AccessDenied")
    void testManualCreateAsUser() {
        userContext.when(UserContext::isAdmin).thenReturn(false);

        ProjectStatusHistoryCreateDTO dto = createDTO("DRAFT", "IN_PROGRESS", "MANUAL_EDIT");

        assertThatThrownBy(() -> service.manualCreate(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅 ADMIN");
        verify(historyMapper, never()).insert(any());
    }

    @Test
    @DisplayName("UTSH-005: manualCreate ADMIN → 返新 ID")
    void testManualCreateAsAdmin() {
        userContext.when(UserContext::isAdmin).thenReturn(true);
        userContext.when(UserContext::getCurrentUsername).thenReturn("admin");
        when(projectMapper.selectById(1L)).thenReturn(project);
        when(historyMapper.selectCount(any())).thenReturn(0L);
        org.mockito.Mockito.doAnswer(inv -> {
            ProjectStatusHistory h = inv.getArgument(0);
            h.setId(42L);
            return 1;
        }).when(historyMapper).insert(any(ProjectStatusHistory.class));

        ProjectStatusHistoryCreateDTO dto = createDTO("DRAFT", "IN_PROGRESS", "MANUAL_EDIT");
        Long id = service.manualCreate(1L, dto);

        assertThat(id).isEqualTo(42L);
        verify(historyMapper).insert(any(ProjectStatusHistory.class));
    }

    @Test
    @DisplayName("UTSH-006: manualCreate event != MANUAL_EDIT → 抛异常")
    void testManualCreateEventNotManualEdit() {
        userContext.when(UserContext::isAdmin).thenReturn(true);
        ProjectStatusHistoryCreateDTO dto = createDTO("DRAFT", "IN_PROGRESS", "EVAL_START");

        assertThatThrownBy(() -> service.manualCreate(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("MANUAL_EDIT");
    }

    @Test
    @DisplayName("UTSH-007: manualCreate from == to → 抛异常")
    void testManualCreateSameStatus() {
        userContext.when(UserContext::isAdmin).thenReturn(true);
        ProjectStatusHistoryCreateDTO dto = createDTO("COMPLETED", "COMPLETED", "MANUAL_EDIT");

        assertThatThrownBy(() -> service.manualCreate(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能相同");
    }

    @Test
    @DisplayName("UTSH-008: manualCreate changeTime 未来 → 抛异常")
    void testManualCreateFutureTime() {
        userContext.when(UserContext::isAdmin).thenReturn(true);
        ProjectStatusHistoryCreateDTO dto = createDTO("DRAFT", "IN_PROGRESS", "MANUAL_EDIT");
        dto.setChangeTime(LocalDateTime.now().plusYears(100));

        assertThatThrownBy(() -> service.manualCreate(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未来");
    }

    @Test
    @DisplayName("UTSH-009: manualCreate 重复记录 → 抛异常")
    void testManualCreateDuplicate() {
        userContext.when(UserContext::isAdmin).thenReturn(true);
        when(projectMapper.selectById(1L)).thenReturn(project);
        when(historyMapper.selectCount(any())).thenReturn(1L);  // 已存在

        ProjectStatusHistoryCreateDTO dto = createDTO("DRAFT", "IN_PROGRESS", "MANUAL_EDIT");

        assertThatThrownBy(() -> service.manualCreate(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已存在");
    }

    @Test
    @DisplayName("UTSH-010: state machine transition 自动写历史（覆盖用，无具体 mock）")
    void testStateMachineRecordsHistory() {
        // 验证：StateMachine.recordHistory 被调用时，historyMapper.insert 收到正确入参
        // 此用例仅覆盖 recordHistory 的方法调用约定，具体流转由 ProjectStateMachineTest 保证
        userContext.when(UserContext::getCurrentUsername).thenReturn("admin");
        project.setId(99L);
        org.mockito.Mockito.doAnswer(inv -> {
            ProjectStatusHistory h = inv.getArgument(0);
            assertThat(h.getProjectId()).isEqualTo(99L);
            assertThat(h.getToStatus()).isEqualTo("IN_PROGRESS");
            assertThat(h.getEvent()).isEqualTo("EVAL_START");
            assertThat(h.getOperator()).isEqualTo("admin");
            assertThat(h.getManualEdit()).isEqualTo(0);
            return 1;
        }).when(historyMapper).insert(any(ProjectStatusHistory.class));

        // 直接调 StateMachine 的 recordHistory
        ProjectStateMachine sm = new ProjectStateMachine(projectMapper, historyMapper);
        sm.recordHistory(project, com.migrametric.entity.project.ProjectStatus.DRAFT,
                com.migrametric.entity.project.ProjectStatus.IN_PROGRESS,
                ProjectEvent.EVAL_START, false);

        verify(historyMapper).insert(any(ProjectStatusHistory.class));
    }

    // ========== helpers ==========

    private ProjectStatusHistory history(Long id, Long projectId, String toStatus, String event) {
        ProjectStatusHistory h = new ProjectStatusHistory();
        h.setId(id);
        h.setProjectId(projectId);
        h.setToStatus(toStatus);
        h.setEvent(event);
        h.setOperator("admin");
        h.setChangeTime(LocalDateTime.now());
        h.setManualEdit(0);
        return h;
    }

    private ProjectStatusHistoryCreateDTO createDTO(String from, String to, String event) {
        ProjectStatusHistoryCreateDTO dto = new ProjectStatusHistoryCreateDTO();
        dto.setFromStatus(from);
        dto.setToStatus(to);
        dto.setEvent(event);
        dto.setChangeTime(LocalDateTime.now().minusDays(1));
        dto.setReason("测试");
        return dto;
    }
}
