/**
 * 系统类型API
 */
import { request } from '@/utils/request'

/**
 * 系统类型查询参数
 */
export interface SystemTypeQuery {
  systemName?: string
  systemCategory?: number
  status?: number
  pageNum?: number
  pageSize?: number
}

/**
 * 系统类型创建参数
 */
export interface SystemTypeCreate {
  systemName: string
  systemCategory: number
  description?: string
  remark?: string
}

/**
 * 系统类型更新参数
 */
export interface SystemTypeUpdate {
  id: number
  systemName: string
  systemCategory: number
  description?: string
  remark?: string
}

/**
 * 系统类型VO
 */
export interface SystemTypeVO {
  id: number
  systemName: string
  systemCategory: number
  systemCategoryText: string
  description: string
  status: number
  statusText: string
  createTime: string
  createBy: string
  remark: string
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
  hasPrevious: boolean
  hasNext: boolean
}

/**
 * 分页查询系统类型
 */
export function querySystemTypePage(params: SystemTypeQuery) {
  return request.get<PageResult<SystemTypeVO>>('/system/types', params)
}

/**
 * 获取所有启用的系统类型
 */
export function listEnabledSystemTypes(category?: number) {
  return request.get<SystemTypeVO[]>('/system/types/enabled', category !== undefined ? { category } : {})
}

/**
 * 根据ID获取系统类型详情
 */
export function getSystemTypeById(id: number) {
  return request.get<SystemTypeVO>(`/system/types/${id}`)
}

/**
 * 创建系统类型
 */
export function createSystemType(data: SystemTypeCreate) {
  return request.post<number>('/system/types', data)
}

/**
 * 更新系统类型
 */
export function updateSystemType(id: number, data: SystemTypeUpdate) {
  return request.put<void>(`/system/types/${id}`, data)
}

/**
 * 删除系统类型
 */
export function deleteSystemType(id: number) {
  return request.delete<void>(`/system/types/${id}`)
}

/**
 * 启用系统类型
 */
export function enableSystemType(id: number) {
  return request.patch<void>(`/system/types/${id}/enable`)
}

/**
 * 禁用系统类型
 */
export function disableSystemType(id: number) {
  return request.patch<void>(`/system/types/${id}/disable`)
}
