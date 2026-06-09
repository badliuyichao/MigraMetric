package com.migrametric.controller.statistics;

import com.migrametric.common.Result;
import com.migrametric.service.statistics.StatisticsService;
import com.migrametric.vo.statistics.StatisticsResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计Controller
 *
 * @author MigraMetric Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
@Tag(name = "统计分析", description = "统计图表相关接口")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取首页仪表盘概览
     */
    @GetMapping("/overview")
    @Operation(summary = "获取首页仪表盘概览", description = "返回项目总数、评估次数、总工作量、用户数")
    public Result<StatisticsResultVO.DashboardOverview> getOverview() {
        log.info("获取首页仪表盘概览");
        StatisticsResultVO.DashboardOverview result = statisticsService.getOverview();
        return Result.success(result);
    }

    /**
     * 获取项目统计结果
     *
     * @param projectId 项目ID
     * @return 统计结果
     */
    @GetMapping("/{projectId}")
    @Operation(summary = "获取项目统计结果", description = "获取项目的统计图表数据，包括工作量类型分布、模块对比、多维度指标等")
    public Result<StatisticsResultVO> getStatistics(
            @Parameter(description = "项目ID", required = true)
            @PathVariable(name = "projectId") Long projectId) {
        log.info("获取项目统计结果, projectId: {}", projectId);
        StatisticsResultVO result = statisticsService.getStatistics(projectId);
        return Result.success(result);
    }
}
