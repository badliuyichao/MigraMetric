package com.migrametric.service.export.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.migrametric.common.BusinessException;
import com.migrametric.common.ResultCode;
import com.migrametric.dto.export.ExportRequestDTO;
import com.migrametric.entity.evaluation.Evaluation;
import com.migrametric.entity.evaluation.ProjectModuleConfig;
import com.migrametric.entity.ladder.DataVolumeLadder;
import com.migrametric.entity.ladder.UserCountLadder;
import com.migrametric.entity.module.Module;
import com.migrametric.entity.project.Project;
import com.migrametric.mapper.evaluation.EvaluationMapper;
import com.migrametric.mapper.evaluation.ProjectModuleConfigMapper;
import com.migrametric.mapper.ladder.DataVolumeLadderMapper;
import com.migrametric.mapper.ladder.UserCountLadderMapper;
import com.migrametric.mapper.module.ModuleMapper;
import com.migrametric.mapper.project.ProjectMapper;
import com.migrametric.service.export.PdfExportService;
import com.migrametric.service.statistics.StatisticsService;
import com.migrametric.vo.statistics.StatisticsResultVO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * PDF导出服务实现类
 *
 * @author MigraMetric Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfExportServiceImpl implements PdfExportService {

    private final ProjectMapper projectMapper;
    private final EvaluationMapper evaluationMapper;
    private final ProjectModuleConfigMapper moduleConfigMapper;
    private final ModuleMapper moduleMapper;
    private final DataVolumeLadderMapper dataVolumeLadderMapper;
    private final UserCountLadderMapper userCountLadderMapper;
    private final StatisticsService statisticsService;

    /**
     * 工期计算常量（人天/人月）
     */
    private static final BigDecimal DAYS_PER_MONTH = new BigDecimal("22");

    /**
     * 页面尺寸
     */
    private static final Rectangle PAGE_SIZE = PageSize.A4;

    /**
     * 页边距
     */
    private static final float MARGIN = 50;

    @Override
    public byte[] exportToPdf(ExportRequestDTO request) {
        log.info("开始导出PDF报告, projectId: {}", request.getProjectId());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // 创建文档
            Document document = new Document(PAGE_SIZE, MARGIN, MARGIN, MARGIN, MARGIN);

            // 创建PDF写入器
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // 打开文档
            document.open();

            // 获取项目信息
            Project project = projectMapper.selectById(request.getProjectId());
            if (project == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
            }

            // 获取评估信息
            Evaluation evaluation = getEvaluation(request.getProjectId());
            StatisticsResultVO statistics = statisticsService.getStatistics(request.getProjectId());

            // 添加内容
            if (request.getSections() == null || request.getSections().getProjectInfo()) {
                addProjectInfoSection(document, project, evaluation);
            }

            if (request.getSections() == null || request.getSections().getEvaluationSummary()) {
                addEvaluationSummarySection(document, statistics);
            }

            if (request.getSections() == null || request.getSections().getWorkloadDetail()) {
                addWorkloadDetailSection(document, request.getProjectId(), evaluation);
            }

            if (request.getSections() == null || request.getSections().getRiskWarnings()) {
                addRiskWarningSection(document, statistics);
            }

            // 关闭文档
            document.close();

            log.info("PDF报告导出完成, projectId: {}", request.getProjectId());
            return out.toByteArray();

        } catch (BusinessException e) {
            // 业务异常直接抛出，不包装
            throw e;
        } catch (Exception e) {
            log.error("PDF导出失败", e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "PDF导出失败");
        }
    }

    /**
     * 获取评估记录
     */
    private Evaluation getEvaluation(Long projectId) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getProjectId, projectId);
        Evaluation evaluation = evaluationMapper.selectOne(wrapper);
        if (evaluation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "评估记录不存在");
        }
        return evaluation;
    }

    /**
     * 添加项目基本信息章节
     */
    private void addProjectInfoSection(Document document, Project project, Evaluation evaluation) throws DocumentException {
        // 标题
        addTitle(document, "一、项目基本信息");

        // 创建表格
        PdfPTable table = createTable(4);
        table.setWidthPercentage(100);

        // 基本信息
        addLabelValueCell(table, "项目名称", nvl(project.getProjectName()));
        addLabelValueCell(table, "客户名称", nvl(project.getCustomerName()));
        addLabelValueCell(table, "项目负责人", nvl(project.getProjectLeader()));
        addLabelValueCell(table, "联系方式", nvl(project.getContact()));
        addLabelValueCell(table, "评估日期", project.getEvaluationDate() != null ? project.getEvaluationDate().toString() : "-");
        addLabelValueCell(table, "源系统", "系统" + project.getSourceSystemId());
        addLabelValueCell(table, "目标系统", "系统" + project.getTargetSystemId());
        addLabelValueCell(table, "评估状态", nvl(project != null ? project.getStatus() : ""));
        addLabelValueCell(table, "总工作量", formatWorkload(evaluation.getTotalWorkload()) + " 人天");
        addLabelValueCell(table, "预估工期", formatEstimatedMonths(evaluation.getTotalWorkload()) + " 人月");

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * 添加评估指标汇总章节
     */
    private void addEvaluationSummarySection(Document document, StatisticsResultVO statistics) throws DocumentException {
        // 标题
        addTitle(document, "二、评估指标汇总");

        // 工作量汇总
        addSubTitle(document, "2.1 工作量汇总");
        PdfPTable summaryTable = createTable(2);
        summaryTable.setWidthPercentage(50);
        addLabelValueCell(summaryTable, "总工作量", formatWorkload(statistics.getTotalWorkload()) + " 人天");
        addLabelValueCell(summaryTable, "预估工期", formatEstimatedMonths(statistics.getTotalWorkload()) + " 人月");
        document.add(summaryTable);
        document.add(new Paragraph("\n"));

        // 工作量类型分布
        addSubTitle(document, "2.2 工作量类型分布");
        if (statistics.getWorkloadTypeDistribution() != null && !statistics.getWorkloadTypeDistribution().isEmpty()) {
            PdfPTable typeTable = createTable(3);
            typeTable.setWidthPercentage(70);
            addHeaderCell(typeTable, "类型");
            addHeaderCell(typeTable, "工作量（人天）");
            addHeaderCell(typeTable, "占比");

            for (StatisticsResultVO.WorkloadTypeDistribution type : statistics.getWorkloadTypeDistribution()) {
                addTextCell(typeTable, type.getType());
                addNumberCell(typeTable, type.getWorkload());
                addTextCell(typeTable, formatPercentage(type.getPercentage()));
            }
            document.add(typeTable);
        }
        document.add(new Paragraph("\n"));

        // 评估概览
        addSubTitle(document, "2.3 评估指标概览");
        if (statistics.getEvaluationOverview() != null) {
            StatisticsResultVO.EvaluationOverview overview = statistics.getEvaluationOverview();
            PdfPTable overviewTable = createTable(4);
            overviewTable.setWidthPercentage(100);
            addLabelValueCell(overviewTable, "已选模块数", overview.getModuleCount() + " 个");
            addLabelValueCell(overviewTable, "数据量", formatBigDecimal(overview.getDataVolume()) + " 万条");
            addLabelValueCell(overviewTable, "用户数", overview.getUserCount() + " 人");
            addLabelValueCell(overviewTable, "报表数量", overview.getReportCount() + " 个");
            addLabelValueCell(overviewTable, "客开情况", (overview.getHasCustomDev() != null && overview.getHasCustomDev()) ? "有客开" : "无客开");
            addEmptyCell(overviewTable);
            addEmptyCell(overviewTable);
            addEmptyCell(overviewTable);
            document.add(overviewTable);
        }
        document.add(new Paragraph("\n"));
    }

    /**
     * 添加工作量明细章节
     */
    private void addWorkloadDetailSection(Document document, Long projectId, Evaluation evaluation) throws DocumentException {
        // 标题
        addTitle(document, "三、工作量评估明细");

        // 模块明细表
        addSubTitle(document, "3.1 模块工作量明细");

        PdfPTable moduleTable = createTable(6);
        moduleTable.setWidthPercentage(100);
        addHeaderCell(moduleTable, "序号");
        addHeaderCell(moduleTable, "模块名称");
        addHeaderCell(moduleTable, "分类");
        addHeaderCell(moduleTable, "基础人天");
        addHeaderCell(moduleTable, "加权系数");
        addHeaderCell(moduleTable, "工作量");

        // 获取模块配置
        LambdaQueryWrapper<ProjectModuleConfig> configWrapper = new LambdaQueryWrapper<>();
        configWrapper.eq(ProjectModuleConfig::getProjectId, projectId);
        List<ProjectModuleConfig> configs = moduleConfigMapper.selectList(configWrapper);

        if (!configs.isEmpty()) {
            List<Long> moduleIds = configs.stream().map(ProjectModuleConfig::getModuleId).collect(Collectors.toList());
            List<Module> modules = moduleMapper.selectBatchIds(moduleIds);
            Map<Long, Module> moduleMap = modules.stream().collect(Collectors.toMap(Module::getId, Function.identity()));

            BigDecimal dataVolumeWeight = getWeight(evaluation.getDataVolumeLadderId(), dataVolumeLadderMapper);
            BigDecimal userCountWeight = getWeight(evaluation.getUserCountLadderId(), userCountLadderMapper);

            int index = 1;
            BigDecimal totalCoreWorkload = BigDecimal.ZERO;

            for (ProjectModuleConfig config : configs) {
                Module module = moduleMap.get(config.getModuleId());
                if (module == null) continue;

                BigDecimal baseWorkload = module.getBaseWorkload() != null ? module.getBaseWorkload() : BigDecimal.ZERO;
                BigDecimal weight = config.getWeight() != null ? config.getWeight() :
                        (module.getDefaultWeight() != null ? module.getDefaultWeight() : BigDecimal.ONE);

                BigDecimal moduleWorkload = baseWorkload.multiply(weight).multiply(dataVolumeWeight).multiply(userCountWeight).setScale(2, RoundingMode.HALF_UP);
                totalCoreWorkload = totalCoreWorkload.add(moduleWorkload);

                addNumberCell(moduleTable, index++);
                addTextCell(moduleTable, module.getModuleName());
                addTextCell(moduleTable, nvl(module.getCategory()));
                addNumberCell(moduleTable, baseWorkload);
                addNumberCell(moduleTable, weight);
                addNumberCell(moduleTable, moduleWorkload);
            }

            // 合计行
            PdfPCell totalCell = new PdfPCell(new Phrase("合计", createBoldFont(11)));
            totalCell.setColspan(5);
            totalCell.setBackgroundColor(new Color(220, 220, 220));
            totalCell.setPadding(6f);
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            moduleTable.addCell(totalCell);
            addNumberCell(moduleTable, totalCoreWorkload);
        }

        document.add(moduleTable);
        document.add(new Paragraph("\n"));

        // 报表工作量和客开工作量
        addSubTitle(document, "3.2 其他工作量");
        PdfPTable otherTable = createTable(2);
        otherTable.setWidthPercentage(50);
        addLabelValueCell(otherTable, "报表工作量", formatWorkload(evaluation.getReportWorkload()) + " 人天");
        if (evaluation.getHasCustomDev() != null && evaluation.getHasCustomDev() == 1) {
            addLabelValueCell(otherTable, "客开工作量", formatWorkload(evaluation.getCustomDevWorkload()) + " 人天");
        }
        document.add(otherTable);
        document.add(new Paragraph("\n"));

        // 总计
        addSubTitle(document, "3.3 总计");
        Paragraph totalPara = new Paragraph();
        totalPara.add(new Chunk("总工作量：", createBoldFont(14)));
        totalPara.add(new Chunk(formatWorkload(evaluation.getTotalWorkload()) + " 人天", createBoldFont(14)));
        document.add(totalPara);
        document.add(new Paragraph("\n"));
    }

    /**
     * 添加风险提示章节
     */
    private void addRiskWarningSection(Document document, StatisticsResultVO statistics) throws DocumentException {
        // 标题
        addTitle(document, "四、风险提示与建议");

        if (statistics.getRiskWarnings() != null && !statistics.getRiskWarnings().isEmpty()) {
            PdfPTable riskTable = createTable(4);
            riskTable.setWidthPercentage(100);
            addHeaderCell(riskTable, "风险类型");
            addHeaderCell(riskTable, "风险等级");
            addHeaderCell(riskTable, "风险描述");
            addHeaderCell(riskTable, "建议");

            for (StatisticsResultVO.RiskWarning warning : statistics.getRiskWarnings()) {
                addTextCell(riskTable, getRiskTypeName(warning.getType()));
                addTextCell(riskTable, warning.getLevel());
                addTextCell(riskTable, warning.getDescription());
                addTextCell(riskTable, warning.getSuggestion());
            }
            document.add(riskTable);
        } else {
            document.add(new Paragraph("暂无风险提示"));
        }

        // 添加页脚
        addFooter(document);
    }

    // ========== PDF辅助方法 ==========

    private PdfPTable createTable(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        return table;
    }

    private void addTitle(Document document, String title) throws DocumentException {
        Paragraph para = new Paragraph();
        para.setSpacingBefore(20f);
        para.setSpacingAfter(15f);
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(51, 51, 51));
        para.add(new Chunk(title, titleFont));
        document.add(para);
    }

    private void addSubTitle(Document document, String subtitle) throws DocumentException {
        Paragraph para = new Paragraph();
        para.setSpacingBefore(10f);
        para.setSpacingAfter(8f);
        Font subFont = new Font(Font.HELVETICA, 13, Font.BOLD, new Color(51, 51, 51));
        para.add(new Chunk(subtitle, subFont));
        document.add(para);
    }

    private void addLabelValueCell(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, createLabelFont()));
        labelCell.setBackgroundColor(new Color(240, 240, 240));
        labelCell.setPadding(8f);
        labelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, createValueFont()));
        valueCell.setPadding(8f);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, createHeaderFont()));
        cell.setBackgroundColor(new Color(41, 128, 185));
        // 设置白色文字
        Font whiteFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
        cell.setPhrase(new Phrase(text, whiteFont));
        cell.setPadding(8f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTextCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(nvl(text), createValueFont()));
        cell.setPadding(6f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addNumberCell(PdfPTable table, BigDecimal value) {
        PdfPCell cell = new PdfPCell(new Phrase(formatWorkload(value), createValueFont()));
        cell.setPadding(6f);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
    }

    private void addNumberCell(PdfPTable table, int value) {
        PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(value), createValueFont()));
        cell.setPadding(6f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addEmptyCell(PdfPTable table) {
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.setSpacingBefore(30f);
        footer.setAlignment(Element.ALIGN_RIGHT);
        Font footerFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(128, 128, 128));
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        footer.add(new Chunk("生成日期：" + dateStr, footerFont));
        document.add(footer);
    }

    private Font createLabelFont() {
        return new Font(Font.HELVETICA, 11, Font.BOLD);
    }

    private Font createValueFont() {
        return new Font(Font.HELVETICA, 11, Font.NORMAL);
    }

    private Font createHeaderFont() {
        return new Font(Font.HELVETICA, 11, Font.BOLD);
    }

    private Font createBoldFont(float size) {
        return new Font(Font.HELVETICA, size, Font.BOLD);
    }

    private BigDecimal getWeight(Long ladderId, DataVolumeLadderMapper mapper) {
        if (ladderId == null) return BigDecimal.ONE;
        DataVolumeLadder ladder = mapper.selectById(ladderId);
        return ladder != null && ladder.getWeight() != null ? ladder.getWeight() : BigDecimal.ONE;
    }

    private BigDecimal getWeight(Long ladderId, UserCountLadderMapper mapper) {
        if (ladderId == null) return BigDecimal.ONE;
        UserCountLadder ladder = mapper.selectById(ladderId);
        return ladder != null && ladder.getWeight() != null ? ladder.getWeight() : BigDecimal.ONE;
    }

    private String formatWorkload(BigDecimal value) {
        if (value == null) return "0.00";
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatEstimatedMonths(BigDecimal workload) {
        if (workload == null) return "0.00";
        return workload.divide(DAYS_PER_MONTH, 2, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatBigDecimal(BigDecimal value) {
        if (value == null) return "-";
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatPercentage(BigDecimal value) {
        if (value == null) return "-";
        return value.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private String nvl(String value) {
        return value != null ? value : "-";
    }

    private String getRiskTypeName(String type) {
        if (type == null) return "未知";
        return switch (type) {
            case "MODULE_COMPLEXITY" -> "模块复杂度";
            case "DATA_VOLUME" -> "数据量风险";
            case "USER_SCALE" -> "用户规模风险";
            case "CUSTOM_DEV" -> "客开风险";
            default -> type;
        };
    }
}
