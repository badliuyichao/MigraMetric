package com.migrametric.dto.export;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 导出请求DTO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "导出请求DTO")
public class ExportRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 导出格式：EXCEL, PDF, WORD
     */
    @Schema(description = "导出格式")
    private String format;

    /**
     * 导出内容（章节）
     */
    @Schema(description = "导出内容")
    private ExportSection sections;

    /**
     * 导出内容章节
     */
    @Data
    public static class ExportSection implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 项目基本信息
         */
        @Schema(description = "项目基本信息")
        private Boolean projectInfo = true;

        /**
         * 评估指标汇总
         */
        @Schema(description = "评估指标汇总")
        private Boolean evaluationSummary = true;

        /**
         * 工作量评估明细
         */
        @Schema(description = "工作量评估明细")
        private Boolean workloadDetail = true;

        /**
         * 可视化图表
         */
        @Schema(description = "可视化图表")
        private Boolean charts = true;

        /**
         * 风险提示与建议
         */
        @Schema(description = "风险提示与建议")
        private Boolean riskWarnings = true;
    }
}
