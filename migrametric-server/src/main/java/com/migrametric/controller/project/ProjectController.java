package com.migrametric.controller.project;

import com.migrametric.common.PageResult;
import com.migrametric.common.Result;
import com.migrametric.dto.project.ProjectCreateDTO;
import com.migrametric.dto.project.ProjectQueryDTO;
import com.migrametric.dto.project.ProjectUpdateDTO;
import com.migrametric.service.project.ProjectService;
import com.migrametric.vo.project.ProjectDetailVO;
import com.migrametric.vo.project.ProjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 项目控制器
 *
 * @author MigraMetric Team
 */
@Tag(name = "项目管理", description = "项目管理相关接口")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 创建项目
     */
    @Operation(summary = "创建项目", description = "创建新的评估项目")
    @PostMapping
    public Result<Long> create(
            @Valid @RequestBody ProjectCreateDTO createDTO) {
        Long id = projectService.create(createDTO);
        return Result.success(id);
    }

    /**
     * 分页查询项目列表
     */
    @Operation(summary = "分页查询项目", description = "支持按项目名称、客户名称、系统、状态筛选")
    @GetMapping
    public Result<PageResult<ProjectVO>> queryPage(ProjectQueryDTO queryDTO) {
        PageResult<ProjectVO> pageResult = projectService.queryPage(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取项目详情
     */
    @Operation(summary = "获取项目详情", description = "根据ID获取项目详细信息")
    @GetMapping("/{id}")
    public Result<ProjectVO> getById(
            @Parameter(description = "项目ID")
            @PathVariable Long id) {
        ProjectVO vo = projectService.getById(id);
        return Result.success(vo);
    }

    /**
     * 获取项目详情（包含评估概况）
     */
    @Operation(summary = "获取项目详情（包含评估概况）", description = "根据ID获取项目详细信息和评估概况")
    @GetMapping("/{id}/detail")
    public Result<ProjectDetailVO> getProjectDetail(
            @Parameter(description = "项目ID")
            @PathVariable Long id) {
        ProjectDetailVO vo = projectService.getProjectDetail(id);
        return Result.success(vo);
    }

    /**
     * 更新项目信息
     */
    @Operation(summary = "更新项目", description = "更新项目基本信息")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "项目ID")
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateDTO updateDTO) {
        projectService.update(id, updateDTO);
        return Result.success(null);
    }

    /**
     * 复制项目
     */
    @Operation(summary = "复制项目", description = "创建项目的副本，副本状态为草稿")
    @PostMapping("/{id}/copy")
    public Result<Long> copy(
            @Parameter(description = "项目ID")
            @PathVariable Long id) {
        Long newId = projectService.copy(id);
        return Result.success(newId);
    }

    /**
     * 删除项目
     */
    @Operation(summary = "删除项目", description = "删除项目，仅草稿状态可删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "项目ID")
            @PathVariable Long id) {
        projectService.delete(id);
        return Result.success(null);
    }

    /**
     * 归档项目
     */
    @Operation(summary = "归档项目", description = "归档项目，仅已完成状态可归档")
    @PostMapping("/{id}/archive")
    public Result<Void> archive(
            @Parameter(description = "项目ID")
            @PathVariable Long id) {
        projectService.archive(id);
        return Result.success(null);
    }
}
