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
import com.migrametric.service.export.WordExportService;
import com.migrametric.service.statistics.StatisticsService;
import com.migrametric.vo.statistics.StatisticsResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

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
 * Word导出服务实现类
 *
 * @author MigraMetric Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WordExportServiceImpl implements WordExportService {

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

    @Override
    public byte[] exportToWord(ExportRequestDTO request) {
        log.info("开始导出Word报告, projectId: {}", request.getProjectId());

        try (XWPFDocument document = new XWPFDocument()) {
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

            // 添加页脚
            addFooter(document);

            // 写入字节数组
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);

            log.info("Word报告导出完成, projectId: {}", request.getProjectId());
            return out.toByteArray();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Word导出失败", e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "Word导出失败");
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
    private void addProjectInfoSection(XWPFDocument document, Project project, Evaluation evaluation) {
        // 标题
        addHeading1(document, "一、项目基本信息");

        // 创建表格
        XWPFTable table = document.createTable(10, 2);
        table.setWidth("100%");

        int rowIndex = 0;
        setLabelValueRow(table, rowIndex++, "项目名称", nvl(project.getProjectName()));
        setLabelValueRow(table, rowIndex++, "客户名称", nvl(project.getCustomerName()));
        setLabelValueRow(table, rowIndex++, "项目负责人", nvl(project.getProjectLeader()));
        setLabelValueRow(table, rowIndex++, "联系方式", nvl(project.getContact()));
        setLabelValueRow(table, rowIndex++, "评估日期",
            project.getEvaluationDate() != null ? project.getEvaluationDate().toString() : "-");
        setLabelValueRow(table, rowIndex++, "源系统", "系统" + nvl(project.getSourceSystemId()));
        setLabelValueRow(table, rowIndex++, "目标系统", "系统" + nvl(project.getTargetSystemId()));
        setLabelValueRow(table, rowIndex++, "评估状态", nvl(evaluation.getEvaluationStatus()));
        setLabelValueRow(table, rowIndex++, "总工作量", formatWorkload(evaluation.getTotalWorkload()) + " 人天");
        setLabelValueRow(table, rowIndex++, "预估工期", formatEstimatedMonths(evaluation.getTotalWorkload()) + " 人月");

        // 设置表格边框
        setTableBorders(table);

        // 空行
        addEmptyParagraph(document);
    }

    /**
     * 添加评估指标汇总章节
     */
    private void addEvaluationSummarySection(XWPFDocument document, StatisticsResultVO statistics) {
        // 标题
        addHeading1(document, "二、评估指标汇总");

        // 工作量汇总
        addHeading2(document, "2.1 工作量汇总");
        XWPFTable summaryTable = document.createTable(2, 2);
        summaryTable.setWidth("50%");
        setLabelValueRow(summaryTable, 0, "总工作量", formatWorkload(statistics.getTotalWorkload()) + " 人天");
        setLabelValueRow(summaryTable, 1, "预估工期", formatEstimatedMonths(statistics.getTotalWorkload()) + " 人月");
        setTableBorders(summaryTable);
        addEmptyParagraph(document);

        // 工作量类型分布
        addHeading2(document, "2.2 工作量类型分布");
        if (statistics.getWorkloadTypeDistribution() != null && !statistics.getWorkloadTypeDistribution().isEmpty()) {
            XWPFTable typeTable = document.createTable(
                statistics.getWorkloadTypeDistribution().size() + 1, 3);
            typeTable.setWidth("70%");

            // 表头
            setTableHeaderCell(typeTable.getRow(0), 0, "类型");
            setTableHeaderCell(typeTable.getRow(0), 1, "工作量（人天）");
            setTableHeaderCell(typeTable.getRow(0), 2, "占比");

            // 数据行
            int rowIdx = 1;
            for (StatisticsResultVO.WorkloadTypeDistribution type : statistics.getWorkloadTypeDistribution()) {
                setTableCell(typeTable.getRow(rowIdx), 0, type.getType());
                setTableCell(typeTable.getRow(rowIdx), 1, formatWorkload(type.getWorkload()));
                setTableCell(typeTable.getRow(rowIdx), 2, formatPercentage(type.getPercentage()));
                rowIdx++;
            }
            setTableBorders(typeTable);
        }
        addEmptyParagraph(document);

        // 评估概览
        addHeading2(document, "2.3 评估指标概览");
        if (statistics.getEvaluationOverview() != null) {
            StatisticsResultVO.EvaluationOverview overview = statistics.getEvaluationOverview();
            XWPFTable overviewTable = document.createTable(5, 2);
            overviewTable.setWidth("100%");

            int idx = 0;
            setLabelValueRow(overviewTable, idx++, "已选模块数", overview.getModuleCount() + " 个");
            setLabelValueRow(overviewTable, idx++, "数据量", formatBigDecimal(overview.getDataVolume()) + " 万条");
            setLabelValueRow(overviewTable, idx++, "用户数", overview.getUserCount() + " 人");
            setLabelValueRow(overviewTable, idx++, "报表数量", overview.getReportCount() + " 个");
            setLabelValueRow(overviewTable, idx++, "客开情况",
                (overview.getHasCustomDev() != null && overview.getHasCustomDev()) ? "有客开" : "无客开");
            setTableBorders(overviewTable);
        }
        addEmptyParagraph(document);
    }

    /**
     * 添加工作量明细章节
     */
    private void addWorkloadDetailSection(XWPFDocument document, Long projectId, Evaluation evaluation) {
        // 标题
        addHeading1(document, "三、工作量评估明细");

        // 模块明细表
        addHeading2(document, "3.1 模块工作量明细");

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

            XWPFTable moduleTable = document.createTable(configs.size() + 2, 6);
            moduleTable.setWidth("100%");

            // 表头
            setTableHeaderCell(moduleTable.getRow(0), 0, "序号");
            setTableHeaderCell(moduleTable.getRow(0), 1, "模块名称");
            setTableHeaderCell(moduleTable.getRow(0), 2, "分类");
            setTableHeaderCell(moduleTable.getRow(0), 3, "基础人天");
            setTableHeaderCell(moduleTable.getRow(0), 4, "加权系数");
            setTableHeaderCell(moduleTable.getRow(0), 5, "工作量");

            int index = 1;
            BigDecimal totalCoreWorkload = BigDecimal.ZERO;

            for (ProjectModuleConfig config : configs) {
                Module module = moduleMap.get(config.getModuleId());
                if (module == null) continue;

                XWPFTableRow row = moduleTable.getRow(index);

                BigDecimal baseWorkload = module.getBaseWorkload() != null ? module.getBaseWorkload() : BigDecimal.ZERO;
                BigDecimal weight = config.getWeight() != null ? config.getWeight() :
                        (module.getDefaultWeight() != null ? module.getDefaultWeight() : BigDecimal.ONE);

                BigDecimal moduleWorkload = baseWorkload.multiply(weight).multiply(dataVolumeWeight).multiply(userCountWeight).setScale(2, RoundingMode.HALF_UP);
                totalCoreWorkload = totalCoreWorkload.add(moduleWorkload);

                setTableCell(row, 0, String.valueOf(index++));
                setTableCell(row, 1, nvl(module.getModuleName()));
                setTableCell(row, 2, nvl(module.getCategory()));
                setTableCell(row, 3, formatWorkload(baseWorkload));
                setTableCell(row, 4, formatWorkload(weight));
                setTableCell(row, 5, formatWorkload(moduleWorkload));
            }

            // 合计行
            XWPFTableRow totalRow = moduleTable.getRow(index);
            setTableCell(totalRow, 0, "合计");
            totalRow.getCell(0).setText("合计");
            setTableCell(totalRow, 5, formatWorkload(totalCoreWorkload));

            // 合并单元格
            totalRow.addNewTableCell();
            totalRow.getCell(0).setWidth("1500");

            setTableBorders(moduleTable);
        }
        addEmptyParagraph(document);

        // 报表工作量和客开工作量
        addHeading2(document, "3.2 其他工作量");
        XWPFTable otherTable = document.createTable(2, 2);
        otherTable.setWidth("50%");
        setLabelValueRow(otherTable, 0, "报表工作量", formatWorkload(evaluation.getReportWorkload()) + " 人天");
        if (evaluation.getHasCustomDev() != null && evaluation.getHasCustomDev() == 1) {
            setLabelValueRow(otherTable, 1, "客开工作量", formatWorkload(evaluation.getCustomDevWorkload()) + " 人天");
        } else {
            // 清空第二行
            otherTable.getRow(1).getCell(0).setText("");
            otherTable.getRow(1).getCell(1).setText("");
        }
        setTableBorders(otherTable);
        addEmptyParagraph(document);

        // 总计
        addHeading2(document, "3.3 总计");
        addParagraph(document, "总工作量：" + formatWorkload(evaluation.getTotalWorkload()) + " 人天", true, 14);
        addEmptyParagraph(document);
    }

    /**
     * 添加风险提示章节
     */
    private void addRiskWarningSection(XWPFDocument document, StatisticsResultVO statistics) {
        // 标题
        addHeading1(document, "四、风险提示与建议");

        if (statistics.getRiskWarnings() != null && !statistics.getRiskWarnings().isEmpty()) {
            XWPFTable riskTable = document.createTable(statistics.getRiskWarnings().size() + 1, 4);
            riskTable.setWidth("100%");

            // 表头
            setTableHeaderCell(riskTable.getRow(0), 0, "风险类型");
            setTableHeaderCell(riskTable.getRow(0), 1, "风险等级");
            setTableHeaderCell(riskTable.getRow(0), 2, "风险描述");
            setTableHeaderCell(riskTable.getRow(0), 3, "建议");

            // 数据行
            int rowIdx = 1;
            for (StatisticsResultVO.RiskWarning warning : statistics.getRiskWarnings()) {
                XWPFTableRow row = riskTable.getRow(rowIdx++);
                setTableCell(row, 0, getRiskTypeName(warning.getType()));
                setTableCell(row, 1, nvl(warning.getLevel()));
                setTableCell(row, 2, nvl(warning.getDescription()));
                setTableCell(row, 3, nvl(warning.getSuggestion()));
            }
            setTableBorders(riskTable);
        } else {
            addParagraph(document, "暂无风险提示", false, 12);
        }
    }

    /**
     * 添加页脚
     */
    private void addFooter(XWPFDocument document) {
        addEmptyParagraph(document);
        XWPFParagraph footerPara = document.createParagraph();
        footerPara.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun footerRun = footerPara.createRun();
        footerRun.setText("生成日期：" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        footerRun.setFontSize(10);
        footerRun.setColor("808080");
    }

    // ========== Word辅助方法 ==========

    private void addHeading1(XWPFDocument document, String text) {
        XWPFParagraph para = document.createParagraph();
        para.setSpacingBefore(400);
        para.setSpacingAfter(200);
        XWPFRun run = para.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontSize(18);
        run.setColor("333333");
    }

    private void addHeading2(XWPFDocument document, String text) {
        XWPFParagraph para = document.createParagraph();
        para.setSpacingBefore(200);
        para.setSpacingAfter(100);
        XWPFRun run = para.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontSize(14);
        run.setColor("333333");
    }

    private void addParagraph(XWPFDocument document, String text, boolean bold, int fontSize) {
        XWPFParagraph para = document.createParagraph();
        XWPFRun run = para.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontSize(fontSize);
    }

    private void addEmptyParagraph(XWPFDocument document) {
        XWPFParagraph para = document.createParagraph();
        para.setSpacingAfter(100);
    }

    private void setLabelValueRow(XWPFTable table, int rowIndex, String label, String value) {
        XWPFTableRow row = table.getRow(rowIndex);
        if (row == null) {
            row = table.createRow();
        }
        XWPFTableCell labelCell = row.getCell(0);
        if (labelCell == null) {
            labelCell = row.createCell();
        }
        labelCell.setText(label);
        labelCell.setWidth("3000");

        XWPFTableCell valueCell = row.getCell(1);
        if (valueCell == null) {
            valueCell = row.createCell();
        }
        valueCell.setText(value != null ? value : "-");
        valueCell.setWidth("7000");
    }

    private void setTableHeaderCell(XWPFTableRow row, int colIndex, String text) {
        XWPFTableCell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell();
        }
        cell.setText(text);

        // 设置表头样式
        XWPFParagraph para = cell.getParagraphs().get(0);
        para.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun run = para.getRuns().get(0);
        run.setBold(true);
        run.setFontSize(11);
        run.setColor("FFFFFF");

        // 设置背景色
        cell.setColor("2957A8");
    }

    private void setTableCell(XWPFTableRow row, int colIndex, String text) {
        XWPFTableCell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell();
        }
        cell.setText(text != null ? text : "-");

        // 居中对齐
        XWPFParagraph para = cell.getParagraphs().get(0);
        para.setAlignment(ParagraphAlignment.CENTER);
    }

    private void setTableBorders(XWPFTable table) {
        // 设置表格边框
        table.setLeftBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
        table.setRightBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
        table.setTopBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
        table.setBottomBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
        table.setInsideHBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
        table.setInsideVBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
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

    private String nvl(Long value) {
        return value != null ? String.valueOf(value) : "-";
    }

    private String nvl(String value) {
        return value != null ? value : "-";
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
