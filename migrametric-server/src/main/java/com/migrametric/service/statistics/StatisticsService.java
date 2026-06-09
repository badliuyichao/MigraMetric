package com.migrametric.service.statistics;

import com.migrametric.vo.statistics.StatisticsResultVO;

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
}
