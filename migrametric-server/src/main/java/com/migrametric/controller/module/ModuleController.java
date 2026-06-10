package com.migrametric.controller.module;

import com.migrametric.common.PageResult;
import com.migrametric.common.Result;
import com.migrametric.dto.module.ModuleCreateDTO;
import com.migrametric.dto.module.ModuleQueryDTO;
import com.migrametric.dto.module.ModuleUpdateDTO;
import com.migrametric.service.module.ModuleService;
import com.migrametric.vo.module.ModuleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模块控制器
 *
 * @author MigraMetric Team
 */
@Tag(name = "模块管理", description = "模块配置相关接口")
@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @Operation(summary = "分页查询模块")
    @GetMapping
    public Result<PageResult<ModuleVO>> queryPage(
            @Parameter(description = "模块名称") @RequestParam(name = "moduleName", required = false) String moduleName,
            @Parameter(description = "所属系统ID") @RequestParam(name = "systemId", required = false) Long systemId,
            @Parameter(description = "模块分类") @RequestParam(name = "category", required = false) String category,
            @Parameter(description = "状态") @RequestParam(name = "status", required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        ModuleQueryDTO queryDTO = new ModuleQueryDTO();
        queryDTO.setModuleName(moduleName);
        queryDTO.setSystemId(systemId);
        queryDTO.setCategory(category);
        queryDTO.setStatus(status);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);

        PageResult<ModuleVO> result = moduleService.queryPage(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取模块详情")
    @GetMapping("/{id}")
    public Result<ModuleVO> getById(
            @Parameter(description = "模块ID") @PathVariable Long id
    ) {
        ModuleVO vo = moduleService.getById(id);
        return Result.success(vo);
    }

    @Operation(summary = "创建模块")
    @PostMapping
    public Result<Long> create(
            @Valid @RequestBody ModuleCreateDTO createDTO
    ) {
        Long id = moduleService.create(createDTO);
        return Result.success(id);
    }

    @Operation(summary = "更新模块")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "模块ID") @PathVariable Long id,
            @Valid @RequestBody ModuleUpdateDTO updateDTO
    ) {
        updateDTO.setId(id);
        moduleService.update(updateDTO);
        return Result.success();
    }

    @Operation(summary = "删除模块")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "模块ID") @PathVariable Long id
    ) {
        moduleService.delete(id);
        return Result.success();
    }

    @Operation(summary = "启用模块")
    @PatchMapping("/{id}/enable")
    public Result<Void> enable(
            @Parameter(description = "模块ID") @PathVariable Long id
    ) {
        moduleService.enable(id);
        return Result.success();
    }

    @Operation(summary = "禁用模块")
    @PatchMapping("/{id}/disable")
    public Result<Void> disable(
            @Parameter(description = "模块ID") @PathVariable Long id
    ) {
        moduleService.disable(id);
        return Result.success();
    }

    @Operation(summary = "获取所有启用的模块")
    @GetMapping("/enabled")
    public Result<List<ModuleVO>> listEnabled(
            @Parameter(description = "所属系统ID") @RequestParam(name = "systemId", required = false) Long systemId
    ) {
        List<ModuleVO> list = moduleService.listEnabled(systemId);
        return Result.success(list);
    }

    @Operation(summary = "获取所有模块分类")
    @GetMapping("/categories")
    public Result<List<String>> listCategories(
            @Parameter(description = "所属系统ID") @RequestParam(name = "systemId", required = false) Long systemId
    ) {
        List<String> categories = moduleService.listCategories(systemId);
        return Result.success(categories);
    }
}
