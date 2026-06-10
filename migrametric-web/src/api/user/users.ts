/**
 * 用户管理API
 */
import { request } from '@/utils/request'

export interface UserQuery {
  username?: string
  name?: string
  role?: string
  status?: number
  current?: number
  pageSize?: number
}

export interface UserCreate {
  username: string
  password: string
  name: string
  role: string
  email?: string
  phone?: string
  remark?: string
}

export interface UserUpdate {
  name?: string
  email?: string
  phone?: string
  role?: string
  status?: number
  remark?: string
}

export interface UserVO {
  id: number
  username: string
  name: string
  email: string
  phone: string
  role: string
  roleName: string
  status: number
  statusName: string
  createTime: string
  updateTime: string
  lastLoginTime: string
  remark: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

/** 分页查询用户 */
export function queryUserPage(params: UserQuery) {
  return request.get<PageResult<UserVO>>('/users/page', params)
}

/** 获取用户详情 */
export function getUserById(id: number) {
  return request.get<UserVO>(`/users/${id}`)
}

/** 创建用户 */
export function createUser(data: UserCreate) {
  return request.post<number>('/users', data)
}

/** 更新用户 */
export function updateUser(id: number, data: UserUpdate) {
  return request.put<void>(`/users/${id}`, data)
}

/** 删除用户 */
export function deleteUser(id: number) {
  return request.delete<void>(`/users/${id}`)
}

/** 重置密码 */
export function resetPassword(userId: number, newPassword: string, confirmPassword: string) {
  return request.post<void>('/users/password/reset', { userId, newPassword, confirmPassword })
}

/** 修改用户状态 */
export function updateUserStatus(userId: number, status: number) {
  return request.put<void>(`/users/${userId}/status?status=${status}`)
}

/** 检查用户名是否可用 */
export function checkUsername(username: string, excludeId?: number) {
  return request.get<boolean>('/users/check/username', { username, excludeId })
}
