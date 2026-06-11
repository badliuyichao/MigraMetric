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
import com.migrametric.service.export.ExcelExportService;
import com.migrametric.service.statistics.StatisticsService;
import com.migrametric.vo.statistics.StatisticsResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Excel导出服务实现类
 *
 * @author MigraMetric Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportServiceImpl implements ExcelExportService {

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
     * 默认样式
     */
    private XSSFWorkbook workbook;

    @Override
    public byte[] exportToExcel(ExportRequestDTO request) {
        log.info("开始导出Excel报告, projectId: {}", request.getProjectId());

        // 创建工作簿
        workbook = new XSSFWorkbook();

        try {
            // 获取项目信息
            Project project = projectMapper.selectById(request.getProjectId());
            if (project == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
            }

            // 获取评估信息
            Evaluation evaluation = getEvaluation(request.getProjectId());
            StatisticsResultVO statistics = statisticsService.getStatistics(request.getProjectId());

            // 创建Sheet
            XSSFSheet projectInfoSheet = workbook.createSheet("项目基本信息");
            XSSFSheet evalSummarySheet = workbook.createSheet("评估指标汇总");
            XSSFSheet workloadDetailSheet = workbook.createSheet("工作量明细");
            XSSFSheet riskSheet = workbook.createSheet("风险提示");

            // 设置Sheet样式
            setSheetColumnWidth(projectInfoSheet);
            setSheetColumnWidth(evalSummarySheet);
            setSheetColumnWidth(workloadDetailSheet);
            setSheetColumnWidth(riskSheet);

            // 生成各Sheet内容
            if (request.getSections() == null || request.getSections().getProjectInfo()) {
                createProjectInfoSheet(projectInfoSheet, project, evaluation);
            }

            if (request.getSections() == null || request.getSections().getEvaluationSummary()) {
                createEvalSummarySheet(evalSummarySheet, statistics);
            }

            if (request.getSections() == null || request.getSections().getWorkloadDetail()) {
                createWorkloadDetailSheet(workloadDetailSheet, request.getProjectId(), evaluation);
            }

            if (request.getSections() == null || request.getSections().getRiskWarnings()) {
                createRiskSheet(riskSheet, statistics);
            }

            // 写入字节数组
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            log.info("Excel报告导出完成, projectId: {}", request.getProjectId());

            return out.toByteArray();

        } catch (IOException e) {
            log.error("Excel导出失败", e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "Excel导出失败");
        } finally {
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                log.error("关闭Workbook失败", e);
            }
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
     * 设置Sheet列宽
     */
    private void setSheetColumnWidth(XSSFSheet sheet) {
        sheet.setColumnWidth(0, 20 * 256);
        sheet.setColumnWidth(1, 30 * 256);
        sheet.setColumnWidth(2, 20 * 256);
        sheet.setColumnWidth(3, 20 * 256);
        sheet.setColumnWidth(4, 20 * 256);
    }

    /**
     * 创建项目基本信息Sheet
     */
    private void createProjectInfoSheet(XSSFSheet sheet, Project project, Evaluation evaluation) {
        int rowNum = 0;

        // 标题
        XSSFRow titleRow = sheet.createRow(rowNum++);
        XSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("项目基本信息");
        titleCell.setCellStyle(createTitleStyle(sheet));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        // 基本信息
        createLabelValueRow(sheet, rowNum++, "项目名称", project.getProjectName());
        createLabelValueRow(sheet, rowNum++, "客户名称", project.getCustomerName());
        createLabelValueRow(sheet, rowNum++, "项目负责人", project.getProjectLeader());
        createLabelValueRow(sheet, rowNum++, "联系方式", project.getContact());
        createLabelValueRow(sheet, rowNum++, "评估日期",
            project.getEvaluationDate() != null ? project.getEvaluationDate().toString() : "-");

        // 系统信息
        rowNum++;
        XSSFRow systemTitleRow = sheet.createRow(rowNum++);
        XSSFCell systemTitleCell = systemTitleRow.createCell(0);
        systemTitleCell.setCellValue("系统升迁信息");
        systemTitleCell.setCellStyle(createSubTitleStyle(sheet));

        DataVolumeLadder dataVolumeLadder = dataVolumeLadderMapper.selectById(evaluation.getDataVolumeLadderId());
        UserCountLadder userCountLadder = userCountLadderMapper.selectById(evaluation.getUserCountLadderId());

        createLabelValueRow(sheet, rowNum++, "源系统", getSystemName(project.getSourceSystemId()));
        createLabelValueRow(sheet, rowNum++, "目标系统", getSystemName(project.getTargetSystemId()));

        // 评估状态
        rowNum++;
        createLabelValueRow(sheet, rowNum++, "评估状态", project != null ? project.getStatus() : "");
        createLabelValueRow(sheet, rowNum++, "总工作量", formatWorkload(evaluation.getTotalWorkload()) + " 人天");
        createLabelValueRow(sheet, rowNum++, "预估工期", formatEstimatedMonths(evaluation.getTotalWorkload()) + " 人月");
    }

    /**
     * 创建评估指标汇总Sheet
     */
    private void createEvalSummarySheet(XSSFSheet sheet, StatisticsResultVO statistics) {
        int rowNum = 0;

        // 标题
        XSSFRow titleRow = sheet.createRow(rowNum++);
        XSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("评估指标汇总");
        titleCell.setCellStyle(createTitleStyle(sheet));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        // 工作量汇总
        rowNum++;
        XSSFRow summaryTitleRow = sheet.createRow(rowNum++);
        XSSFCell summaryTitleCell = summaryTitleRow.createCell(0);
        summaryTitleCell.setCellValue("工作量汇总");
        summaryTitleCell.setCellStyle(createSubTitleStyle(sheet));

        createLabelValueRow(sheet, rowNum++, "总工作量", formatWorkload(statistics.getTotalWorkload()) + " 人天");
        createLabelValueRow(sheet, rowNum++, "预估工期", formatEstimatedMonths(statistics.getTotalWorkload()) + " 人月");

        // 工作量类型分布
        rowNum++;
        XSSFRow typeTitleRow = sheet.createRow(rowNum++);
        XSSFCell typeTitleCell = typeTitleRow.createCell(0);
        typeTitleCell.setCellValue("工作量类型分布");
        typeTitleCell.setCellStyle(createSubTitleStyle(sheet));

        // 表头
        XSSFRow typeHeaderRow = sheet.createRow(rowNum++);
        createHeaderCell(typeHeaderRow, 0, "类型");
        createHeaderCell(typeHeaderRow, 1, "工作量（人天）");
        createHeaderCell(typeHeaderRow, 2, "占比");

        // 数据行
        if (statistics.getWorkloadTypeDistribution() != null) {
            for (StatisticsResultVO.WorkloadTypeDistribution type : statistics.getWorkloadTypeDistribution()) {
                XSSFRow dataRow = sheet.createRow(rowNum++);
                createTextCell(dataRow, 0, type.getType());
                createNumberCell(dataRow, 1, type.getWorkload());
                createNumberCell(dataRow, 2, type.getPercentage(), 1, "%");
            }
        }

        // 评估概览
        rowNum++;
        XSSFRow overviewTitleRow = sheet.createRow(rowNum++);
        XSSFCell overviewTitleCell = overviewTitleRow.createCell(0);
        overviewTitleCell.setCellValue("评估指标概览");
        overviewTitleCell.setCellStyle(createSubTitleStyle(sheet));

        StatisticsResultVO.EvaluationOverview overview = statistics.getEvaluationOverview();
        if (overview != null) {
            createLabelValueRow(sheet, rowNum++, "已选模块数", String.valueOf(overview.getModuleCount()) + " 个");
            createLabelValueRow(sheet, rowNum++, "数据量", formatBigDecimal(overview.getDataVolume()) + " 万条 (" + nvl(overview.getDataVolumeLadder()) + ")");
            createLabelValueRow(sheet, rowNum++, "用户数", String.valueOf(overview.getUserCount()) + " 人 (" + nvl(overview.getUserCountLadder()) + ")");
            createLabelValueRow(sheet, rowNum++, "报表数量", String.valueOf(overview.getReportCount()) + " 个");
            createLabelValueRow(sheet, rowNum++, "客开情况", overview.getHasCustomDev() != null && overview.getHasCustomDev() ? "有客开" : "无客开");
        }
    }

    /**
     * 创建工作量明细Sheet
     */
    private void createWorkloadDetailSheet(XSSFSheet sheet, Long projectId, Evaluation evaluation) {
        int rowNum = 0;

        // 标题
        XSSFRow titleRow = sheet.createRow(rowNum++);
        XSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("工作量评估明细");
        titleCell.setCellStyle(createTitleStyle(sheet));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        // 表头
        XSSFRow headerRow = sheet.createRow(rowNum++);
        createHeaderCell(headerRow, 0, "序号");
        createHeaderCell(headerRow, 1, "模块名称");
        createHeaderCell(headerRow, 2, "分类");
        createHeaderCell(headerRow, 3, "基础人天");
        createHeaderCell(headerRow, 4, "加权系数");
        createHeaderCell(headerRow, 5, "工作量（人天）");

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

                XSSFRow dataRow = sheet.createRow(rowNum++);

                BigDecimal baseWorkload = module.getBaseWorkload() != null ? module.getBaseWorkload() : BigDecimal.ZERO;
                BigDecimal weight = config.getWeight() != null ? config.getWeight() : (module.getDefaultWeight() != null ? module.getDefaultWeight() : BigDecimal.ONE);

                BigDecimal moduleWorkload = baseWorkload.multiply(weight).multiply(dataVolumeWeight).multiply(userCountWeight).setScale(2, RoundingMode.HALF_UP);
                totalCoreWorkload = totalCoreWorkload.add(moduleWorkload);

                createNumberCell(dataRow, 0, new BigDecimal(index++));
                createTextCell(dataRow, 1, module.getModuleName());
                createTextCell(dataRow, 2, module.getCategory());
                createNumberCell(dataRow, 3, baseWorkload);
                createNumberCell(dataRow, 4, weight);
                createNumberCell(dataRow, 5, moduleWorkload);
            }

            // 合计行
            XSSFRow totalRow = sheet.createRow(rowNum++);
            XSSFCell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("合计");
            totalLabelCell.setCellStyle(createBoldStyle(sheet));
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));

            createNumberCell(totalRow, 5, totalCoreWorkload);
        }

        // 报表工作量和客开工作量
        rowNum++;
        createLabelValueRow(sheet, rowNum++, "报表工作量", formatWorkload(evaluation.getReportWorkload()) + " 人天");
        if (evaluation.getHasCustomDev() != null && evaluation.getHasCustomDev() == 1) {
            createLabelValueRow(sheet, rowNum++, "客开工作量", formatWorkload(evaluation.getCustomDevWorkload()) + " 人天");
        }

        // 总计
        rowNum++;
        XSSFRow grandTotalRow = sheet.createRow(rowNum++);
        XSSFCell grandTotalLabelCell = grandTotalRow.createCell(0);
        grandTotalLabelCell.setCellValue("总工作量");
        grandTotalLabelCell.setCellStyle(createBoldStyle(sheet));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));

        XSSFCell grandTotalValueCell = grandTotalRow.createCell(5);
        grandTotalValueCell.setCellValue(formatWorkload(evaluation.getTotalWorkload()) + " 人天");
        grandTotalValueCell.setCellStyle(createHighlightStyle(sheet));
    }

    /**
     * 创建风险提示Sheet
     */
    private void createRiskSheet(XSSFSheet sheet, StatisticsResultVO statistics) {
        int rowNum = 0;

        // 标题
        XSSFRow titleRow = sheet.createRow(rowNum++);
        XSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("风险提示与建议");
        titleCell.setCellStyle(createTitleStyle(sheet));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        // 表头
        XSSFRow headerRow = sheet.createRow(rowNum++);
        createHeaderCell(headerRow, 0, "风险类型");
        createHeaderCell(headerRow, 1, "风险等级");
        createHeaderCell(headerRow, 2, "风险描述");
        createHeaderCell(headerRow, 3, "建议");

        // 数据行
        if (statistics.getRiskWarnings() != null && !statistics.getRiskWarnings().isEmpty()) {
            for (StatisticsResultVO.RiskWarning warning : statistics.getRiskWarnings()) {
                XSSFRow dataRow = sheet.createRow(rowNum++);
                createTextCell(dataRow, 0, getRiskTypeName(warning.getType()));
                createTextCell(dataRow, 1, warning.getLevel());
                createTextCell(dataRow, 2, warning.getDescription());
                createTextCell(dataRow, 3, warning.getSuggestion());
            }
        } else {
            XSSFRow emptyRow = sheet.createRow(rowNum++);
            XSSFCell emptyCell = emptyRow.createCell(0);
            emptyCell.setCellValue("暂无风险提示");
        }
    }

    // ========== 辅助方法 ==========

    private void createLabelValueRow(XSSFSheet sheet, int rowNum, String label, String value) {
        XSSFRow row = sheet.createRow(rowNum);
        XSSFCell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(createLabelStyle(sheet));

        XSSFCell valueCell = row.createCell(1);
        valueCell.setCellValue(value != null ? value : "-");
        valueCell.setCellStyle(createValueStyle(sheet));
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, 4));
    }

    private void createHeaderCell(XSSFRow row, int column, String value) {
        XSSFCell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(createHeaderStyle(workbook));
    }

    private void createTextCell(XSSFRow row, int column, String value) {
        XSSFCell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "-");
        cell.setCellStyle(createCellStyle(workbook));
    }

    private void createNumberCell(XSSFRow row, int column, BigDecimal value) {
        XSSFCell cell = row.createCell(column);
        cell.setCellValue(value != null ? value.doubleValue() : 0);
        cell.setCellStyle(createNumberStyle(workbook));
    }

    private void createNumberCell(XSSFRow row, int column, BigDecimal value, int scale, String suffix) {
        XSSFCell cell = row.createCell(column);
        double val = value != null ? value.setScale(scale, RoundingMode.HALF_UP).doubleValue() : 0;
        cell.setCellValue(val + suffix);
        cell.setCellStyle(createCellStyle(workbook));
    }

    private XSSFCellStyle createTitleStyle(XSSFSheet sheet) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle createSubTitleStyle(XSSFSheet sheet) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle createHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle createLabelStyle(XSSFSheet sheet) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle createValueStyle(XSSFSheet sheet) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle createCellStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle createNumberStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private XSSFCellStyle createBoldStyle(XSSFSheet sheet) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle createHighlightStyle(XSSFSheet sheet) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
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

    private String getSystemName(Long systemId) {
        if (systemId == null) return "-";
        // 简单处理，实际应查询system_type表
        return "系统" + systemId;
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
