/**
 * 模块API单元测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  queryModulePage,
  listEnabledModules,
  listModuleCategories,
  getModuleById,
  createModule,
  updateModule,
  deleteModule,
  enableModule,
  disableModule
} from '@/api/module/modules'

// Mock the request utility
vi.mock('@/utils/request', () => ({
  request: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn()
  }
}))

import { request } from '@/utils/request'

const mockedRequest = request as {
  get: ReturnType<typeof vi.fn>
  post: ReturnType<typeof vi.fn>
  put: ReturnType<typeof vi.fn>
  delete: ReturnType<typeof vi.fn>
  patch: ReturnType<typeof vi.fn>
}

describe('Module API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('queryModulePage', () => {
    it('should call request.get with correct parameters', async () => {
      const mockPageResult = {
        records: [],
        total: 0,
        pageNum: 1,
        pageSize: 10,
        totalPages: 0,
        hasPrevious: false,
        hasNext: false
      }
      mockedRequest.get.mockResolvedValue({ data: mockPageResult })

      const result = await queryModulePage({
        pageNum: 1,
        pageSize: 10,
        moduleName: '总账管理',
        systemId: 1,
        status: 1
      })

      expect(mockedRequest.get).toHaveBeenCalledWith('/modules', {
        params: {
          pageNum: 1,
          pageSize: 10,
          moduleName: '总账管理',
          systemId: 1,
          status: 1
        }
      })
      expect(result).toEqual(mockPageResult)
    })

    it('should handle optional parameters', async () => {
      const mockPageResult = {
        records: [],
        total: 0,
        pageNum: 1,
        pageSize: 10,
        totalPages: 0,
        hasPrevious: false,
        hasNext: false
      }
      mockedRequest.get.mockResolvedValue({ data: mockPageResult })

      await queryModulePage({
        pageNum: 1,
        pageSize: 10
      })

      expect(mockedRequest.get).toHaveBeenCalledWith('/modules', {
        params: {
          pageNum: 1,
          pageSize: 10,
          moduleName: undefined,
          systemId: undefined,
          category: undefined,
          status: undefined
        }
      })
    })
  })

  describe('listEnabledModules', () => {
    it('should call request.get without systemId parameter', async () => {
      const mockData = [
        { id: 1, moduleName: '总账管理', systemId: 1 },
        { id: 2, moduleName: '应收管理', systemId: 1 }
      ]
      mockedRequest.get.mockResolvedValue({ data: mockData })

      const result = await listEnabledModules()

      expect(mockedRequest.get).toHaveBeenCalledWith('/modules/enabled', {
        params: {}
      })
      expect(result).toEqual(mockData)
    })

    it('should call request.get with systemId parameter', async () => {
      const mockData = [{ id: 1, moduleName: '总账管理', systemId: 1 }]
      mockedRequest.get.mockResolvedValue({ data: mockData })

      const result = await listEnabledModules(1)

      expect(mockedRequest.get).toHaveBeenCalledWith('/modules/enabled', {
        params: { systemId: 1 }
      })
      expect(result).toEqual(mockData)
    })
  })

  describe('listModuleCategories', () => {
    it('should call request.get and return categories', async () => {
      const mockData = ['财务模块', '供应链模块', '生产模块']
      mockedRequest.get.mockResolvedValue({ data: mockData })

      const result = await listModuleCategories()

      expect(mockedRequest.get).toHaveBeenCalledWith('/modules/categories', {
        params: {}
      })
      expect(result).toEqual(mockData)
    })
  })

  describe('getModuleById', () => {
    it('should call request.get with correct id', async () => {
      const mockData = { id: 1, moduleName: '总账管理', systemId: 1 }
      mockedRequest.get.mockResolvedValue({ data: mockData })

      const result = await getModuleById(1)

      expect(mockedRequest.get).toHaveBeenCalledWith('/modules/1')
      expect(result).toEqual(mockData)
    })
  })

  describe('createModule', () => {
    it('should call request.post with correct data', async () => {
      mockedRequest.post.mockResolvedValue({ data: 1 })

      const data = {
        moduleName: '应收管理',
        systemId: 1,
        category: '财务模块',
        baseWorkload: 12,
        defaultWeight: 1.1,
        description: '应收账款管理'
      }
      const result = await createModule(data)

      expect(mockedRequest.post).toHaveBeenCalledWith('/modules', data)
      expect(result).toBe(1)
    })
  })

  describe('updateModule', () => {
    it('should call request.put with correct id and data', async () => {
      mockedRequest.put.mockResolvedValue({ data: undefined })

      const data = {
        id: 1,
        moduleName: '总账管理V2',
        systemId: 1,
        category: '财务模块',
        baseWorkload: 18,
        defaultWeight: 1.3,
        description: '更新后的描述'
      }
      await updateModule(1, data)

      expect(mockedRequest.put).toHaveBeenCalledWith('/modules/1', data)
    })
  })

  describe('deleteModule', () => {
    it('should call request.delete with correct id', async () => {
      mockedRequest.delete.mockResolvedValue({ data: undefined })

      await deleteModule(1)

      expect(mockedRequest.delete).toHaveBeenCalledWith('/modules/1')
    })
  })

  describe('enableModule', () => {
    it('should call request.patch with correct id', async () => {
      mockedRequest.patch.mockResolvedValue({ data: undefined })

      await enableModule(1)

      expect(mockedRequest.patch).toHaveBeenCalledWith('/modules/1/enable')
    })
  })

  describe('disableModule', () => {
    it('should call request.patch with correct id', async () => {
      mockedRequest.patch.mockResolvedValue({ data: undefined })

      await disableModule(1)

      expect(mockedRequest.patch).toHaveBeenCalledWith('/modules/1/disable')
    })
  })
})
