/**
 * 导出API测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { request } from '@/utils/request'

// Mock request
vi.mock('@/utils/request', () => ({
  request: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn()
  }
}))

// 导入待测试的函数
import {
  exportToExcel,
  exportToPdf,
  exportToWord,
  downloadFile
} from './export'

describe('导出API测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('exportToExcel', () => {
    it('EXPORT-API-001: 应该调用Excel导出接口', async () => {
      const mockBlob = new Blob(['test'], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      ;(request.post as any).mockResolvedValue(mockBlob)

      const result = await exportToExcel(1)

      expect(request.post).toHaveBeenCalledWith(
        '/api/export/excel/1',
        expect.objectContaining({
          projectId: 1,
          format: 'EXCEL'
        }),
        { responseType: 'blob' }
      )
    })

    it('EXPORT-API-002: 应该支持导出章节参数', async () => {
      const mockBlob = new Blob(['test'], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      ;(request.post as any).mockResolvedValue(mockBlob)

      const sections = {
        projectInfo: true,
        evaluationSummary: true,
        workloadDetail: false,
        charts: false,
        riskWarnings: false
      }

      await exportToExcel(1, sections)

      expect(request.post).toHaveBeenCalledWith(
        '/api/export/excel/1',
        expect.objectContaining({
          projectId: 1,
          format: 'EXCEL',
          sections
        }),
        { responseType: 'blob' }
      )
    })
  })

  describe('exportToPdf', () => {
    it('EXPORT-API-003: 应该调用PDF导出接口', async () => {
      const mockBlob = new Blob(['test'], { type: 'application/pdf' })
      ;(request.post as any).mockResolvedValue(mockBlob)

      await exportToPdf(1)

      expect(request.post).toHaveBeenCalledWith(
        '/api/export/pdf/1',
        expect.objectContaining({
          projectId: 1,
          format: 'PDF'
        }),
        { responseType: 'blob' }
      )
    })
  })

  describe('exportToWord', () => {
    it('EXPORT-API-004: 应该调用Word导出接口', async () => {
      const mockBlob = new Blob(['test'], { type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' })
      ;(request.post as any).mockResolvedValue(mockBlob)

      await exportToWord(1)

      expect(request.post).toHaveBeenCalledWith(
        '/api/export/word/1',
        expect.objectContaining({
          projectId: 1,
          format: 'WORD'
        }),
        { responseType: 'blob' }
      )
    })
  })

  describe('downloadFile', () => {
    it('EXPORT-API-005: 应该触发文件下载', () => {
      const blob = new Blob(['test content'], { type: 'application/octet-stream' })
      const fileName = 'test.xlsx'

      // Mock createElement, click, removeChild
      const mockLink = {
        href: '',
        download: '',
        click: vi.fn(),
        remove: vi.fn()
      }
      vi.stubGlobal('URL', {
        createObjectURL: vi.fn(() => 'blob:test'),
        revokeObjectURL: vi.fn()
      })
      vi.stubGlobal('document', {
        createElement: vi.fn(() => mockLink),
        body: {
          appendChild: vi.fn(),
          removeChild: vi.fn()
        }
      })

      downloadFile(blob, fileName)

      expect(mockLink.download).toBe(fileName)
      expect(mockLink.click).toHaveBeenCalled()
    })
  })
})
