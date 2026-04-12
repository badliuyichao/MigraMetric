/**
 * 用户数阶梯API
 */
import { request } from '@/utils/request'

/**
 * 用户数阶梯创建参数
 */
export interface UserCountLadderCreate {
  ladderName: string
  minCount: number
  maxCount?: number
  weight: number
  sortOrder?: number
}

/**
 * 用户数阶梯更新参数
 */
export interface UserCountLadderUpdate {
  id: number
  ladderName: string
  minCount: number
  maxCount?: number
  weight: number
  sortOrder: number
}

/**
 * 用户数阶梯VO
 */
export interface UserCountLadderVO {
  id: number
  ladderName: string
  minCount: number
  maxCount?: number
  countRangeText: string
  weight: number
  sortOrder: number
  createTime: string
  createBy: string
}

/**
 * 获取所有阶梯
 */
export function listAllUserCountLadders() {
  return request.get<UserCountLadderVO[]>('/user-count-ladders')
}

/**
 * 根据ID获取阶梯详情
 */
export function getUserCountLadderById(id: number) {
  return request.get<UserCountLadderVO>(`/user-count-ladders/${id}`)
}

/**
 * 创建阶梯
 */
export function createUserCountLadder(data: UserCountLadderCreate) {
  return request.post<number>('/user-count-ladders', data)
}

/**
 * 更新阶梯
 */
export function updateUserCountLadder(id: number, data: UserCountLadderUpdate) {
  return request.put<void>(`/user-count-ladders/${id}`, data)
}

/**
 * 删除阶梯
 */
export function deleteUserCountLadder(id: number) {
  return request.delete<void>(`/user-count-ladders/${id}`)
}

/**
 * 上移动阶梯
 */
export function moveUpUserCountLadder(id: number) {
  return request.patch<void>(`/user-count-ladders/${id}/move-up`)
}

/**
 * 下移动阶梯
 */
export function moveDownUserCountLadder(id: number) {
  return request.patch<void>(`/user-count-ladders/${id}/move-down`)
}

/**
 * 根据用户数匹配阶梯
 */
export function matchUserCountLadder(count: number) {
  return request.get<UserCountLadderVO>(`/user-count-ladders/match`, { count })
}
