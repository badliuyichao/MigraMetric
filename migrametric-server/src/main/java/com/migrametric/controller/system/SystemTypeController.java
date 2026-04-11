package com.migrametric.controller.system;

import com.migrametric.common.PageResult;
import com.migrametric.common.Result;
import com.migrametric.dto.system.SystemTypeCreateDTO;
import com.migrametric.dto.system.SystemTypeQueryDTO;
import com.migrametric.dto.system.SystemTypeUpdateDTO;
import com.migrametric.service.system.SystemTypeService;
import com.migrametric.vo.system.SystemTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统类型管理控制器
 *
 * @author MigraMetric Team
 */
@Tag(name = "系统配置", description = "系统管理接口")
@RestController
@RequestMapping("/api/system/types")
@RequiredArgsConstructor
public class SystemTypeController {

    private final SystemTypeService systemTypeService;

    /**
     * 分页查询系统类型
     */
    @Operation(summary = "分页查询系统类型", description = "支持按名称、类别、状态筛选")
    @GetMapping
    public Result<PageResult<SystemTypeVO>> queryPage(SystemTypeQueryDTO queryDTO) {
        PageResult<SystemTypeVO> pageResult = systemTypeService.queryPage(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取所有启用的系统类型（下拉框用）
     */
    @Operation(summary = "获取启用的系统类型", description = "获取所有启用的系统类型，用于下拉选择")
    @GetMapping("/enabled")
    public Result<List<SystemTypeVO>> listEnabled(
            @Parameter(description = "系统类别：1-源系统，2-目标系统")
            @RequestParam(required = false) Integer category) {
        List<SystemTypeVO> list = systemTypeService.listEnabled(category);
        return Result.success(list);
    }

    /**
     * 根据ID获取系统类型详情
     */
    @Operation(summary = "获取系统类型详情", description = "根据ID获取系统类型详细信息")
    @GetMapping("/{id}")
    public Result<SystemTypeVO> getById(
            @Parameter(description = "系统类型ID")
            @PathVariable Long id) {
        SystemTypeVO vo = systemTypeService.getById(id);
        return Result.success(vo);
    }

    /**
     * 创建系统类型
     */
    @Operation(summary = "创建系统类型", description = "创建新的系统类型")
    @PostMapping
    public Result<Long> create(
            @Valid @RequestBody SystemTypeCreateDTO createDTO) {
        Long id = systemTypeService.create(createDTO);
        return Result.success(id);
    }

    /**
     * 更新系统类型
     */
    @Operation(summary = "更新系统类型", description = "更新系统类型信息")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "系统类型ID")
            @PathVariable Long id,
            @Valid @RequestBody SystemTypeUpdateDTO updateDTO) {
        // 确保路径ID与DTO中的ID一致
        updateDTO.setId(id);
        systemTypeService.update(updateDTO);
        return Result.success();
    }

    /**
     * 删除系统类型
     */
    @Operation(summary = "删除系统类型", description = "删除系统类型（会被引用检查拦截）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "系统类型ID")
            @PathVariable Long id) {
        systemTypeService.delete(id);
        return Result.success();
    }

    /**
     * 启用系统类型
     */
    @Operation(summary = "启用系统类型", description = "启用指定的系统类型")
    @PatchMapping("/{id}/enable")
    public Result<Void> enable(
            @Parameter(description = "系统类型ID")
            @PathVariable Long id) {
        systemTypeService.enable(id);
        return Result.success();
    }

    /**
     * 禁用系统类型
     */
    @Operation(summary = "禁用系统类型", description = "禁用指定的系统类型")
    @PatchMapping("/{id}/disable")
    public Result<Void> disable(
            @Parameter(description = "系统类型ID")
            @PathVariable Long id) {
        systemTypeService.disable(id);
        return Result.success();
    }
}
