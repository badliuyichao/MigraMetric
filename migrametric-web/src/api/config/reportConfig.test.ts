import { describe, it, expect, vi, beforeEach } from 'vitest'
import { request } from '@/utils/request'
import {
  listAllReportConfigs,
  getReportConfigValue,
  getReportConfigValueAsNumber,
  updateReportConfig,
  type ReportConfigVO,
  type ReportConfigUpdate
} from './reportConfig'

vi.mock('@/utils/request')

const mockRequest = request as ReturnType<typeof vi.fn>

describe('报表配置 API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('listAllReportConfigs', () => {
    it('应返回所有报表配置列表', async () => {
      const mockConfigs: ReportConfigVO[] = [
        {
          id: 1,
          configKey: 'REPORT_MIGRATION_COEFFICIENT',
          configValue: '2.5',
          configName: '报表迁移系数',
          description: '每个报表迁移所需的工作量（人天/个）',
          status: 1,
          statusText: '启用',
          createTime: '2024-01-01 10:00:00',
          createBy: 'admin'
        }
      ]
      mockRequest.get.mockResolvedValue({ data: mockConfigs })

      const result = await listAllReportConfigs()

      expect(result).toEqual(mockConfigs)
      expect(mockRequest.get).toHaveBeenCalledWith('/config/report')
    })
  })

  describe('getReportConfigValue', () => {
    it('应根据配置键获取配置值', async () => {
      mockRequest.get.mockResolvedValue({ data: '2.5' })

      const result = await getReportConfigValue('REPORT_MIGRATION_COEFFICIENT')

      expect(result).toBe('2.5')
      expect(mockRequest.get).toHaveBeenCalledWith('/config/report/value', {
        params: { configKey: 'REPORT_MIGRATION_COEFFICIENT' }
      })
    })
  })

  describe('getReportConfigValueAsNumber', () => {
    it('应返回数值类型的配置值', async () => {
      mockRequest.get.mockResolvedValue({ data: 2.5 })

      const result = await getReportConfigValueAsNumber('REPORT_MIGRATION_COEFFICIENT')

      expect(result).toBe(2.5)
      expect(mockRequest.get).toHaveBeenCalledWith('/config/report/value/number', {
        params: { configKey: 'REPORT_MIGRATION_COEFFICIENT', defaultValue: undefined }
      })
    })

    it('应支持默认值参数', async () => {
      mockRequest.get.mockResolvedValue({ data: 1.0 })

      const result = await getReportConfigValueAsNumber('NON_EXISTENT', 1.0)

      expect(result).toBe(1.0)
      expect(mockRequest.get).toHaveBeenCalledWith('/config/report/value/number', {
        params: { configKey: 'NON_EXISTENT', defaultValue: 1.0 }
      })
    })
  })

  describe('updateReportConfig', () => {
    it('应成功更新配置', async () => {
      const updateData: ReportConfigUpdate = {
        id: 1,
        configKey: 'REPORT_MIGRATION_COEFFICIENT',
        configValue: '3.0'
      }
      mockRequest.put.mockResolvedValue({ data: undefined })

      await expect(updateReportConfig(updateData)).resolves.toBeUndefined()
      expect(mockRequest.put).toHaveBeenCalledWith('/config/report', updateData)
    })
  })
})
