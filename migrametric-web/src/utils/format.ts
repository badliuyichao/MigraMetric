/**
 * 日期格式化工具
 */
import dayjs from 'dayjs'

/**
 * 格式化日期
 */
export function formatDate(date: Date | string | number, format: string = 'YYYY-MM-DD'): string {
  return dayjs(date).format(format)
}

/**
 * 格式化日期时间
 */
export function formatDateTime(date: Date | string | number): string {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

/**
 * 格式化时间
 */
export function formatTime(date: Date | string | number): string {
  return dayjs(date).format('HH:mm:ss')
}

/**
 * 获取今天日期
 */
export function getToday(): string {
  return dayjs().format('YYYY-MM-DD')
}

/**
 * 获取昨天日期
 */
export function getYesterday(): string {
  return dayjs().subtract(1, 'day').format('YYYY-MM-DD')
}

/**
 * 获取指定天数后的日期
 */
export function addDays(days: number): string {
  return dayjs().add(days, 'day').format('YYYY-MM-DD')
}

/**
 * 获取指定天数前的日期
 */
export function subtractDays(days: number): string {
  return dayjs().subtract(days, 'day').format('YYYY-MM-DD')
}

/**
 * 计算日期差
 */
export function diffDays(date1: Date | string, date2: Date | string): number {
  return dayjs(date1).diff(dayjs(date2), 'day')
}

/**
 * 是否是今天
 */
export function isToday(date: Date | string | number): boolean {
  return dayjs(date).isSame(dayjs(), 'day')
}

/**
 * 是否是昨天
 */
export function isYesterday(date: Date | string | number): boolean {
  return dayjs(date).isSame(dayjs().subtract(1, 'day'), 'day')
}

/**
 * 是否是明天
 */
export function isTomorrow(date: Date | string | number): boolean {
  return dayjs(date).isSame(dayjs().add(1, 'day'), 'day')
}
