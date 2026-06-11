/**
 * 项目状态历史 API（REQ-§3.2.4）
 */
import { request } from '@/utils/request'

/**
 * 历史 VO
 */
export interface StatusHistoryVO {
  id: number
  projectId: number
  fromStatus: string | null
  toStatus: string
  event: string
  operator: string
  reason: string | null
  changeTime: string
  manualEdit: boolean
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

/**
 * 查询参数
 */
export interface StatusHistoryQuery {
  pageNum?: number
  pageSize?: number
  operator?: string
  event?: string
}

/**
 * 补录 DTO
 */
export interface StatusHistoryCreateDTO {
  fromStatus: string
  toStatus: string
  event: string
  reason?: string
  changeTime: string
}

/**
 * 分页查询项目状态历史
 */
export function listStatusHistory(projectId: number, query: StatusHistoryQuery = {}) {
  return request.get<PageResult<StatusHistoryVO>>(
    `/projects/${projectId}/status-history`,
    query as Record<string, unknown>
  )
}

/**
 * 人工补录（仅 ADMIN）
 */
export function manualCreateStatusHistory(
  projectId: number,
  dto: StatusHistoryCreateDTO
) {
  return request.post<number>(
    `/projects/${projectId}/status-history`,
    dto
  )
}
