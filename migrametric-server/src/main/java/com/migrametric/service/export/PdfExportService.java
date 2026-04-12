package com.migrametric.service.export;

import com.migrametric.dto.export.ExportRequestDTO;

/**
 * PDF导出服务接口
 *
 * @author MigraMetric Team
 */
public interface PdfExportService {

    /**
     * 导出项目评估报告为PDF
     *
     * @param request 导出请求
     * @return PDF文件字节数组
     */
    byte[] exportToPdf(ExportRequestDTO request);
}
