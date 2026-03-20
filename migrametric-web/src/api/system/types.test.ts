/**
 * 系统类型API单元测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  querySystemTypePage,
  listEnabledSystemTypes,
  getSystemTypeById,
  createSystemType,
  updateSystemType,
  deleteSystemType,
  enableSystemType,
  disableSystemType
} from '@/api/system/types'

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

describe('System Type API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('querySystemTypePage', () => {
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

      const result = await querySystemTypePage({
        pageNum: 1,
        pageSize: 10,
        systemName: 'SAP',
        systemCategory: 1,
        status: 1
      })

      expect(mockedRequest.get).toHaveBeenCalledWith('/system/types', {
        params: {
          pageNum: 1,
          pageSize: 10,
          systemName: 'SAP',
          systemCategory: 1,
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

      await querySystemTypePage({
        pageNum: 1,
        pageSize: 10
      })

      expect(mockedRequest.get).toHaveBeenCalledWith('/system/types', {
        params: {
          pageNum: 1,
          pageSize: 10,
          systemName: undefined,
          systemCategory: undefined,
          status: undefined
        }
      })
    })
  })

  describe('listEnabledSystemTypes', () => {
    it('should call request.get without category parameter', async () => {
      const mockData = [
        { id: 1, systemName: 'SAP', systemCategory: 1 },
        { id: 2, systemName: '用友', systemCategory: 2 }
      ]
      mockedRequest.get.mockResolvedValue({ data: mockData })

      const result = await listEnabledSystemTypes()

      expect(mockedRequest.get).toHaveBeenCalledWith('/system/types/enabled', {
        params: {}
      })
      expect(result).toEqual(mockData)
    })

    it('should call request.get with category parameter', async () => {
      const mockData = [{ id: 1, systemName: 'SAP', systemCategory: 1 }]
      mockedRequest.get.mockResolvedValue({ data: mockData })

      const result = await listEnabledSystemTypes(1)

      expect(mockedRequest.get).toHaveBeenCalledWith('/system/types/enabled', {
        params: { category: 1 }
      })
      expect(result).toEqual(mockData)
    })
  })

  describe('getSystemTypeById', () => {
    it('should call request.get with correct id', async () => {
      const mockData = { id: 1, systemName: 'SAP', systemCategory: 1 }
      mockedRequest.get.mockResolvedValue({ data: mockData })

      const result = await getSystemTypeById(1)

      expect(mockedRequest.get).toHaveBeenCalledWith('/system/types/1')
      expect(result).toEqual(mockData)
    })
  })

  describe('createSystemType', () => {
    it('should call request.post with correct data', async () => {
      mockedRequest.post.mockResolvedValue({ data: 1 })

      const data = {
        systemName: 'Oracle',
        systemCategory: 1,
        description: 'Oracle EBS'
      }
      const result = await createSystemType(data)

      expect(mockedRequest.post).toHaveBeenCalledWith('/system/types', data)
      expect(result).toBe(1)
    })
  })

  describe('updateSystemType', () => {
    it('should call request.put with correct id and data', async () => {
      mockedRequest.put.mockResolvedValue({ data: undefined })

      const data = {
        id: 1,
        systemName: 'SAP R3',
        systemCategory: 1,
        description: 'Updated'
      }
      await updateSystemType(1, data)

      expect(mockedRequest.put).toHaveBeenCalledWith('/system/types/1', data)
    })
  })

  describe('deleteSystemType', () => {
    it('should call request.delete with correct id', async () => {
      mockedRequest.delete.mockResolvedValue({ data: undefined })

      await deleteSystemType(1)

      expect(mockedRequest.delete).toHaveBeenCalledWith('/system/types/1')
    })
  })

  describe('enableSystemType', () => {
    it('should call request.patch with correct id', async () => {
      mockedRequest.patch.mockResolvedValue({ data: undefined })

      await enableSystemType(1)

      expect(mockedRequest.patch).toHaveBeenCalledWith('/system/types/1/enable')
    })
  })

  describe('disableSystemType', () => {
    it('should call request.patch with correct id', async () => {
      mockedRequest.patch.mockResolvedValue({ data: undefined })

      await disableSystemType(1)

      expect(mockedRequest.patch).toHaveBeenCalledWith('/system/types/1/disable')
    })
  })
})
