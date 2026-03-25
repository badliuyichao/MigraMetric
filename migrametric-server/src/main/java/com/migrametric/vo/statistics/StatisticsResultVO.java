package com.migrametric.vo.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 统计结果VO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "统计结果VO")
public class StatisticsResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 总工作量（人天）
     */
    @Schema(description = "总工作量（人天）")
    private BigDecimal totalWorkload;

    /**
     * 预估工期（人月，按22天/人月计算）
     */
    @Schema(description = "预估工期（人月）")
    private BigDecimal estimatedMonths;

    /**
     * 工作量类型分布
     */
    @Schema(description = "工作量类型分布")
    private List<WorkloadTypeDistribution> workloadTypeDistribution;

    /**
     * 模块工作量对比列表
     */
    @Schema(description = "模块工作量对比列表")
    private List<ModuleWorkloadVO> moduleWorkloads;

    /**
     * 多维度评估指标
     */
    @Schema(description = "多维度评估指标")
    private MultiDimensionIndicators multiDimensionIndicators;

    /**
     * 风险提示列表
     */
    @Schema(description = "风险提示列表")
    private List<RiskWarning> riskWarnings;

    /**
     * 评估指标概览
     */
    @Schema(description = "评估指标概览")
    private EvaluationOverview evaluationOverview;

    /**
     * 工作量类型分布
     */
    @Data
    public static class WorkloadTypeDistribution implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 类型：核心迁移、报表迁移、客开定制
         */
        @Schema(description = "类型")
        private String type;

        /**
         * 工作量（人天）
         */
        @Schema(description = "工作量（人天）")
        private BigDecimal workload;

        /**
         * 占比（%）
         */
        @Schema(description = "占比（%）")
        private BigDecimal percentage;
    }

    /**
     * 模块工作量VO
     */
    @Data
    public static class ModuleWorkloadVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 模块名称
         */
        @Schema(description = "模块名称")
        private String moduleName;

        /**
         * 分类
         */
        @Schema(description = "分类")
        private String category;

        /**
         * 基础人天
         */
        @Schema(description = "基础人天")
        private BigDecimal baseWorkload;

        /**
         * 加权系数
         */
        @Schema(description = "加权系数")
        private BigDecimal weight;

        /**
         * 工作量（人天）
         */
        @Schema(description = "工作量（人天）")
        private BigDecimal workload;

        /**
         * 占比（%）
         */
        @Schema(description = "占比（%）")
        private BigDecimal percentage;
    }

    /**
     * 多维度评估指标
     */
    @Data
    public static class MultiDimensionIndicators implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 数据量维度值
         */
        @Schema(description = "数据量维度值（0-100）")
        private BigDecimal dataVolumeValue;

        /**
         * 数据量实际值
         */
        @Schema(description = "数据量实际值")
        private String dataVolumeActual;

        /**
         * 数据量阶梯名称
         */
        @Schema(description = "数据量阶梯名称")
        private String dataVolumeLadder;

        /**
         * 用户数维度值
         */
        @Schema(description = "用户数维度值（0-100）")
        private BigDecimal userCountValue;

        /**
         * 用户数实际值
         */
        @Schema(description = "用户数实际值")
        private String userCountActual;

        /**
         * 用户数阶梯名称
         */
        @Schema(description = "用户数阶梯名称")
        private String userCountLadder;

        /**
         * 模块数维度值
         */
        @Schema(description = "模块数维度值（0-100）")
        private BigDecimal moduleCountValue;

        /**
         * 模块数实际值
         */
        @Schema(description = "模块数实际值")
        private Integer moduleCountActual;

        /**
         * 报表数维度值
         */
        @Schema(description = "报表数维度值（0-100）")
        private BigDecimal reportCountValue;

        /**
         * 报表数实际值
         */
        @Schema(description = "报表数实际值")
        private Integer reportCountActual;

        /**
         * 客开情况维度值
         */
        @Schema(description = "客开情况维度值（0-100）")
        private BigDecimal customDevValue;

        /**
         * 是否有客开
         */
        @Schema(description = "是否有客开")
        private Boolean hasCustomDev;

        /**
         * 客开人天
         */
        @Schema(description = "客开人天")
        private BigDecimal customDevWorkload;
    }

    /**
     * 风险提示
     */
    @Data
    public static class RiskWarning implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 风险类型
         */
        @Schema(description = "风险类型")
        private String type;

        /**
         * 风险等级：高、中、低
         */
        @Schema(description = "风险等级")
        private String level;

        /**
         * 风险描述
         */
        @Schema(description = "风险描述")
        private String description;

        /**
         * 建议
         */
        @Schema(description = "建议")
        private String suggestion;
    }

    /**
     * 评估指标概览
     */
    @Data
    public static class EvaluationOverview implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 已选模块数
         */
        @Schema(description = "已选模块数")
        private Integer moduleCount;

        /**
         * 数据量（万条）
         */
        @Schema(description = "数据量（万条）")
        private BigDecimal dataVolume;

        /**
         * 数据量阶梯名称
         */
        @Schema(description = "数据量阶梯名称")
        private String dataVolumeLadder;

        /**
         * 用户数
         */
        @Schema(description = "用户数")
        private Integer userCount;

        /**
         * 用户数阶梯名称
         */
        @Schema(description = "用户数阶梯名称")
        private String userCountLadder;

        /**
         * 报表数
         */
        @Schema(description = "报表数")
        private Integer reportCount;

        /**
         * 是否有客开
         */
        @Schema(description = "是否有客开")
        private Boolean hasCustomDev;
    }
}
