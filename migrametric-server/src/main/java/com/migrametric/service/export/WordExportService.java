package com.migrametric.service.export;

import com.migrametric.dto.export.ExportRequestDTO;

/**
 * Word导出服务接口
 *
 * @author MigraMetric Team
 */
public interface WordExportService {

    /**
     * 导出项目评估报告为Word
     *
     * @param request 导出请求
     * @return Word文件字节数组
     */
    byte[] exportToWord(ExportRequestDTO request);
}
