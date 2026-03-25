import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 添加Token
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }

    // 添加时间戳防止缓存
    if (config.method === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }

    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse<unknown>>) => {
    // 如果responseType是blob，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }

    const res = response.data

    // 判断业务逻辑是否成功
    if (res.code === 200) {
      return response
    }

    // 处理特定错误码
    if (res.code === 401) {
      handleUnauthorized()
      return Promise.reject(new Error(res.message || '未授权'))
    }

    if (res.code === 403) {
      ElMessage.error('暂无权限访问该资源')
      return Promise.reject(new Error(res.message || '暂无权限'))
    }

    // 其他业务错误
    ElMessage.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message || '操作失败'))
  },
  (error) => {
    console.error('响应错误:', error)

    if (error.response) {
      switch (error.response.status) {
        case 400:
          ElMessage.error('请求参数错误')
          break
        case 401:
          handleUnauthorized()
          break
        case 403:
          ElMessage.error('暂无权限访问该资源')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        case 502:
          ElMessage.error('网关错误')
          break
        case 503:
          ElMessage.error('服务暂不可用')
          break
        case 504:
          ElMessage.error('网关超时')
          break
        default:
          ElMessage.error('请求失败，请稍后重试')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else if (error.message.includes('Network Error')) {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error(error.message || '请求失败，请稍后重试')
    }

    return Promise.reject(error)
  }
)

// 处理401未授权
function handleUnauthorized() {
  const userStore = useUserStore()

  // 清除用户信息
  userStore.logout()

  // 提示并跳转登录页
  ElMessageBox.confirm('您的登录状态已过期，请重新登录', '提示', {
    confirmButtonText: '重新登录',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    router.push('/login')
  })
}

// 封装请求方法
export const request = {
  get<T>(url: string, params?: Record<string, unknown>, config?: AxiosRequestConfig) {
    return service.get<ApiResponse<T>>(url, { params, ...config })
  },

  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return service.post<ApiResponse<T>>(url, data, config)
  },

  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return service.put<ApiResponse<T>>(url, data, config)
  },

  delete<T>(url: string, params?: Record<string, unknown>, config?: AxiosRequestConfig) {
    return service.delete<ApiResponse<T>>(url, { params, ...config })
  },

  patch<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return service.patch<ApiResponse<T>>(url, data, config)
  }
}

// 导出axios实例
export default service

// 通用响应类型
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  timestamp: string
}

// 分页响应类型
export interface PageResponse<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
  hasPrevious: boolean
  hasNext: boolean
}

// ========== 增强功能 ==========

// 请求缓存
const requestCache = new Map<string, { promise: Promise<unknown>; timestamp: number }>()
const CACHE_TTL = 5000 // 缓存5秒

/**
 * 清除请求缓存
 */
export function clearRequestCache() {
  requestCache.clear()
}

/**
 * 清除指定URL的缓存
 */
export function clearUrlCache(url: string) {
  requestCache.delete(url)
}

/**
 * 带缓存的请求方法
 * 适用于频繁请求的数据（如下拉选项）
 */
export const cachedRequest = {
  async get<T>(url: string, params?: Record<string, unknown>, config?: AxiosRequestConfig): Promise<T> {
    const cacheKey = `${url}?${JSON.stringify(params || {})}`
    const now = Date.now()

    // 检查缓存
    const cached = requestCache.get(cacheKey)
    if (cached && now - cached.timestamp < CACHE_TTL) {
      return cached.promise as Promise<T>
    }

    // 创建新请求
    const promise = service.get<ApiResponse<T>>(url, { params, ...config }).then(res => {
      requestCache.delete(cacheKey) // 请求完成后清除缓存
      return res.data.data
    })

    // 存入缓存
    requestCache.set(cacheKey, { promise, timestamp: now })

    return promise
  }
}

/**
 * 防抖请求
 * 适用于搜索等高频请求场景
 */
export function debounceRequest<T>(
  fn: (...args: unknown[]) => Promise<T>,
  delay = 300
): (...args: unknown[]) => Promise<T | null> {
  let timer: ReturnType<typeof setTimeout> | null = null

  return (...args: unknown[]): Promise<T | null> => {
    return new Promise((resolve) => {
      if (timer) {
        clearTimeout(timer)
      }

      timer = setTimeout(async () => {
        try {
          const result = await fn(...args)
          resolve(result)
        } catch {
          resolve(null)
        }
      }, delay)
    })
  }
}

// ========== 错误码定义 ==========
export const ErrorCodes = {
  SUCCESS: 200,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  BAD_REQUEST: 400,
  INTERNAL_ERROR: 500,
  SERVICE_UNAVAILABLE: 503,
  GATEWAY_TIMEOUT: 504
} as const

// 错误消息映射
export const ErrorMessages: Record<number, string> = {
  [ErrorCodes.BAD_REQUEST]: '请求参数错误',
  [ErrorCodes.UNAUTHORIZED]: '登录状态已过期，请重新登录',
  [ErrorCodes.FORBIDDEN]: '暂无权限访问该资源',
  [ErrorCodes.NOT_FOUND]: '请求的资源不存在',
  [ErrorCodes.INTERNAL_ERROR]: '服务器内部错误',
  [ErrorCodes.SERVICE_UNAVAILABLE]: '服务暂不可用，请稍后重试',
  [ErrorCodes.GATEWAY_TIMEOUT]: '请求超时，请稍后重试'
} as const
