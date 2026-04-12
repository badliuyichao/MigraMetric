/**
 * 数据量阶梯API单元测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  listAllDataVolumeLadders,
  getDataVolumeLadderById,
  createDataVolumeLadder,
  updateDataVolumeLadder,
  deleteDataVolumeLadder,
  moveUpDataVolumeLadder,
  moveDownDataVolumeLadder,
  matchDataVolumeLadder
} from '@/api/ladder/dataVolume'

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

describe('Data Volume Ladder API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('listAllDataVolumeLadders', () => {
    it('should call request.get and return ladders', async () => {
      const mockData = [
        { id: 1, ladderName: '小型', minVolume: 0, maxVolume: 10, weight: 0.8, sortOrder: 1 },
        { id: 2, ladderName: '中型', minVolume: 10, maxVolume: 100, weight: 1.0, sortOrder: 2 }
      ]
      mockedRequest.get.mockResolvedValue({ data: mockData })

      const result = await listAllDataVolumeLadders()

      expect(mockedRequest.get).toHaveBeenCalledWith('/ladder/data-volume')
      expect(result).toEqual(mockData)
    })
  })

  describe('getDataVolumeLadderById', () => {
    it('should call request.get with correct id', async () => {
      const mockData = { id: 1, ladderName: '小型', minVolume: 0, maxVolume: 10, weight: 0.8 }
      mockedRequest.get.mockResolvedValue({ data: mockData })

      const result = await getDataVolumeLadderById(1)

      expect(mockedRequest.get).toHaveBeenCalledWith('/ladder/data-volume/1')
      expect(result).toEqual(mockData)
    })
  })

  describe('createDataVolumeLadder', () => {
    it('should call request.post with correct data', async () => {
      mockedRequest.post.mockResolvedValue({ data: 1 })

      const data = {
        ladderName: '大型',
        minVolume: 100,
        maxVolume: 1000,
        weight: 1.5
      }
      const result = await createDataVolumeLadder(data)

      expect(mockedRequest.post).toHaveBeenCalledWith('/ladder/data-volume', data)
      expect(result).toBe(1)
    })
  })

  describe('updateDataVolumeLadder', () => {
    it('should call request.put with correct id and data', async () => {
      mockedRequest.put.mockResolvedValue({ data: undefined })

      const data = {
        id: 1,
        ladderName: '小型V2',
        minVolume: 0,
        maxVolume: 15,
        weight: 0.9,
        sortOrder: 1
      }
      await updateDataVolumeLadder(1, data)

      expect(mockedRequest.put).toHaveBeenCalledWith('/ladder/data-volume/1', data)
    })
  })

  describe('deleteDataVolumeLadder', () => {
    it('should call request.delete with correct id', async () => {
      mockedRequest.delete.mockResolvedValue({ data: undefined })

      await deleteDataVolumeLadder(1)

      expect(mockedRequest.delete).toHaveBeenCalledWith('/ladder/data-volume/1')
    })
  })

  describe('moveUpDataVolumeLadder', () => {
    it('should call request.patch with correct id', async () => {
      mockedRequest.patch.mockResolvedValue({ data: undefined })

      await moveUpDataVolumeLadder(2)

      expect(mockedRequest.patch).toHaveBeenCalledWith('/ladder/data-volume/2/move-up')
    })
  })

  describe('moveDownDataVolumeLadder', () => {
    it('should call request.patch with correct id', async () => {
      mockedRequest.patch.mockResolvedValue({ data: undefined })

      await moveDownDataVolumeLadder(1)

      expect(mockedRequest.patch).toHaveBeenCalledWith('/ladder/data-volume/1/move-down')
    })
  })

  describe('matchDataVolumeLadder', () => {
    it('should call request.get with volume parameter', async () => {
      const mockData = { id: 1, ladderName: '小型', minVolume: 0, maxVolume: 10, weight: 0.8 }
      mockedRequest.get.mockResolvedValue({ data: mockData })

      const result = await matchDataVolumeLadder(5)

      expect(mockedRequest.get).toHaveBeenCalledWith('/ladder/data-volume/match', {
        params: { volume: 5 }
      })
      expect(result).toEqual(mockData)
    })
  })
})
