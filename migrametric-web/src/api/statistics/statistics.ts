/**
 * 统计相关 API
 */
import { request } from '@/utils/request'

/**
 * 工作量类型分布项
 */
export interface WorkloadTypeItem {
  type: string
  workload: number
  percentage: number
}

/**
 * 模块工作量对比项
 */
export interface ModuleWorkloadItem {
  moduleName: string
  category: string
  baseWorkload: number
  weight: number
  workload: number
  percentage: number
}

/**
 * 多维度评估指标
 */
export interface MultiDimensionIndicators {
  dataVolumeValue: number
  dataVolumeActual: string
  dataVolumeLadder: string
  userCountValue: number
  userCountActual: string
  userCountLadder: string
  moduleCountValue: number
  moduleCountActual: number
  reportCountValue: number
  reportCountActual: number
  customDevValue: number
  hasCustomDev: boolean
  customDevWorkload: number | null
}

/**
 * 风险提示
 */
export interface RiskWarning {
  type: string
  level: string
  description: string
  suggestion: string
}

/**
 * 评估指标概览
 */
export interface EvaluationOverview {
  moduleCount: number
  dataVolume: number | null
  dataVolumeLadder: string | null
  userCount: number | null
  userCountLadder: string | null
  reportCount: number | null
  hasCustomDev: boolean
}

/**
 * 统计结果VO
 */
export interface StatisticsResultVO {
  projectId: number
  totalWorkload: number
  estimatedMonths: number
  workloadTypeDistribution: WorkloadTypeItem[]
  moduleWorkloads: ModuleWorkloadItem[]
  multiDimensionIndicators: MultiDimensionIndicators
  riskWarnings: RiskWarning[]
  evaluationOverview: EvaluationOverview
}

// ========== 统计相关API ==========

/**
 * 获取项目统计结果
 */
export function getStatistics(projectId: number) {
  return request.get<StatisticsResultVO>(`/statistics/${projectId}`)
}
