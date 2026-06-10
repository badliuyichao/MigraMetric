/**
 * 全局统计 API（REQ-3.4.2）
 */
import { request } from '@/utils/request'

/**
 * 维度
 */
export type AggregationDimension = 'module' | 'type' | 'complexity'

/**
 * 排行指标
 */
export type RankingMetric = 'workload' | 'userCount' | 'dataVolume'

/**
 * 筛选条件
 */
export interface GlobalAggregationFilters {
  dimension: AggregationDimension
  dateFrom?: string
  dateTo?: string
  sourceSystemId?: number
  targetSystemId?: number
  status?: string
}

/**
 * 聚合项
 */
export interface AggregationItem {
  name: string
  value: number
  percentage: number
  projectCount: number
}

/**
 * 阶梯桶（complexity 维度）
 */
export interface LadderBucket {
  ladderName: string
  projectCount: number
  totalWorkload: number
}

/**
 * 高复杂度模块
 */
export interface HighComplexityModule {
  moduleName: string
  weight: number
  projectCount: number
}

/**
 * 全局聚合结果
 */
export interface GlobalAggregationVO {
  dimension: AggregationDimension
  total: number | null
  items: AggregationItem[] | null
  dataVolumeDistribution: LadderBucket[] | null
  userCountDistribution: LadderBucket[] | null
  highComplexityModules: HighComplexityModule[] | null
}

/**
 * 排行条目
 */
export interface RankingItem {
  projectId: number
  projectName: string
  customerName: string
  value: number
  unit: string
}

/**
 * 全局排行结果
 */
export interface GlobalRankingVO {
  metric: RankingMetric
  items: RankingItem[]
}

/**
 * 调全局聚合接口
 */
export function getGlobalAggregations(filters: GlobalAggregationFilters) {
  return request.get<GlobalAggregationVO>('/statistics/global/aggregations', {
    ...filters
  } as Record<string, unknown>)
}

/**
 * 调全局排行接口
 */
export function getGlobalRanking(metric: RankingMetric, limit = 10) {
  return request.get<GlobalRankingVO>('/statistics/global/ranking', {
    metric,
    limit
  } as Record<string, unknown>)
}
