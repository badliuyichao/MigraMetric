package com.migrametric.controller.export;

import com.migrametric.common.Result;
import com.migrametric.dto.export.ExportRequestDTO;
import com.migrametric.entity.project.Project;
import com.migrametric.mapper.project.ProjectMapper;
import com.migrametric.service.export.ExcelExportService;
import com.migrametric.service.export.PdfExportService;
import com.migrametric.service.export.WordExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 导出Controller
 *
 * @author MigraMetric Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/export")
@Tag(name = "报告导出", description = "Excel/PDF/Word导出相关接口")
public class ExportController {

    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;
    private final WordExportService wordExportService;
    private final ProjectMapper projectMapper;

    /**
     * 导出评估报告为Excel
     *
     * @param projectId 项目ID
     * @param sections  导出章节（可选）
     * @return Excel文件字节数组
     */
    @PostMapping("/excel/{projectId}")
    @Operation(summary = "导出评估报告为Excel", description = "导出项目评估报告为Excel格式")
    public Result<byte[]> exportExcel(
            @Parameter(description = "项目ID", required = true)
            @PathVariable Long projectId,
            @Parameter(description = "导出章节")
            @RequestBody(required = false) ExportRequestDTO.ExportSection sections) {

        log.info("导出Excel报告, projectId: {}", projectId);

        // 获取项目信息用于文件名
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            return Result.fail(404, "项目不存在");
        }

        // 构建请求
        ExportRequestDTO request = new ExportRequestDTO();
        request.setProjectId(projectId);
        request.setFormat("EXCEL");
        request.setSections(sections);

        // 导出Excel
        byte[] excelData = excelExportService.exportToExcel(request);

        // 生成文件名
        String fileName = generateFileName(project, "xlsx");

        log.info("Excel报告导出成功, projectId: {}, fileName: {}", projectId, fileName);

        return Result.success(excelData);
    }

    /**
     * 导出评估报告为PDF
     *
     * @param projectId 项目ID
     * @param sections  导出章节（可选）
     * @return PDF文件字节数组
     */
    @PostMapping("/pdf/{projectId}")
    @Operation(summary = "导出评估报告为PDF", description = "导出项目评估报告为PDF格式")
    public Result<byte[]> exportPdf(
            @Parameter(description = "项目ID", required = true)
            @PathVariable Long projectId,
            @Parameter(description = "导出章节")
            @RequestBody(required = false) ExportRequestDTO.ExportSection sections) {

        log.info("导出PDF报告, projectId: {}", projectId);

        // 获取项目信息用于文件名
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            return Result.fail(404, "项目不存在");
        }

        // 构建请求
        ExportRequestDTO request = new ExportRequestDTO();
        request.setProjectId(projectId);
        request.setFormat("PDF");
        request.setSections(sections);

        // 导出PDF
        byte[] pdfData = pdfExportService.exportToPdf(request);

        log.info("PDF报告导出成功, projectId: {}", projectId);

        return Result.success(pdfData);
    }

    /**
     * 导出评估报告为Word
     *
     * @param projectId 项目ID
     * @param sections  导出章节（可选）
     * @return Word文件字节数组
     */
    @PostMapping("/word/{projectId}")
    @Operation(summary = "导出评估报告为Word", description = "导出项目评估报告为Word格式")
    public Result<byte[]> exportWord(
            @Parameter(description = "项目ID", required = true)
            @PathVariable Long projectId,
            @Parameter(description = "导出章节")
            @RequestBody(required = false) ExportRequestDTO.ExportSection sections) {

        log.info("导出Word报告, projectId: {}", projectId);

        // 获取项目信息用于文件名
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            return Result.fail(404, "项目不存在");
        }

        // 构建请求
        ExportRequestDTO request = new ExportRequestDTO();
        request.setProjectId(projectId);
        request.setFormat("WORD");
        request.setSections(sections);

        // 导出Word
        byte[] wordData = wordExportService.exportToWord(request);

        log.info("Word报告导出成功, projectId: {}", projectId);

        return Result.success(wordData);
    }

    /**
     * 生成文件名
     */
    private String generateFileName(Project project, String extension) {
        String customerName = project.getCustomerName() != null ? project.getCustomerName() : "客户";
        String projectName = project.getProjectName() != null ? project.getProjectName() : "项目";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 清理特殊字符
        customerName = customerName.replaceAll("[\\\\/:*?\"<>|]", "_");
        projectName = projectName.replaceAll("[\\\\/:*?\"<>|]", "_");

        return customerName + "_" + projectName + "_工作量评估报告_" + date + "." + extension;
    }
}
