package com.migrametric.service.export;

import com.migrametric.dto.export.ExportRequestDTO;

/**
 * Excel导出服务接口
 *
 * @author MigraMetric Team
 */
public interface ExcelExportService {

    /**
     * 导出项目评估报告为Excel
     *
     * @param request 导出请求
     * @return Excel文件字节数组
     */
    byte[] exportToExcel(ExportRequestDTO request);
}
