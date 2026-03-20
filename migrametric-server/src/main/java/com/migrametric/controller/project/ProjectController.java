package com.migrametric.controller.project;

import com.migrametric.common.PageResult;
import com.migrametric.common.Result;
import com.migrametric.dto.project.ProjectCreateDTO;
import com.migrametric.dto.project.ProjectQueryDTO;
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
}
