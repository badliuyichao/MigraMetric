package com.migrametric.service.project;

import com.migrametric.common.BusinessException;
import com.migrametric.entity.project.Project;
import com.migrametric.entity.project.ProjectStatus;
import com.migrametric.mapper.project.ProjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 项目状态机单测（REQ-3.2.3 重构）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("项目状态机测试")
class ProjectStateMachineTest {

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectStateMachine stateMachine;

    private Project project;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setProjectName("测试项目");
        project.setUserId(1L);
        project.setCreateTime(LocalDateTime.now());
        project.setCreateBy("admin");
    }

    @Test
    @DisplayName("STM-001: DRAFT + EVAL_START → IN_PROGRESS")
    void testDraftToInProgress() {
        project.setStatus(ProjectStatus.DRAFT.getCode());
        when(projectMapper.selectById(1L)).thenReturn(project);

        ProjectStatus next = stateMachine.transition(1L, ProjectEvent.EVAL_START);

        assertThat(next).isEqualTo(ProjectStatus.IN_PROGRESS);
        verify(projectMapper).updateById(any(Project.class));
    }

    @Test
    @DisplayName("STM-002: IN_PROGRESS + EVAL_COMPLETE → COMPLETED")
    void testInProgressToCompleted() {
        project.setStatus(ProjectStatus.IN_PROGRESS.getCode());
        when(projectMapper.selectById(1L)).thenReturn(project);

        ProjectStatus next = stateMachine.transition(1L, ProjectEvent.EVAL_COMPLETE);

        assertThat(next).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    @DisplayName("STM-003: COMPLETED + ARCHIVE → ARCHIVED")
    void testCompletedToArchived() {
        project.setStatus(ProjectStatus.COMPLETED.getCode());
        when(projectMapper.selectById(1L)).thenReturn(project);

        ProjectStatus next = stateMachine.transition(1L, ProjectEvent.ARCHIVE);

        assertThat(next).isEqualTo(ProjectStatus.ARCHIVED);
    }

    @Test
    @DisplayName("STM-004: DRAFT + ARCHIVE（非法流转）→ 抛异常")
    void testDraftCannotArchive() {
        project.setStatus(ProjectStatus.DRAFT.getCode());
        when(projectMapper.selectById(1L)).thenReturn(project);

        assertThatThrownBy(() -> stateMachine.transition(1L, ProjectEvent.ARCHIVE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("非法状态流转");
        verify(projectMapper, never()).updateById(any(Project.class));
    }

    @Test
    @DisplayName("STM-005: ARCHIVED + 任何事件（终态）→ 抛异常")
    void testArchivedIsFinal() {
        project.setStatus(ProjectStatus.ARCHIVED.getCode());
        when(projectMapper.selectById(1L)).thenReturn(project);

        assertThatThrownBy(() -> stateMachine.transition(1L, ProjectEvent.ARCHIVE))
                .isInstanceOf(BusinessException.class);
        verify(projectMapper, never()).updateById(any(Project.class));
    }

    @Test
    @DisplayName("STM-006: 项目不存在 → 抛异常")
    void testProjectNotFound() {
        when(projectMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> stateMachine.transition(999L, ProjectEvent.EVAL_START))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("项目不存在");
    }

    @Test
    @DisplayName("STM-007: 未知 status 字符串 fallback 到 DRAFT（健壮性）")
    void testUnknownStatusFallback() {
        project.setStatus("INVALID_VALUE");
        when(projectMapper.selectById(1L)).thenReturn(project);

        // 未知 status fallback 到 DRAFT，DRAFT + EVAL_START → IN_PROGRESS 合法
        ProjectStatus next = stateMachine.transition(1L, ProjectEvent.EVAL_START);
        assertThat(next).isEqualTo(ProjectStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("STM-008: 完整流转链 DRAFT → IN_PROGRESS → COMPLETED → ARCHIVED")
    void testFullChain() {
        project.setStatus(ProjectStatus.DRAFT.getCode());
        when(projectMapper.selectById(1L)).thenReturn(project);

        assertThat(stateMachine.transition(1L, ProjectEvent.EVAL_START))
                .isEqualTo(ProjectStatus.IN_PROGRESS);
        project.setStatus(ProjectStatus.IN_PROGRESS.getCode());

        assertThat(stateMachine.transition(1L, ProjectEvent.EVAL_COMPLETE))
                .isEqualTo(ProjectStatus.COMPLETED);
        project.setStatus(ProjectStatus.COMPLETED.getCode());

        assertThat(stateMachine.transition(1L, ProjectEvent.ARCHIVE))
                .isEqualTo(ProjectStatus.ARCHIVED);
    }
}
