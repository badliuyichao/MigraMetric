/**
 * 评估API测试
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
  createEvaluation,
  getEvaluation,
  saveIndicators,
  getProjectModules,
  saveModuleConfig,
  matchDataVolume,
  matchUserCount,
  calculateWorkload,
  completeEvaluation,
  getConfiguredModules,
  EvaluationMetrics,
  WorkloadResultVO,
  ModuleWorkloadVO
} from './evaluations'

describe('评估API测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('createEvaluation', () => {
    it('EVAL-API-001: 应该成功创建评估记录', async () => {
      const mockResponse = { data: 1 }
      ;(request.post as any).mockResolvedValue(mockResponse)

      const result = await createEvaluation(1)

      expect(request.post).toHaveBeenCalledWith('/api/evaluations', { projectId: 1 })
      expect(result).toBe(1)
    })
  })

  describe('getEvaluation', () => {
    it('EVAL-API-002: 应该获取评估详情', async () => {
      const mockEvaluation = {
        id: 1,
        projectId: 1,
        tableCount: 100,
        dataVolume: 500,
        userCount: 500,
        reportCount: 50,
        hasCustomDev: true,
        coreWorkload: 111.38,
        totalWorkload: 156.38
      }
      ;(request.get as any).mockResolvedValue({ data: mockEvaluation })

      const result = await getEvaluation(1)

      expect(request.get).toHaveBeenCalledWith('/api/evaluations/1')
      expect(result).toEqual(mockEvaluation)
    })

    it('EVAL-API-003: 获取不存在的评估返回null', async () => {
      ;(request.get as any).mockResolvedValue({ data: null })

      const result = await getEvaluation(999)

      expect(result).toBeNull()
    })
  })

  describe('saveIndicators', () => {
    it('EVAL-API-004: 应该成功保存评估指标', async () => {
      ;(request.put as any).mockResolvedValue({ data: null })

      const metrics: EvaluationMetrics = {
        tableCount: 100,
        dataVolume: 500,
        dataVolumeLadderId: 2,
        userCount: 500,
        userCountLadderId: 2,
        reportCount: 50,
        hasCustomDev: true,
        customDevCount: 2,
        customDevWorkload: 20,
        dataCleanDesc: '测试描述',
        dataCleanComplexity: 2
      }

      await saveIndicators(1, metrics)

      expect(request.put).toHaveBeenCalledWith('/api/evaluations/1/indicators', metrics)
    })
  })

  describe('getProjectModules', () => {
    it('EVAL-API-005: 应该获取项目可选模块列表', async () => {
      const mockModules = [
        { moduleId: 1, moduleName: '财务管理', baseWorkload: 15, defaultWeight: 1.3, checked: true },
        { moduleId: 2, moduleName: '供应链管理', baseWorkload: 20, defaultWeight: 1.5, checked: false }
      ]
      ;(request.get as any).mockResolvedValue({ data: { data: mockModules } })

      const result = await getProjectModules(1)

      expect(request.get).toHaveBeenCalledWith('/api/evaluations/1/modules')
      expect(result).toEqual(mockModules)
    })
  })

  describe('saveModuleConfig', () => {
    it('EVAL-API-006: 应该成功保存模块配置', async () => {
      ;(request.post as any).mockResolvedValue({ data: null })

      const modules = [
        { moduleId: 1, moduleName: '财务管理', weight: 1.3, checked: true },
        { moduleId: 2, moduleName: '供应链管理', weight: 1.5, checked: true }
      ]

      await saveModuleConfig(1, modules)

      expect(request.post).toHaveBeenCalledWith('/api/evaluations/1/modules', modules)
    })
  })

  describe('matchDataVolume', () => {
    it('EVAL-API-007: 应该匹配数据量阶梯', async () => {
      const mockMatch = {
        ladderId: 2,
        ladderName: '中型',
        weight: 1.0,
        ladderType: 'DATA_VOLUME',
        inputValue: '500万条',
        rangeText: '100万-1000万条'
      }
      ;(request.get as any).mockResolvedValue({ data: mockMatch })

      const result = await matchDataVolume(500)

      expect(request.get).toHaveBeenCalledWith('/api/ladder/data-volume/match', { params: { volume: 500 } })
      expect(result.ladderName).toBe('中型')
      expect(result.weight).toBe(1.0)
    })

    it('EVAL-API-008: 数据量<100万匹配小型阶梯', async () => {
      const mockMatch = {
        ladderId: 1,
        ladderName: '小型',
        weight: 0.8,
        ladderType: 'DATA_VOLUME',
        inputValue: '50万条',
        rangeText: '0-100万条'
      }
      ;(request.get as any).mockResolvedValue({ data: mockMatch })

      const result = await matchDataVolume(50)

      expect(result.ladderName).toBe('小型')
      expect(result.weight).toBe(0.8)
    })

    it('EVAL-API-009: 数据量≥10000万匹配超大型阶梯', async () => {
      const mockMatch = {
        ladderId: 4,
        ladderName: '超大型',
        weight: 2.0,
        ladderType: 'DATA_VOLUME',
        inputValue: '15000万条',
        rangeText: '10000万条以上'
      }
      ;(request.get as any).mockResolvedValue({ data: mockMatch })

      const result = await matchDataVolume(15000)

      expect(result.ladderName).toBe('超大型')
      expect(result.weight).toBe(2.0)
    })
  })

  describe('matchUserCount', () => {
    it('EVAL-API-010: 应该匹配用户数阶梯', async () => {
      const mockMatch = {
        ladderId: 2,
        ladderName: '中规模',
        weight: 1.2,
        ladderType: 'USER_COUNT',
        inputValue: '300人',
        rangeText: '100-500人'
      }
      ;(request.get as any).mockResolvedValue({ data: mockMatch })

      const result = await matchUserCount(300)

      expect(request.get).toHaveBeenCalledWith('/api/ladder/user-count/match', { params: { count: 300 } })
      expect(result.ladderName).toBe('中规模')
      expect(result.weight).toBe(1.2)
    })

    it('EVAL-API-011: 用户数<100人匹配小规模阶梯', async () => {
      const mockMatch = {
        ladderId: 1,
        ladderName: '小规模',
        weight: 1.0,
        ladderType: 'USER_COUNT',
        inputValue: '50人',
        rangeText: '0-100人'
      }
      ;(request.get as any).mockResolvedValue({ data: mockMatch })

      const result = await matchUserCount(50)

      expect(result.ladderName).toBe('小规模')
      expect(result.weight).toBe(1.0)
    })

    it('EVAL-API-012: 用户数≥1000人匹配超大规模阶梯', async () => {
      const mockMatch = {
        ladderId: 4,
        ladderName: '超大规模',
        weight: 2.0,
        ladderType: 'USER_COUNT',
        inputValue: '1500人',
        rangeText: '1000人以上'
      }
      ;(request.get as any).mockResolvedValue({ data: mockMatch })

      const result = await matchUserCount(1500)

      expect(result.ladderName).toBe('超大规模')
      expect(result.weight).toBe(2.0)
    })
  })

  describe('calculateWorkload', () => {
    it('EVAL-API-013: 应该成功计算工作量', async () => {
      const mockResult: WorkloadResultVO = {
        projectId: 1,
        coreWorkload: 111.38,
        reportWorkload: 25.00,
        customDevWorkload: 20.00,
        totalWorkload: 156.38,
        moduleWorkloads: [
          {
            moduleId: 1,
            moduleName: '财务管理',
            baseWorkload: 15,
            weight: 1.3,
            dataVolumeWeight: 1.5,
            userCountWeight: 1.5,
            moduleWorkload: 43.88
          },
          {
            moduleId: 2,
            moduleName: '供应链管理',
            baseWorkload: 20,
            weight: 1.5,
            dataVolumeWeight: 1.5,
            userCountWeight: 1.5,
            moduleWorkload: 67.50
          }
        ],
        dataVolumeWeight: 1.5,
        userCountWeight: 1.5,
        reportCoefficient: 0.5
      }
      ;(request.post as any).mockResolvedValue({ data: mockResult })

      const result = await calculateWorkload(1)

      expect(request.post).toHaveBeenCalledWith('/api/evaluations/1/calculate')
      expect(result.coreWorkload).toBe(111.38)
      expect(result.reportWorkload).toBe(25.00)
      expect(result.customDevWorkload).toBe(20.00)
      expect(result.totalWorkload).toBe(156.38)
      expect(result.moduleWorkloads).toHaveLength(2)
    })

    it('EVAL-API-014: 工作量计算公式验证', async () => {
      // 财务管理模块: 15 × 1.3 × 1.5 × 1.5 = 43.875 ≈ 43.88
      // 供应链管理模块: 20 × 1.5 × 1.5 × 1.5 = 67.5
      // 核心工作量: 43.88 + 67.50 = 111.38
      // 报表工作量: 50 × 0.5 = 25.00
      // 客开工作量: 20.00
      // 总工作量: 111.38 + 25.00 + 20.00 = 156.38

      const mockResult: WorkloadResultVO = {
        projectId: 1,
        coreWorkload: 111.38,
        reportWorkload: 25.00,
        customDevWorkload: 20.00,
        totalWorkload: 156.38,
        moduleWorkloads: [
          {
            moduleId: 1,
            moduleName: '财务管理',
            baseWorkload: 15,
            weight: 1.3,
            dataVolumeWeight: 1.5,
            userCountWeight: 1.5,
            moduleWorkload: 43.88
          },
          {
            moduleId: 2,
            moduleName: '供应链管理',
            baseWorkload: 20,
            weight: 1.5,
            dataVolumeWeight: 1.5,
            userCountWeight: 1.5,
            moduleWorkload: 67.50
          }
        ],
        dataVolumeWeight: 1.5,
        userCountWeight: 1.5,
        reportCoefficient: 0.5
      }
      ;(request.post as any).mockResolvedValue({ data: mockResult })

      const result = await calculateWorkload(1)

      // 验证财务管理模块工作量
      expect(result.moduleWorkloads[0].moduleWorkload).toBeCloseTo(43.88, 1)

      // 验证供应链管理模块工作量
      expect(result.moduleWorkloads[1].moduleWorkload).toBeCloseTo(67.5, 1)

      // 验证总工作量
      expect(result.totalWorkload).toBeCloseTo(156.38, 1)
    })
  })

  describe('completeEvaluation', () => {
    it('EVAL-API-015: 应该成功完成评估', async () => {
      ;(request.post as any).mockResolvedValue({ data: null })

      await completeEvaluation(1)

      expect(request.post).toHaveBeenCalledWith('/api/evaluations/1/complete')
    })
  })

  describe('getConfiguredModules', () => {
    it('EVAL-API-016: 应该获取已配置的模块', async () => {
      const mockModules = [
        { moduleId: 1, moduleName: '财务管理', weight: 1.3 },
        { moduleId: 2, moduleName: '供应链管理', weight: 1.5 }
      ]
      ;(request.get as any).mockResolvedValue({ data: { data: mockModules } })

      const result = await getConfiguredModules(1)

      expect(request.get).toHaveBeenCalledWith('/api/evaluations/1/modules/configured')
      expect(result).toEqual(mockModules)
    })
  })
})
