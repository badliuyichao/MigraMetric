package com.migrametric.controller.evaluation;

import com.migrametric.common.Result;
import com.migrametric.dto.evaluation.EvaluationCreateDTO;
import com.migrametric.dto.evaluation.EvaluationUpdateDTO;
import com.migrametric.dto.evaluation.ModuleConfigDTO;
import com.migrametric.service.evaluation.EvaluationService;
import com.migrametric.service.evaluation.ModuleConfigService;
import com.migrametric.service.evaluation.WorkloadCalculationService;
import com.migrametric.vo.evaluation.EvaluationVO;
import com.migrametric.vo.evaluation.ModuleConfigVO;
import com.migrametric.vo.evaluation.WorkloadResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评估控制器
 *
 * @author MigraMetric Team
 */
@Tag(name = "评估管理", description = "评估相关接口")
@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final ModuleConfigService moduleConfigService;
    private final WorkloadCalculationService workloadCalculationService;

    /**
     * 创建评估记录
     */
    @Operation(summary = "创建评估记录", description = "为项目创建评估记录")
    @PostMapping
    public Result<Long> createEvaluation(@RequestBody @Valid EvaluationCreateDTO dto) {
        Long evaluationId = evaluationService.createEvaluation(dto);
        return Result.success(evaluationId);
    }

    /**
     * 获取评估详情
     */
    @Operation(summary = "获取评估详情", description = "根据项目ID获取评估详情")
    @GetMapping("/{projectId}")
    public Result<EvaluationVO> getEvaluation(
            @Parameter(description = "项目ID") @PathVariable Long projectId) {
        EvaluationVO evaluation = evaluationService.getByProjectId(projectId);
        return Result.success(evaluation);
    }

    /**
     * 保存评估指标
     */
    @Operation(summary = "保存评估指标", description = "保存项目的评估指标数据")
    @PutMapping("/{projectId}/indicators")
    public Result<Void> saveIndicators(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @RequestBody @Valid EvaluationUpdateDTO dto) {
        evaluationService.saveIndicators(projectId, dto);
        return Result.success();
    }

    /**
     * 获取项目可选模块列表
     */
    @Operation(summary = "获取项目模块列表", description = "获取项目可选的模块列表，基于目标系统")
    @GetMapping("/{projectId}/modules")
    public Result<List<ModuleConfigVO>> getProjectModules(
            @Parameter(description = "项目ID") @PathVariable Long projectId) {
        List<ModuleConfigVO> modules = moduleConfigService.getProjectModules(projectId);
        return Result.success(modules);
    }

    /**
     * 保存模块配置
     */
    @Operation(summary = "保存模块配置", description = "保存项目选择的模块及其系数配置")
    @PostMapping("/{projectId}/modules")
    public Result<Void> saveModuleConfig(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @RequestBody @Valid List<ModuleConfigDTO> modules) {
        moduleConfigService.saveModuleConfig(projectId, modules);
        return Result.success();
    }

    /**
     * 获取已配置的模块
     */
    @Operation(summary = "获取已配置模块", description = "获取项目已保存的模块配置")
    @GetMapping("/{projectId}/modules/configured")
    public Result<List<ModuleConfigVO>> getConfiguredModules(
            @Parameter(description = "项目ID") @PathVariable Long projectId) {
        List<ModuleConfigVO> modules = moduleConfigService.getConfiguredModules(projectId);
        return Result.success(modules);
    }

    /**
     * 统计已选模块数量
     */
    @Operation(summary = "统计已选模块数量", description = "统计项目已选择的模块数量")
    @GetMapping("/{projectId}/modules/count")
    public Result<Integer> countSelectedModules(
            @Parameter(description = "项目ID") @PathVariable Long projectId) {
        int count = moduleConfigService.getConfiguredModules(projectId).size();
        return Result.success(count);
    }

    /**
     * 计算工作量
     */
    @Operation(summary = "计算工作量", description = "根据评估指标和模块配置计算工作量")
    @PostMapping("/{projectId}/calculate")
    public Result<WorkloadResultVO> calculateWorkload(
            @Parameter(description = "项目ID") @PathVariable Long projectId) {
        WorkloadResultVO result = workloadCalculationService.calculateWorkload(projectId);
        return Result.success(result);
    }

    /**
     * 完成评估
     */
    @Operation(summary = "完成评估", description = "将评估状态更新为已完成")
    @PostMapping("/{projectId}/complete")
    public Result<Void> completeEvaluation(
            @Parameter(description = "项目ID") @PathVariable Long projectId) {
        evaluationService.completeEvaluation(projectId);
        return Result.success();
    }
}
