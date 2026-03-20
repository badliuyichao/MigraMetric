/**
 * 数据量阶梯API
 */
import { request } from '@/utils/request'

/**
 * 数据量阶梯创建参数
 */
export interface DataVolumeLadderCreate {
  ladderName: string
  minVolume: number
  maxVolume?: number
  weight: number
  sortOrder?: number
}

/**
 * 数据量阶梯更新参数
 */
export interface DataVolumeLadderUpdate {
  id: number
  ladderName: string
  minVolume: number
  maxVolume?: number
  weight: number
  sortOrder: number
}

/**
 * 数据量阶梯VO
 */
export interface DataVolumeLadderVO {
  id: number
  ladderName: string
  minVolume: number
  maxVolume?: number
  volumeRangeText: string
  weight: number
  sortOrder: number
  createTime: string
  createBy: string
}

/**
 * 获取所有阶梯
 */
export function listAllDataVolumeLadders() {
  return request.get<DataVolumeLadderVO[]>('/ladder/data-volume').then(res => res.data)
}

/**
 * 根据ID获取阶梯详情
 */
export function getDataVolumeLadderById(id: number) {
  return request.get<DataVolumeLadderVO>(`/ladder/data-volume/${id}`).then(res => res.data)
}

/**
 * 创建阶梯
 */
export function createDataVolumeLadder(data: DataVolumeLadderCreate) {
  return request.post<number>('/ladder/data-volume', data).then(res => res.data)
}

/**
 * 更新阶梯
 */
export function updateDataVolumeLadder(id: number, data: DataVolumeLadderUpdate) {
  return request.put<void>(`/ladder/data-volume/${id}`, data).then(res => res.data)
}

/**
 * 删除阶梯
 */
export function deleteDataVolumeLadder(id: number) {
  return request.delete<void>(`/ladder/data-volume/${id}`).then(res => res.data)
}

/**
 * 上移动阶梯
 */
export function moveUpDataVolumeLadder(id: number) {
  return request.patch<void>(`/ladder/data-volume/${id}/move-up`).then(res => res.data)
}

/**
 * 下移动阶梯
 */
export function moveDownDataVolumeLadder(id: number) {
  return request.patch<void>(`/ladder/data-volume/${id}/move-down`).then(res => res.data)
}

/**
 * 根据数据量匹配阶梯
 */
export function matchDataVolumeLadder(volume: number) {
  return request.get<DataVolumeLadderVO>(`/ladder/data-volume/match`, {
    params: { volume }
  }).then(res => res.data)
}
