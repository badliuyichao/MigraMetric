/**
 * 模块API
 */
import { request } from '@/utils/request'

/**
 * 模块查询参数
 */
export interface ModuleQuery {
  moduleName?: string
  systemId?: number
  category?: string
  status?: number
  pageNum?: number
  pageSize?: number
}

/**
 * 模块创建参数
 */
export interface ModuleCreate {
  moduleName: string
  systemId: number
  category?: string
  baseWorkload: number
  defaultWeight: number
  description?: string
}

/**
 * 模块更新参数
 */
export interface ModuleUpdate {
  id: number
  moduleName: string
  systemId: number
  category?: string
  baseWorkload: number
  defaultWeight: number
  description?: string
}

/**
 * 模块VO
 */
export interface ModuleVO {
  id: number
  moduleName: string
  systemId: number
  systemName: string
  systemCategory: number
  category: string
  baseWorkload: number
  defaultWeight: number
  description: string
  status: number
  statusText: string
  createTime: string
  createBy: string
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
 * 分页查询模块
 */
export function queryModulePage(params: ModuleQuery) {
  return request.get<PageResult<ModuleVO>>('/modules', { params }).then(res => res.data)
}

/**
 * 获取所有启用的模块
 */
export function listEnabledModules(systemId?: number) {
  return request.get<ModuleVO[]>('/modules/enabled', {
    params: systemId !== undefined ? { systemId } : {}
  }).then(res => res.data)
}

/**
 * 获取模块分类列表
 */
export function listModuleCategories(systemId?: number) {
  return request.get<string[]>('/modules/categories', {
    params: systemId !== undefined ? { systemId } : {}
  }).then(res => res.data)
}

/**
 * 根据ID获取模块详情
 */
export function getModuleById(id: number) {
  return request.get<ModuleVO>(`/modules/${id}`).then(res => res.data)
}

/**
 * 创建模块
 */
export function createModule(data: ModuleCreate) {
  return request.post<number>('/modules', data).then(res => res.data)
}

/**
 * 更新模块
 */
export function updateModule(id: number, data: ModuleUpdate) {
  return request.put<void>(`/modules/${id}`, data).then(res => res.data)
}

/**
 * 删除模块
 */
export function deleteModule(id: number) {
  return request.delete<void>(`/modules/${id}`).then(res => res.data)
}

/**
 * 启用模块
 */
export function enableModule(id: number) {
  return request.patch<void>(`/modules/${id}/enable`).then(res => res.data)
}

/**
 * 禁用模块
 */
export function disableModule(id: number) {
  return request.patch<void>(`/modules/${id}/disable`).then(res => res.data)
}
