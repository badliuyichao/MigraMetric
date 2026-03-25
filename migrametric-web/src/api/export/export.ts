/**
 * 导出相关 API
 */
import { request } from '@/utils/request'

/**
 * 导出格式
 */
export type ExportFormat = 'EXCEL' | 'PDF' | 'WORD'

/**
 * 导出内容章节
 */
export interface ExportSection {
  projectInfo?: boolean
  evaluationSummary?: boolean
  workloadDetail?: boolean
  charts?: boolean
  riskWarnings?: boolean
}

/**
 * 导出请求参数
 */
export interface ExportParams {
  projectId: number
  format: ExportFormat
  sections?: ExportSection
}

// ========== 导出相关API ==========

/**
 * 导出评估报告为Excel
 */
export function exportToExcel(projectId: number, sections?: ExportSection) {
  const params: ExportParams = {
    projectId,
    format: 'EXCEL',
    sections
  }
  return request.post<Blob>(`/api/export/excel/${projectId}`, params, {
    responseType: 'blob'
  })
}

/**
 * 导出评估报告为PDF
 */
export function exportToPdf(projectId: number, sections?: ExportSection) {
  const params: ExportParams = {
    projectId,
    format: 'PDF',
    sections
  }
  return request.post<Blob>(`/api/export/pdf/${projectId}`, params, {
    responseType: 'blob'
  })
}

/**
 * 导出评估报告为Word
 */
export function exportToWord(projectId: number, sections?: ExportSection) {
  const params: ExportParams = {
    projectId,
    format: 'WORD',
    sections
  }
  return request.post<Blob>(`/api/export/word/${projectId}`, params, {
    responseType: 'blob'
  })
}

/**
 * 下载文件
 */
export function downloadFile(data: Blob, fileName: string) {
  const url = window.URL.createObjectURL(data)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}
