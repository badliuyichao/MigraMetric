import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { request, type ApiResponse } from '@/utils/request'

export interface UserInfo {
  id: number
  username: string
  userName: string
  role: string
  status: number
  email?: string
  phone?: string
  avatar?: string
}

export interface LoginData {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  expiresIn: number
}

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.userName || userInfo.value?.username || '')
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const roles = computed(() => (userInfo.value?.role ? [userInfo.value.role] : []))

  // Actions
  /**
   * 用户登录
   */
  async function login(loginData: LoginData): Promise<void> {
    const response = await request.post<LoginResult>('/auth/login', loginData)
    const result = response.data

    if (result) {
      token.value = result.token
      localStorage.setItem('token', result.token)
    }
  }

  /**
   * 获取用户信息
   */
  async function getUserInfo(): Promise<void> {
    if (!token.value) return

    try {
      const response = await request.get<UserInfo>('/auth/info')
      const result = response.data

      if (result) {
        userInfo.value = result
        localStorage.setItem('userInfo', JSON.stringify(result))
      }
    } catch (error) {
      logout()
      throw error
    }
  }

  /**
   * 用户登出
   */
  function logout(): void {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  /**
   * 更新用户信息
   */
  function updateUserInfo(info: Partial<UserInfo>): void {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...info }
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
  }

  /**
   * 检查是否有指定角色
   */
  function hasRole(requiredRoles: string[]): boolean {
    if (isAdmin.value) return true
    return roles.value.some((role) => requiredRoles.includes(role))
  }

  // 初始化时从localStorage恢复用户信息
  function initUserInfo(): void {
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      try {
        userInfo.value = JSON.parse(storedUserInfo)
      } catch {
        localStorage.removeItem('userInfo')
      }
    }
  }

  // 初始化
  initUserInfo()

  return {
    // State
    token,
    userInfo,
    // Getters
    isLoggedIn,
    userName,
    isAdmin,
    roles,
    // Actions
    login,
    getUserInfo,
    logout,
    updateUserInfo,
    hasRole
  }
})
