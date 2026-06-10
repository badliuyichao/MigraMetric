package com.migrametric.service.statistics;

import com.migrametric.vo.statistics.StatisticsGlobalVO;
import com.migrametric.vo.statistics.StatisticsResultVO;

import java.time.LocalDate;

/**
 * 统计服务接口
 *
 * @author MigraMetric Team
 */
public interface StatisticsService {

    /**
     * 获取项目统计结果
     *
     * @param projectId 项目ID
     * @return 统计结果VO
     */
    StatisticsResultVO getStatistics(Long projectId);

    /**
     * 获取首页仪表盘概览
     *
     * @return 仪表盘概览VO
     */
    StatisticsResultVO.DashboardOverview getOverview();

    /**
     * 全局聚合统计（REQ-3.4.2）
     *
     * @param dimension       维度：module/type/complexity
     * @param dateFrom        评估日期起点（可空）
     * @param dateTo          评估日期终点（可空）
     * @param sourceSystemId  源系统ID（可空）
     * @param targetSystemId  目标系统ID（可空）
     * @param status          项目状态（默认 COMPLETED）
     * @return 聚合结果
     */
    StatisticsGlobalVO.GlobalAggregationVO getGlobalAggregations(
            String dimension, LocalDate dateFrom, LocalDate dateTo,
            Long sourceSystemId, Long targetSystemId, String status);

    /**
     * 全局排行榜（REQ-3.4.2）
     *
     * @param metric 指标：workload/userCount/dataVolume
     * @param limit  返回条数（1-50，超界自动截到 50）
     * @return 排行榜结果
     */
    StatisticsGlobalVO.GlobalRankingVO getGlobalRanking(String metric, Integer limit);
}
