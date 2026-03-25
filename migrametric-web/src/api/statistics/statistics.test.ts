/**
 * 统计API测试
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
import { getStatistics } from './statistics'

describe('统计API测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getStatistics', () => {
    it('STAT-API-001: 应该获取统计数据', async () => {
      const mockStatistics = {
        projectId: 1,
        totalWorkload: 156.38,
        estimatedMonths: 7.11,
        workloadTypeDistribution: [
          { type: '核心迁移', workload: 111.38, percentage: 71.3 },
          { type: '报表迁移', workload: 25.00, percentage: 16.0 },
          { type: '客开定制', workload: 20.00, percentage: 12.7 }
        ],
        moduleWorkloads: [
          { moduleName: '财务管理', workload: 43.88, percentage: 39.4 },
          { moduleName: '供应链管理', workload: 67.50, percentage: 60.6 }
        ],
        multiDimensionIndicators: {
          dataVolumeValue: 50,
          dataVolumeActual: '500万条',
          dataVolumeLadder: '中型',
          userCountValue: 60,
          userCountActual: '600人',
          userCountLadder: '中规模',
          moduleCountValue: 10,
          moduleCountActual: 2,
          reportCountValue: 50,
          reportCountActual: 50,
          customDevValue: 80,
          hasCustomDev: true,
          customDevWorkload: 20
        },
        riskWarnings: [
          { type: 'CUSTOM_DEV', level: '高', description: '客开风险', suggestion: '建议...' }
        ],
        evaluationOverview: {
          moduleCount: 2,
          dataVolume: 500,
          dataVolumeLadder: '中型',
          userCount: 600,
          userCountLadder: '中规模',
          reportCount: 50,
          hasCustomDev: true
        }
      }
      ;(request.get as any).mockResolvedValue({ data: mockStatistics })

      const result = await getStatistics(1)

      expect(request.get).toHaveBeenCalledWith('/api/statistics/1')
      expect(result.projectId).toBe(1)
      expect(result.totalWorkload).toBe(156.38)
      expect(result.workloadTypeDistribution).toHaveLength(3)
    })

    it('STAT-API-002: 工作量类型分布正确', async () => {
      const mockStatistics = {
        projectId: 1,
        totalWorkload: 156.38,
        estimatedMonths: 7.11,
        workloadTypeDistribution: [
          { type: '核心迁移', workload: 111.38, percentage: 71.3 },
          { type: '报表迁移', workload: 25.00, percentage: 16.0 },
          { type: '客开定制', workload: 20.00, percentage: 12.7 }
        ],
        moduleWorkloads: [],
        multiDimensionIndicators: {
          dataVolumeValue: 50,
          dataVolumeActual: '500万条',
          dataVolumeLadder: '中型',
          userCountValue: 60,
          userCountActual: '600人',
          userCountLadder: '中规模',
          moduleCountValue: 10,
          moduleCountActual: 2,
          reportCountValue: 50,
          reportCountActual: 50,
          customDevValue: 80,
          hasCustomDev: true,
          customDevWorkload: 20
        },
        riskWarnings: [],
        evaluationOverview: {
          moduleCount: 2,
          dataVolume: 500,
          dataVolumeLadder: '中型',
          userCount: 600,
          userCountLadder: '中规模',
          reportCount: 50,
          hasCustomDev: true
        }
      }
      ;(request.get as any).mockResolvedValue({ data: mockStatistics })

      const result = await getStatistics(1)

      expect(result.workloadTypeDistribution[0].type).toBe('核心迁移')
      expect(result.workloadTypeDistribution[0].workload).toBe(111.38)
      expect(result.workloadTypeDistribution[0].percentage).toBe(71.3)
    })

    it('STAT-API-003: 模块工作量对比正确', async () => {
      const mockStatistics = {
        projectId: 1,
        totalWorkload: 156.38,
        estimatedMonths: 7.11,
        workloadTypeDistribution: [],
        moduleWorkloads: [
          { moduleName: '供应链管理', workload: 67.50, percentage: 60.6 },
          { moduleName: '财务管理', workload: 43.88, percentage: 39.4 }
        ],
        multiDimensionIndicators: {
          dataVolumeValue: 50,
          dataVolumeActual: '500万条',
          dataVolumeLadder: '中型',
          userCountValue: 60,
          userCountActual: '600人',
          userCountLadder: '中规模',
          moduleCountValue: 10,
          moduleCountActual: 2,
          reportCountValue: 50,
          reportCountActual: 50,
          customDevValue: 80,
          hasCustomDev: true,
          customDevWorkload: 20
        },
        riskWarnings: [],
        evaluationOverview: {
          moduleCount: 2,
          dataVolume: 500,
          dataVolumeLadder: '中型',
          userCount: 600,
          userCountLadder: '中规模',
          reportCount: 50,
          hasCustomDev: true
        }
      }
      ;(request.get as any).mockResolvedValue({ data: mockStatistics })

      const result = await getStatistics(1)

      expect(result.moduleWorkloads).toHaveLength(2)
      expect(result.moduleWorkloads[0].moduleName).toBe('供应链管理')
      expect(result.moduleWorkloads[0].workload).toBe(67.50)
    })

    it('STAT-API-004: 多维度指标正确', async () => {
      const mockStatistics = {
        projectId: 1,
        totalWorkload: 156.38,
        estimatedMonths: 7.11,
        workloadTypeDistribution: [],
        moduleWorkloads: [],
        multiDimensionIndicators: {
          dataVolumeValue: 50,
          dataVolumeActual: '500万条',
          dataVolumeLadder: '中型',
          userCountValue: 60,
          userCountActual: '600人',
          userCountLadder: '中规模',
          moduleCountValue: 10,
          moduleCountActual: 2,
          reportCountValue: 50,
          reportCountActual: 50,
          customDevValue: 80,
          hasCustomDev: true,
          customDevWorkload: 20
        },
        riskWarnings: [],
        evaluationOverview: {
          moduleCount: 2,
          dataVolume: 500,
          dataVolumeLadder: '中型',
          userCount: 600,
          userCountLadder: '中规模',
          reportCount: 50,
          hasCustomDev: true
        }
      }
      ;(request.get as any).mockResolvedValue({ data: mockStatistics })

      const result = await getStatistics(1)

      expect(result.multiDimensionIndicators.dataVolumeValue).toBe(50)
      expect(result.multiDimensionIndicators.dataVolumeActual).toBe('500万条')
      expect(result.multiDimensionIndicators.userCountValue).toBe(60)
      expect(result.multiDimensionIndicators.moduleCountActual).toBe(2)
      expect(result.multiDimensionIndicators.hasCustomDev).toBe(true)
    })

    it('STAT-API-005: 风险提示正确', async () => {
      const mockStatistics = {
        projectId: 1,
        totalWorkload: 156.38,
        estimatedMonths: 7.11,
        workloadTypeDistribution: [],
        moduleWorkloads: [],
        multiDimensionIndicators: {
          dataVolumeValue: 50,
          dataVolumeActual: '500万条',
          dataVolumeLadder: '中型',
          userCountValue: 60,
          userCountActual: '600人',
          userCountLadder: '中规模',
          moduleCountValue: 10,
          moduleCountActual: 2,
          reportCountValue: 50,
          reportCountActual: 50,
          customDevValue: 80,
          hasCustomDev: true,
          customDevWorkload: 20
        },
        riskWarnings: [
          { type: 'CUSTOM_DEV', level: '高', description: '客开风险', suggestion: '建议...' },
          { type: 'DATA_VOLUME', level: '中', description: '数据量风险', suggestion: '建议...' }
        ],
        evaluationOverview: {
          moduleCount: 2,
          dataVolume: 500,
          dataVolumeLadder: '中型',
          userCount: 600,
          userCountLadder: '中规模',
          reportCount: 50,
          hasCustomDev: true
        }
      }
      ;(request.get as any).mockResolvedValue({ data: mockStatistics })

      const result = await getStatistics(1)

      expect(result.riskWarnings).toHaveLength(2)
      expect(result.riskWarnings[0].type).toBe('CUSTOM_DEV')
      expect(result.riskWarnings[0].level).toBe('高')
    })

    it('STAT-API-006: 评估概览正确', async () => {
      const mockStatistics = {
        projectId: 1,
        totalWorkload: 156.38,
        estimatedMonths: 7.11,
        workloadTypeDistribution: [],
        moduleWorkloads: [],
        multiDimensionIndicators: {
          dataVolumeValue: 50,
          dataVolumeActual: '500万条',
          dataVolumeLadder: '中型',
          userCountValue: 60,
          userCountActual: '600人',
          userCountLadder: '中规模',
          moduleCountValue: 10,
          moduleCountActual: 2,
          reportCountValue: 50,
          reportCountActual: 50,
          customDevValue: 80,
          hasCustomDev: true,
          customDevWorkload: 20
        },
        riskWarnings: [],
        evaluationOverview: {
          moduleCount: 2,
          dataVolume: 500,
          dataVolumeLadder: '中型',
          userCount: 600,
          userCountLadder: '中规模',
          reportCount: 50,
          hasCustomDev: true
        }
      }
      ;(request.get as any).mockResolvedValue({ data: mockStatistics })

      const result = await getStatistics(1)

      expect(result.evaluationOverview.moduleCount).toBe(2)
      expect(result.evaluationOverview.dataVolume).toBe(500)
      expect(result.evaluationOverview.userCount).toBe(600)
      expect(result.evaluationOverview.reportCount).toBe(50)
      expect(result.evaluationOverview.hasCustomDev).toBe(true)
    })
  })
})
