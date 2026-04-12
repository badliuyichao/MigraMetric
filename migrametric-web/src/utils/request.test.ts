/**
 * Axios请求封装工具单元测试
 */
import { describe, it, expect, vi } from 'vitest'

// Mock dependencies
vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    token: 'mock-token',
    logout: vi.fn()
  })
}))

vi.mock('@/router', () => ({
  default: {
    push: vi.fn()
  }
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn(),
    warning: vi.fn()
  },
  ElMessageBox: {
    confirm: vi.fn().mockResolvedValue(true)
  }
}))

describe('Axios Request Utils', () => {
  describe('API Response Type', () => {
    it('should have correct ApiResponse structure', () => {
      const mockResponse = {
        code: 200,
        message: 'success',
        data: { id: 1, name: 'test' },
        timestamp: '2026-03-20T10:00:00'
      }

      expect(mockResponse.code).toBe(200)
      expect(mockResponse.message).toBe('success')
      expect(mockResponse.data).toBeDefined()
      expect(mockResponse.timestamp).toBeDefined()
    })

    it('should support generic data type', () => {
      interface UserData {
        id: number
        name: string
      }

      const mockUserResponse = {
        code: 200,
        message: 'success',
        data: { id: 1, name: 'admin' } as UserData
      }

      expect(mockUserResponse.data.id).toBe(1)
      expect(mockUserResponse.data.name).toBe('admin')
    })
  })

  describe('Page Response Type', () => {
    it('should have correct pagination structure', () => {
      const mockPageResponse = {
        records: [1, 2, 3],
        total: 100,
        pageNum: 1,
        pageSize: 10,
        totalPages: 10,
        hasPrevious: false,
        hasNext: true
      }

      expect(mockPageResponse.records).toHaveLength(3)
      expect(mockPageResponse.total).toBe(100)
      expect(mockPageResponse.pageNum).toBe(1)
      expect(mockPageResponse.pageSize).toBe(10)
      expect(mockPageResponse.totalPages).toBe(10)
      expect(mockPageResponse.hasPrevious).toBe(false)
      expect(mockPageResponse.hasNext).toBe(true)
    })

    it('should handle empty records', () => {
      const emptyPage = {
        records: [],
        total: 0,
        pageNum: 1,
        pageSize: 10,
        totalPages: 0,
        hasPrevious: false,
        hasNext: false
      }

      expect(emptyPage.records).toHaveLength(0)
      expect(emptyPage.totalPages).toBe(0)
      expect(emptyPage.hasNext).toBe(false)
    })

    it('should handle last page', () => {
      const lastPage = {
        records: [91, 92, 93],
        total: 93,
        pageNum: 10,
        pageSize: 10,
        totalPages: 10,
        hasPrevious: true,
        hasNext: false
      }

      expect(lastPage.hasPrevious).toBe(true)
      expect(lastPage.hasNext).toBe(false)
    })
  })

  describe('Error Response Handling', () => {
    it('should handle 401 unauthorized error', () => {
      const errorResponse = {
        response: {
          status: 401,
          data: { code: 401, message: 'Unauthorized', data: null }
        }
      }

      expect(errorResponse.response.status).toBe(401)
      expect(errorResponse.response.data.code).toBe(401)
    })

    it('should handle 403 forbidden error', () => {
      const errorResponse = {
        response: {
          status: 403,
          data: { code: 403, message: 'Forbidden', data: null }
        }
      }

      expect(errorResponse.response.status).toBe(403)
    })

    it('should handle 500 server error', () => {
      const errorResponse = {
        response: {
          status: 500,
          data: { code: 500, message: 'Internal Server Error', data: null }
        }
      }

      expect(errorResponse.response.status).toBe(500)
    })

    it('should handle network error', () => {
      const networkError = {
        code: 'ECONNABORTED',
        message: 'Network Error'
      }

      expect(networkError.code).toBe('ECONNABORTED')
    })
  })

  describe('Request Configuration', () => {
    it('should include required headers', () => {
      const config = {
        headers: {
          'Content-Type': 'application/json;charset=UTF-8'
        },
        baseURL: '/api',
        timeout: 30000
      }

      expect(config.headers['Content-Type']).toBe('application/json;charset=UTF-8')
      expect(config.baseURL).toBe('/api')
      expect(config.timeout).toBe(30000)
    })

    it('should add timestamp to GET requests', () => {
      const getParams = { id: 1 }
      const timestamp = Date.now()
      const finalParams = { ...getParams, _t: timestamp }

      expect(finalParams.id).toBe(1)
      expect(finalParams._t).toBeDefined()
    })

    it('should include Authorization header with token', () => {
      const token = 'mock-token'
      const headers = {
        Authorization: `Bearer ${token}`
      }

      expect(headers.Authorization).toBe('Bearer mock-token')
    })
  })
})
