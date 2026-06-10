package com.migrametric.vo.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 全局统计 VO（REQ-3.4.2）
 *
 * 包含 3 个内部类：
 * - GlobalAggregationVO：按维度聚合结果（module/type/complexity）
 * - GlobalRankingVO：排行榜结果（workload/userCount/dataVolume）
 * - AggregationItemVO：聚合项（饼图/柱图共用）
 *
 * @author MigraMetric Team
 */
public class StatisticsGlobalVO {

    /**
     * 全局聚合统计
     */
    @Data
    @Schema(description = "全局聚合统计")
    public static class GlobalAggregationVO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "维度：module/type/complexity")
        private String dimension;

        @Schema(description = "该维度总值（人天），仅 module/type 返回")
        private BigDecimal total;

        /**
         * module / type 维度的明细分项
         */
        @Schema(description = "分项列表（module/type 维度）")
        private List<AggregationItemVO> items;

        /**
         * complexity 维度专用
         */
        @Schema(description = "数据量阶梯分布（仅 complexity 维度返回）")
        private List<LadderBucketVO> dataVolumeDistribution;

        @Schema(description = "用户数阶梯分布（仅 complexity 维度返回）")
        private List<LadderBucketVO> userCountDistribution;

        @Schema(description = "高复杂度模块列表（系数≥1.5，仅 complexity 维度返回）")
        private List<HighComplexityModuleVO> highComplexityModules;
    }

    /**
     * 聚合项（饼图分块 / 柱图单柱）
     */
    @Data
    @Schema(description = "聚合项")
    public static class AggregationItemVO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "名称（模块名/类型名）")
        private String name;

        @Schema(description = "工作量（人天）")
        private BigDecimal value;

        @Schema(description = "占比（%），0-100，保留 2 位小数")
        private BigDecimal percentage;

        @Schema(description = "出现在多少个项目中")
        private Integer projectCount;
    }

    /**
     * 阶梯桶（complexity 维度专用）
     */
    @Data
    @Schema(description = "阶梯桶")
    public static class LadderBucketVO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "阶梯名称")
        private String ladderName;

        @Schema(description = "项目数")
        private Integer projectCount;

        @Schema(description = "累计工作量（人天）")
        private BigDecimal totalWorkload;
    }

    /**
     * 高复杂度模块
     */
    @Data
    @Schema(description = "高复杂度模块")
    public static class HighComplexityModuleVO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "模块名称")
        private String moduleName;

        @Schema(description = "加权系数")
        private BigDecimal weight;

        @Schema(description = "出现在多少个项目中")
        private Integer projectCount;
    }

    /**
     * 排行榜 VO
     */
    @Data
    @Schema(description = "全局排行榜")
    public static class GlobalRankingVO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "排序指标：workload/userCount/dataVolume")
        private String metric;

        @Schema(description = "排行榜条目")
        private List<RankingItemVO> items;
    }

    /**
     * 排行榜条目
     */
    @Data
    @Schema(description = "排行榜条目")
    public static class RankingItemVO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "项目ID")
        private Long projectId;

        @Schema(description = "项目名称")
        private String projectName;

        @Schema(description = "客户名称")
        private String customerName;

        @Schema(description = "指标值")
        private BigDecimal value;

        @Schema(description = "单位（人天/人/万条）")
        private String unit;
    }
}
