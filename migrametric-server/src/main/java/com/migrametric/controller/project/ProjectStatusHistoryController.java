package com.migrametric.controller.project;

import com.migrametric.common.PageResult;
import com.migrametric.common.Result;
import com.migrametric.dto.project.ProjectStatusHistoryCreateDTO;
import com.migrametric.dto.project.ProjectStatusHistoryQueryDTO;
import com.migrametric.service.project.ProjectStatusHistoryService;
import com.migrametric.vo.project.ProjectStatusHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目状态历史 Controller（REQ-§3.2.4）
 */
@Tag(name = "项目状态历史", description = "项目状态变更历史查询与人工补录")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/status-history")
public class ProjectStatusHistoryController {

    private final ProjectStatusHistoryService service;

    @Operation(summary = "分页查询项目状态历史")
    @GetMapping
    public Result<PageResult<ProjectStatusHistoryVO>> list(
            @PathVariable Long projectId,
            ProjectStatusHistoryQueryDTO query) {
        return Result.success(service.list(projectId, query));
    }

    @Operation(summary = "人工补录状态历史（仅 ADMIN）")
    @PostMapping
    public Result<Long> manualCreate(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectStatusHistoryCreateDTO dto) {
        return Result.success(service.manualCreate(projectId, dto));
    }
}
