/**
 * 用户数阶梯API单元测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  listAllUserCountLadders,
  getUserCountLadderById,
  createUserCountLadder,
  updateUserCountLadder,
  deleteUserCountLadder,
  moveUpUserCountLadder,
  moveDownUserCountLadder,
  matchUserCountLadder
} from '@/api/ladder/userCount'

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

describe('User Count Ladder API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('listAllUserCountLadders', () => {
    it('should call request.get and return ladders', async () => {
      const mockData = [
        { id: 1, ladderName: '小规模', minCount: 0, maxCount: 100, weight: 1.0, sortOrder: 1 },
        { id: 2, ladderName: '中规模', minCount: 100, maxCount: 500, weight: 1.2, sortOrder: 2 }
      ]
      mockedRequest.get.mockResolvedValue({ data: mockData })

      const result = await listAllUserCountLadders()

      expect(mockedRequest.get).toHaveBeenCalledWith('/ladder/user-count')
      expect(result).toEqual(mockData)
    })
  })

  describe('getUserCountLadderById', () => {
    it('should call request.get with correct id', async () => {
      const mockData = { id: 1, ladderName: '小规模', minCount: 0, maxCount: 100, weight: 1.0 }
      mockedRequest.get.mockResolvedValue({ data: mockData })

      const result = await getUserCountLadderById(1)

      expect(mockedRequest.get).toHaveBeenCalledWith('/ladder/user-count/1')
      expect(result).toEqual(mockData)
    })
  })

  describe('createUserCountLadder', () => {
    it('should call request.post with correct data', async () => {
      mockedRequest.post.mockResolvedValue({ data: 1 })

      const data = {
        ladderName: '大规模',
        minCount: 500,
        maxCount: 1000,
        weight: 1.5
      }
      const result = await createUserCountLadder(data)

      expect(mockedRequest.post).toHaveBeenCalledWith('/ladder/user-count', data)
      expect(result).toBe(1)
    })
  })

  describe('updateUserCountLadder', () => {
    it('should call request.put with correct id and data', async () => {
      mockedRequest.put.mockResolvedValue({ data: undefined })

      const data = {
        id: 1,
        ladderName: '小规模V2',
        minCount: 0,
        maxCount: 150,
        weight: 1.1,
        sortOrder: 1
      }
      await updateUserCountLadder(1, data)

      expect(mockedRequest.put).toHaveBeenCalledWith('/ladder/user-count/1', data)
    })
  })

  describe('deleteUserCountLadder', () => {
    it('should call request.delete with correct id', async () => {
      mockedRequest.delete.mockResolvedValue({ data: undefined })

      await deleteUserCountLadder(1)

      expect(mockedRequest.delete).toHaveBeenCalledWith('/ladder/user-count/1')
    })
  })

  describe('moveUpUserCountLadder', () => {
    it('should call request.patch with correct id', async () => {
      mockedRequest.patch.mockResolvedValue({ data: undefined })

      await moveUpUserCountLadder(2)

      expect(mockedRequest.patch).toHaveBeenCalledWith('/ladder/user-count/2/move-up')
    })
  })

  describe('moveDownUserCountLadder', () => {
    it('should call request.patch with correct id', async () => {
      mockedRequest.patch.mockResolvedValue({ data: undefined })

      await moveDownUserCountLadder(1)

      expect(mockedRequest.patch).toHaveBeenCalledWith('/ladder/user-count/1/move-down')
    })
  })

  describe('matchUserCountLadder', () => {
    it('should call request.get with count parameter', async () => {
      const mockData = { id: 1, ladderName: '小规模', minCount: 0, maxCount: 100, weight: 1.0 }
      mockedRequest.get.mockResolvedValue({ data: mockData })

      const result = await matchUserCountLadder(50)

      expect(mockedRequest.get).toHaveBeenCalledWith('/ladder/user-count/match', {
        params: { count: 50 }
      })
      expect(result).toEqual(mockData)
    })
  })
})
