package com.migrametric.service.project;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.migrametric.common.BusinessException;
import com.migrametric.common.PageResult;
import com.migrametric.common.ResultCode;
import com.migrametric.context.UserContext;
import com.migrametric.dto.project.ProjectStatusHistoryCreateDTO;
import com.migrametric.dto.project.ProjectStatusHistoryQueryDTO;
import com.migrametric.entity.project.Project;
import com.migrametric.entity.project.ProjectStatus;
import com.migrametric.entity.project.ProjectStatusHistory;
import com.migrametric.mapper.project.ProjectMapper;
import com.migrametric.mapper.project.ProjectStatusHistoryMapper;
import com.migrametric.vo.project.ProjectStatusHistoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目状态历史 Service（REQ-§3.2.4）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectStatusHistoryService {

    private final ProjectStatusHistoryMapper historyMapper;
    private final ProjectMapper projectMapper;

    /**
     * 分页查询项目状态历史
     */
    public PageResult<ProjectStatusHistoryVO> list(Long projectId, ProjectStatusHistoryQueryDTO query) {
        // 权限：普通用户只能查自己创建的项目，ADMIN 可查所有
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        if (!UserContext.isAdmin() && !project.getUserId().equals(UserContext.getCurrentUserId())) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED, "无权访问该项目历史");
        }

        int pageNum = query.getPageNum() == null ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null ? 10 : Math.min(query.getPageSize(), 50);

        // 规范化：空字符串转 null（避免 eq('',...) 匹配不到）
        String operator = (query.getOperator() == null || query.getOperator().isBlank()) ? null : query.getOperator();
        String event = (query.getEvent() == null || query.getEvent().isBlank()) ? null : query.getEvent();

        LambdaQueryWrapper<ProjectStatusHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectStatusHistory::getProjectId, projectId);
        if (operator != null) {
            wrapper.eq(ProjectStatusHistory::getOperator, operator);
        }
        if (event != null) {
            wrapper.eq(ProjectStatusHistory::getEvent, event);
        }
        wrapper.orderByDesc(ProjectStatusHistory::getChangeTime);

        IPage<ProjectStatusHistory> page = historyMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<ProjectStatusHistoryVO> records = page.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    /**
     * 人工补录（仅 ADMIN）
     */
    @Transactional
    public Long manualCreate(Long projectId, ProjectStatusHistoryCreateDTO dto) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED, "仅 ADMIN 可补录");
        }
        // 强制 MANUAL_EDIT
        if (!"MANUAL_EDIT".equals(dto.getEvent())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "补录 event 必须为 MANUAL_EDIT");
        }
        // 状态合法性
        ProjectStatus from = parseStatus(dto.getFromStatus());
        ProjectStatus to = parseStatus(dto.getToStatus());
        if (from == to) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "fromStatus 与 toStatus 不能相同");
        }
        // 未来时间
        if (dto.getChangeTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "changeTime 不能为未来时间");
        }
        // 项目存在
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        // 重复检查
        LambdaQueryWrapper<ProjectStatusHistory> dup = new LambdaQueryWrapper<>();
        dup.eq(ProjectStatusHistory::getProjectId, projectId)
                .eq(ProjectStatusHistory::getFromStatus, from.getCode())
                .eq(ProjectStatusHistory::getToStatus, to.getCode())
                .eq(ProjectStatusHistory::getEvent, "MANUAL_EDIT")
                .eq(ProjectStatusHistory::getChangeTime, dto.getChangeTime());
        if (historyMapper.selectCount(dup) > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "已存在相同的补录记录");
        }
        // 写
        ProjectStatusHistory h = new ProjectStatusHistory();
        h.setProjectId(projectId);
        h.setFromStatus(from.getCode());
        h.setToStatus(to.getCode());
        h.setEvent("MANUAL_EDIT");
        h.setOperator(UserContext.getCurrentUsername());
        h.setReason(dto.getReason());
        h.setChangeTime(dto.getChangeTime());
        h.setManualEdit(1);
        historyMapper.insert(h);
        log.info("ADMIN 补录项目状态历史: projectId={}, user={}, from={}, to={}",
                projectId, UserContext.getCurrentUsername(), from.getCode(), to.getCode());
        return h.getId();
    }

    private ProjectStatus parseStatus(String code) {
        if (code == null) return null;
        try {
            return ProjectStatus.fromCode(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "未知状态: " + code);
        }
    }

    private ProjectStatusHistoryVO toVO(ProjectStatusHistory h) {
        ProjectStatusHistoryVO v = new ProjectStatusHistoryVO();
        v.setId(h.getId());
        v.setProjectId(h.getProjectId());
        v.setFromStatus(h.getFromStatus());
        v.setToStatus(h.getToStatus());
        v.setEvent(h.getEvent());
        v.setOperator(h.getOperator());
        v.setReason(h.getReason());
        v.setChangeTime(h.getChangeTime());
        v.setManualEdit(h.getManualEdit() != null && h.getManualEdit() == 1);
        return v;
    }
}
