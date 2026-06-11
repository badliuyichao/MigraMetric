package com.migrametric.service.project;

import com.migrametric.common.BusinessException;
import com.migrametric.common.ResultCode;
import com.migrametric.entity.project.Project;
import com.migrametric.entity.project.ProjectStatus;
import com.migrametric.entity.project.ProjectStatusHistory;
import com.migrametric.mapper.project.ProjectMapper;
import com.migrametric.mapper.project.ProjectStatusHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 项目状态机（REQ-3.2.3 重构）
 *
 * <p>集中所有 project.status 流转：
 * <ul>
 *   <li>DRAFT → IN_PROGRESS（事件：EVAL_START）</li>
 *   <li>IN_PROGRESS → COMPLETED（事件：EVAL_COMPLETE）</li>
 *   <li>COMPLETED → ARCHIVED（事件：ARCHIVE）</li>
 * </ul>
 *
 * <p>调用方只需 {@code stateMachine.transition(projectId, ProjectEvent.EVAL_START)}，
 * 合法性校验与字段更新均由本类完成，避免 P0/P1 类"漏改 project.status" bug 复发。
 *
 * @author MigraMetric Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectStateMachine {

    private final ProjectMapper projectMapper;
    private final ProjectStatusHistoryMapper projectStatusHistoryMapper;

    /** DRAFT 合法事件 */
    private static final Set<ProjectEvent> FROM_DRAFT =
            EnumSet.of(ProjectEvent.EVAL_START);

    /** IN_PROGRESS 合法事件 */
    private static final Set<ProjectEvent> FROM_IN_PROGRESS =
            EnumSet.of(ProjectEvent.EVAL_COMPLETE);

    /** COMPLETED 合法事件 */
    private static final Set<ProjectEvent> FROM_COMPLETED =
            EnumSet.of(ProjectEvent.ARCHIVE);

    /** ARCHIVED 终态，不允许任何事件 */
    private static final Set<ProjectEvent> FROM_ARCHIVED = EnumSet.noneOf(ProjectEvent.class);

    private static final Map<ProjectStatus, Set<ProjectEvent>> TRANSITIONS = new EnumMap<>(ProjectStatus.class);
    static {
        TRANSITIONS.put(ProjectStatus.DRAFT, FROM_DRAFT);
        TRANSITIONS.put(ProjectStatus.IN_PROGRESS, FROM_IN_PROGRESS);
        TRANSITIONS.put(ProjectStatus.COMPLETED, FROM_COMPLETED);
        TRANSITIONS.put(ProjectStatus.ARCHIVED, FROM_ARCHIVED);
    }

    /**
     * 触发状态流转
     *
     * @param projectId 项目 ID
     * @param event    触发事件
     * @return 流转后的状态
     * @throws BusinessException 项目不存在或流转非法
     */
    public ProjectStatus transition(Long projectId, ProjectEvent event) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        ProjectStatus current = parseStatus(project.getStatus());
        Set<ProjectEvent> allowed = TRANSITIONS.getOrDefault(current, EnumSet.noneOf(ProjectEvent.class));
        if (!allowed.contains(event)) {
            throw new BusinessException(
                    ResultCode.PARAM_INVALID,
                    "非法状态流转: " + current.getCode() + " 不接受事件 " + event);
        }
        ProjectStatus next = nextStatus(current, event);
        project.setStatus(next.getCode());
        project.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(project);
        // 同一事务内追加历史（BUG-§3.2.4 REQ）
        recordHistory(project, current, next, event, false);
        log.info("项目状态流转: projectId={}, {} -> {} (event={})",
                projectId, current.getCode(), next.getCode(), event);
        return next;
    }

    /**
     * 写历史行（由 service 层与 state machine 共用）
     */
    public void recordHistory(Project project, ProjectStatus from, ProjectStatus to,
                              ProjectEvent event, boolean manualEdit) {
        ProjectStatusHistory h = new ProjectStatusHistory();
        h.setProjectId(project.getId());
        h.setFromStatus(from == null ? null : from.getCode());
        h.setToStatus(to.getCode());
        h.setEvent(event.name());
        h.setOperator(com.migrametric.context.UserContext.getCurrentUsername());
        h.setReason(null);
        h.setChangeTime(LocalDateTime.now());
        h.setManualEdit(manualEdit ? 1 : 0);
        projectStatusHistoryMapper.insert(h);
    }

    private ProjectStatus parseStatus(String code) {
        if (code == null) return ProjectStatus.DRAFT;
        try {
            return ProjectStatus.fromCode(code);
        } catch (IllegalArgumentException e) {
            log.warn("未知 status 字符串: {}, fallback to DRAFT", code);
            return ProjectStatus.DRAFT;
        }
    }

    private ProjectStatus nextStatus(ProjectStatus current, ProjectEvent event) {
        return switch (current) {
            case DRAFT -> ProjectStatus.IN_PROGRESS;
            case IN_PROGRESS -> ProjectStatus.COMPLETED;
            case COMPLETED -> ProjectStatus.ARCHIVED;
            case ARCHIVED -> throw new BusinessException(
                    ResultCode.PARAM_INVALID, "ARCHIVED 为终态");
        };
    }
}
