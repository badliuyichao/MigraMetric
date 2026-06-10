package com.migrametric.controller.statistics;

import com.migrametric.common.Result;
import com.migrametric.service.statistics.StatisticsService;
import com.migrametric.vo.statistics.StatisticsGlobalVO;
import com.migrametric.vo.statistics.StatisticsResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

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

    /**
     * 全局聚合统计（REQ-3.4.2 · ADMIN 限定）
     */
    @GetMapping("/global/aggregations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "全局聚合统计", description = "按维度（module/type/complexity）跨项目聚合")
    public Result<StatisticsGlobalVO.GlobalAggregationVO> getGlobalAggregations(
            @Parameter(description = "维度", required = true, example = "module")
            @RequestParam(name = "dimension") String dimension,
            @Parameter(description = "评估日期起点（yyyy-MM-dd）")
            @RequestParam(name = "dateFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "评估日期终点（yyyy-MM-dd）")
            @RequestParam(name = "dateTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @Parameter(description = "源系统ID")
            @RequestParam(name = "sourceSystemId", required = false) Long sourceSystemId,
            @Parameter(description = "目标系统ID")
            @RequestParam(name = "targetSystemId", required = false) Long targetSystemId,
            @Parameter(description = "项目状态（默认 COMPLETED）", example = "COMPLETED")
            @RequestParam(name = "status", required = false) String status) {
        log.info("全局聚合统计: dimension={}", dimension);
        StatisticsGlobalVO.GlobalAggregationVO result = statisticsService.getGlobalAggregations(
                dimension, dateFrom, dateTo, sourceSystemId, targetSystemId, status);
        return Result.success(result);
    }

    /**
     * 全局排行榜（REQ-3.4.2 · ADMIN 限定）
     */
    @GetMapping("/global/ranking")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "全局排行榜", description = "按工作量/用户数/数据量排序的 Top N 项目")
    public Result<StatisticsGlobalVO.GlobalRankingVO> getGlobalRanking(
            @Parameter(description = "排序指标", required = true, example = "workload")
            @RequestParam(name = "metric") String metric,
            @Parameter(description = "返回条数（1-50，超界自动截到 50）", example = "10")
            @RequestParam(name = "limit", required = false) Integer limit) {
        log.info("全局排行榜: metric={}, limit={}", metric, limit);
        StatisticsGlobalVO.GlobalRankingVO result = statisticsService.getGlobalRanking(metric, limit);
        return Result.success(result);
    }
}

