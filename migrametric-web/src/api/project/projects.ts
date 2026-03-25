/**
 * 项目管理 API
 */
import { request } from '@/utils/request'

/**
 * 项目状态
 */
export type ProjectStatus = 'DRAFT' | 'IN_PROGRESS' | 'COMPLETED' | 'ARCHIVED'

/**
 * 评估状态
 */
export type EvaluationStatus = 'DRAFT' | 'IN_PROGRESS' | 'COMPLETED'

/**
 * 项目创建参数
 */
export interface ProjectCreate {
  projectName: string
  customerName: string
  sourceSystemId: number
  targetSystemId: number
  projectLeader?: string
  contact?: string
  description?: string
  evaluationDate: string
}

/**
 * 项目更新参数
 */
export interface ProjectUpdate {
  projectName?: string
  customerName?: string
  sourceSystemId?: number
  targetSystemId?: number
  projectLeader?: string
  contact?: string
  description?: string
  evaluationDate?: string
}

/**
 * 项目查询参数
 */
export interface ProjectQuery {
  pageNum?: number
  pageSize?: number
  projectName?: string
  customerName?: string
  sourceSystemId?: number
  targetSystemId?: number
  status?: ProjectStatus
}

/**
 * 项目VO
 */
export interface ProjectVO {
  id: number
  projectName: string
  customerName: string
  sourceSystemId: number
  sourceSystemName: string
  targetSystemId: number
  targetSystemName: string
  projectLeader: string
  contact: string
  description: string
  evaluationDate: string
  status: ProjectStatus
  statusText: string
  userId: number
  createByName: string
  createTime: string
  updateTime: string
}

/**
 * 项目详情VO（包含评估概况）
 */
export interface ProjectDetailVO {
  // 项目基本信息
  id: number
  projectName: string
  customerName: string
  sourceSystemId: number
  sourceSystemName: string
  targetSystemId: number
  targetSystemName: string
  projectLeader: string
  contact: string
  description: string
  evaluationDate: string
  status: ProjectStatus
  statusText: string
  userId: number
  createByName: string
  createTime: string
  updateTime: string

  // 评估概况
  hasEvaluation: boolean
  selectedModuleCount: number
  totalWorkload: number | null
  coreWorkload: number | null
  reportWorkload: number | null
  customDevWorkload: number | null
  dataVolume: number | null
  userCount: number | null
  reportCount: number | null
  tableCount: number | null
  evaluationStatus: EvaluationStatus | null
  evaluationStatusText: string | null
  evaluationTime: string | null
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
 * 项目分页查询响应
 */
export interface ProjectPageResponse extends PageResult<ProjectVO> {}

/**
 * 创建项目
 */
export function createProject(data: ProjectCreate) {
  return request.post<number>('/api/projects', data).then(res => res.data)
}

/**
 * 查询项目列表
 */
export function queryProjectPage(params: ProjectQuery) {
  return request.get<ProjectPageResponse>('/api/projects', { params }).then(res => res.data)
}

/**
 * 获取项目详情
 */
export function getProjectById(id: number) {
  return request.get<ProjectVO>(`/api/projects/${id}`).then(res => res.data)
}

/**
 * 获取项目详情（包含评估概况）
 */
export function getProjectDetail(id: number) {
  return request.get<ProjectDetailVO>(`/api/projects/${id}/detail`).then(res => res.data)
}

/**
 * 更新项目信息
 */
export function updateProject(id: number, data: ProjectUpdate) {
  return request.put<void>(`/api/projects/${id}`, data).then(res => res.data)
}

/**
 * 复制项目
 */
export function copyProject(id: number) {
  return request.post<number>(`/api/projects/${id}/copy`).then(res => res.data)
}

/**
 * 删除项目
 */
export function deleteProject(id: number) {
  return request.delete<void>(`/api/projects/${id}`).then(res => res.data)
}

/**
 * 归档项目
 */
export function archiveProject(id: number) {
  return request.post<void>(`/api/projects/${id}/archive`).then(res => res.data)
}
