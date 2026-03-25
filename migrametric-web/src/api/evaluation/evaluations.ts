/**
 * 评估相关 API
 */
import { request } from '@/utils/request'

/**
 * 评估状态
 */
export type EvaluationStatus = 'DRAFT' | 'IN_PROGRESS' | 'COMPLETED'

/**
 * 评估步骤
 */
export type EvaluationStep = 1 | 2 | 3 | 4

/**
 * 阶梯匹配结果
 */
export interface LadderMatchVO {
  ladderId: number
  ladderName: string
  weight: number
  ladderType: 'DATA_VOLUME' | 'USER_COUNT'
  inputValue: string
  rangeText: string
}

/**
 * 评估基础信息VO
 */
export interface EvaluationBaseVO {
  id: number
  projectId: number
  evaluationStatus: EvaluationStatus | null
  evaluationStatusText: string | null
  evaluationTime: string | null
}

/**
 * 评估详情VO（包含所有指标）
 */
export interface EvaluationDetailVO {
  id: number
  projectId: number
  tableCount: number | null
  dataVolume: number | null
  dataVolumeLadderId: number | null
  dataVolumeLadderName: string | null
  dataVolumeWeight: number | null
  userCount: number | null
  userCountLadderId: number | null
  userCountLadderName: string | null
  userCountWeight: number | null
  reportCount: number | null
  hasCustomDev: boolean
  customDevCount: number | null
  customDevWorkload: number | null
  dataCleanDesc: string
  dataCleanComplexityText: string
  coreWorkload: number | null
  reportWorkload: number | null
  totalWorkload: number | null
  evaluationStatus: EvaluationStatus | null
  evaluationStatusText: string | null
  evaluationTime: string | null
  createTime: string
  updateTime: string
}

/**
 * 模块配置项
 */
export interface ModuleConfigItem {
  id?: number
  moduleId: number
  moduleName: string
  category: string
  baseWorkload: number
  defaultWeight: number
  weight: number
  checked: boolean
  projectId?: number
  moduleWorkload?: number
}

/**
 * 评估指标数据
 */
export interface EvaluationMetrics {
  tableCount: number | null
  dataVolume: number | null
  dataVolumeLadderId: number | null
  dataVolumeLadderName?: string | null
  dataVolumeWeight?: number | null
  userCount: number | null
  userCountLadderId: number | null
  userCountLadderName?: string | null
  userCountWeight?: number | null
  reportCount: number | null
  hasCustomDev: boolean
  customDevCount: number | null
  customDevWorkload: number | null
  dataCleanDesc: string
  dataCleanComplexity: number | undefined
}

/**
 * 工作量计算结果
 */
export interface WorkloadResultVO {
  projectId: number
  coreWorkload: number
  reportWorkload: number
  customDevWorkload: number
  totalWorkload: number
  moduleWorkloads: ModuleWorkloadVO[]
  dataVolumeWeight: number
  userCountWeight: number
  reportCoefficient: number
}

export interface ModuleWorkloadVO {
  moduleId: number
  moduleName: string
  category?: string
  baseWorkload: number
  weight: number
  dataVolumeWeight: number
  userCountWeight: number
  moduleWorkload: number
}

// ========== 评估相关API ==========

/**
 * 创建评估记录
 */
export function createEvaluation(projectId: number) {
  return request.post<{ data: number }>('/api/evaluations', { projectId }).then(res => (res as any).data)
}

/**
 * 获取评估详情
 */
export function getEvaluation(projectId: number) {
  return request.get<EvaluationDetailVO>(`/api/evaluations/${projectId}`).then(res => (res as any).data)
}

/**
 * 保存评估指标
 */
export function saveIndicators(projectId: number, metrics: EvaluationMetrics) {
  return request.put<void>(`/api/evaluations/${projectId}/indicators`, metrics).then(res => res.data)
}

/**
 * 获取项目可选模块列表
 */
export function getProjectModules(projectId: number) {
  return request.get<{ data: ModuleConfigItem[] }>(`/api/evaluations/${projectId}/modules`).then(res => (res as any).data?.data || [])
}

/**
 * 保存模块配置
 */
export function saveModuleConfig(projectId: number, modules: ModuleConfigItem[]) {
  return request.post<void>(`/api/evaluations/${projectId}/modules`, modules).then(res => res.data)
}

/**
 * 获取已配置的模块
 */
export function getConfiguredModules(projectId: number) {
  return request.get<{ data: ModuleConfigItem[] }>(`/api/evaluations/${projectId}/modules/configured`).then(res => (res as any).data?.data || [])
}

// ========== 阶梯匹配API ==========

/**
 * 获取阶梯匹配结果（数据量）
 */
export function matchDataVolume(volume: number | null) {
  return request.get<LadderMatchVO>('/api/ladder/data-volume/match', { params: { volume } }).then(res => (res as any).data)
}

/**
 * 获取阶梯匹配结果（用户数）
 */
export function matchUserCount(count: number | null) {
  return request.get<LadderMatchVO>('/api/ladder/user-count/match', { params: { count } }).then(res => (res as any).data)
}

// ========== 工作量计算API ==========

/**
 * 计算工作量
 */
export function calculateWorkload(projectId: number) {
  return request.post<WorkloadResultVO>(`/api/evaluations/${projectId}/calculate`).then(res => (res as any).data)
}

/**
 * 完成评估
 */
export function completeEvaluation(projectId: number) {
  return request.post<void>(`/api/evaluations/${projectId}/complete`).then(res => res.data)
}
