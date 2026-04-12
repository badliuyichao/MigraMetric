/**
 * Pinia User Store 单元测试
 */
import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useUserStore } from '@/stores/user'

// Mock request module
vi.mock('@/utils/request', () => ({
  request: {
    get: vi.fn(),
    post: vi.fn().mockResolvedValue({
      data: { code: 200, message: 'success', data: { token: 'new-token' } }
    })
  }
}))

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  describe('State', () => {
    it('should have initial state', () => {
      const userStore = useUserStore()

      expect(userStore.token).toBe('')
      expect(userStore.userInfo).toBe(null)
      expect(userStore.isLoggedIn).toBe(false)
    })

    it('should restore state from localStorage', () => {
      const mockUserInfo = {
        id: 1,
        username: 'admin',
        name: '管理员',
        role: 'ADMIN',
        status: 1
      }
      localStorage.setItem('token', 'mock-token')
      localStorage.setItem('userInfo', JSON.stringify(mockUserInfo))

      const userStore = useUserStore()

      expect(userStore.token).toBe('mock-token')
      expect(userStore.userInfo).toEqual(mockUserInfo)
      expect(userStore.isLoggedIn).toBe(true)
    })
  })

  describe('Actions', () => {
    it('should set token directly', () => {
      const userStore = useUserStore()

      // 直接测试 token 设置功能
      userStore.token = 'test-token'

      expect(userStore.token).toBe('test-token')
      expect(userStore.isLoggedIn).toBe(true)
    })

    it('should clear state after logout', () => {
      const userStore = useUserStore()

      // Set some state
      userStore.token = 'mock-token'
      userStore.userInfo = { id: 1, username: 'admin', name: 'Admin', role: 'ADMIN', status: 1 }

      // Logout
      userStore.logout()

      expect(userStore.token).toBe('')
      expect(userStore.userInfo).toBe(null)
      expect(userStore.isLoggedIn).toBe(false)
      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('userInfo')).toBeNull()
    })

    it('should update user info', () => {
      const userStore = useUserStore()

      userStore.userInfo = { id: 1, username: 'admin', name: 'Admin', role: 'ADMIN', status: 1 }

      userStore.updateUserInfo({ name: 'New Name' })

      expect(userStore.userInfo?.name).toBe('New Name')
    })
  })

  describe('Computed', () => {
    it('should return correct userName', () => {
      const userStore = useUserStore()

      userStore.userInfo = { id: 1, username: 'admin', name: '张三', role: 'USER', status: 1 }
      expect(userStore.userName).toBe('张三')

      userStore.userInfo = { id: 1, username: 'admin', name: '', role: 'USER', status: 1 }
      expect(userStore.userName).toBe('admin')
    })

    it('should return isAdmin correctly', () => {
      const userStore = useUserStore()

      userStore.userInfo = { id: 1, username: 'admin', name: 'Admin', role: 'ADMIN', status: 1 }
      expect(userStore.isAdmin).toBe(true)

      userStore.userInfo = { id: 2, username: 'user', name: 'User', role: 'USER', status: 1 }
      expect(userStore.isAdmin).toBe(false)
    })

    it('should return roles array correctly', () => {
      const userStore = useUserStore()

      userStore.userInfo = { id: 1, username: 'admin', name: 'Admin', role: 'ADMIN', status: 1 }
      expect(userStore.roles).toEqual(['ADMIN'])
    })

    it('should return empty roles when userInfo is null', () => {
      const userStore = useUserStore()
      expect(userStore.roles).toEqual([])
    })
  })

  describe('hasRole', () => {
    it('should return true when user has required role', () => {
      const userStore = useUserStore()

      userStore.userInfo = { id: 1, username: 'admin', name: 'Admin', role: 'ADMIN', status: 1 }
      expect(userStore.hasRole(['ADMIN'])).toBe(true)
      expect(userStore.hasRole(['USER', 'ADMIN'])).toBe(true)
    })

    it('should return false when user does not have required role', () => {
      const userStore = useUserStore()

      userStore.userInfo = { id: 1, username: 'user', name: 'User', role: 'USER', status: 1 }
      expect(userStore.hasRole(['ADMIN'])).toBe(false)
    })

    it('should return true for admin with any role', () => {
      const userStore = useUserStore()

      userStore.userInfo = { id: 1, username: 'admin', name: 'Admin', role: 'ADMIN', status: 1 }
      expect(userStore.hasRole(['ANY_ROLE'])).toBe(true)
    })
  })
})
