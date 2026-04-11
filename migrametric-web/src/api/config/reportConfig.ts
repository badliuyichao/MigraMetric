/**
 * 报表配置API
 */
import { request } from '@/utils/request'

/**
 * 报表配置更新参数
 */
export interface ReportConfigUpdate {
  id: number
  configKey: string
  configValue: string
}

/**
 * 报表配置VO
 */
export interface ReportConfigVO {
  id: number
  configKey: string
  configValue: string
  configName: string
  description: string
  status: number
  statusText: string
  createTime: string
  createBy: string
}

/**
 * 获取所有配置
 */
export function listAllReportConfigs() {
  return request.get<ReportConfigVO[]>('/report-configs')
}

/**
 * 根据配置键获取配置值
 */
export function getReportConfigValue(configKey: string) {
  return request.get<string>('/report-configs/value', { configKey })
}

/**
 * 根据配置键获取配置值（数值）
 */
export function getReportConfigValueAsNumber(configKey: string, defaultValue?: number) {
  return request.get<number>('/report-configs/value/number', { configKey, defaultValue })
}

/**
 * 更新配置
 */
export function updateReportConfig(data: ReportConfigUpdate) {
  return request.put<void>('/report-configs', data)
}
