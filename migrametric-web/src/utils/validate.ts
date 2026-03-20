/**
 * 表单验证工具函数
 */

/**
 * 验证用户名
 * 规则：4-20位字符，只能包含字母、数字、下划线
 */
export function validateUsername(username: string): boolean {
  const reg = /^[a-zA-Z0-9_]{4,20}$/
  return reg.test(username)
}

/**
 * 验证密码
 * 规则：6-20位字符
 */
export function validatePassword(password: string): boolean {
  return password.length >= 6 && password.length <= 20
}

/**
 * 验证邮箱
 */
export function validateEmail(email: string): boolean {
  const reg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  return reg.test(email)
}

/**
 * 验证手机号
 */
export function validatePhone(phone: string): boolean {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

/**
 * 验证URL
 */
export function validateUrl(url: string): boolean {
  try {
    new URL(url)
    return true
  } catch {
    return false
  }
}

/**
 * 验证正整数
 */
export function validatePositiveInteger(num: number | string): boolean {
  const reg = /^[1-9]\d*$/
  return reg.test(String(num))
}

/**
 * 验证非负数
 */
export function validateNonNegative(num: number | string): boolean {
  const reg = /^\d+(\.\d+)?$/
  return reg.test(String(num))
}

/**
 * 验证系数范围（0.1-10.0）
 */
export function validateCoefficient(value: number): boolean {
  return value >= 0.1 && value <= 10.0
}

/**
 * 验证必填字符串
 */
export function validateRequired(value: string | null | undefined): boolean {
  return value !== null && value !== undefined && value.toString().trim().length > 0
}

/**
 * 验证最大长度
 */
export function validateMaxLength(value: string, maxLength: number): boolean {
  return value.length <= maxLength
}

/**
 * 验证最小长度
 */
export function validateMinLength(value: string, minLength: number): boolean {
  return value.length >= minLength
}
