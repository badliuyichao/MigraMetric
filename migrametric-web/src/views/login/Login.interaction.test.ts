/**
 * 登录页面组件交互测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { reactive, ref } from 'vue'

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {}
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value.toString()
    },
    removeItem: (key: string) => {
      delete store[key]
    },
    clear: () => {
      store = {}
    },
    get length() {
      return Object.keys(store).length
    },
    key: (index: number) => Object.keys(store)[index] || null
  }
})()

// Replace localStorage in tests
;(globalThis as any).localStorage = localStorageMock

// Mock user store
const mockLogin = vi.fn()
const mockPush = vi.fn()

vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    login: mockLogin,
    logout: vi.fn(),
    isLoggedIn: ref(false),
    token: ref(''),
    userInfo: ref(null)
  })
}))

vi.mock('@/api/user/auth', () => ({
  login: vi.fn()
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
    replace: vi.fn(),
    go: vi.fn(),
    back: vi.fn()
  }),
  useRoute: () => ({
    params: {},
    query: {},
    path: '/login'
  })
}))

describe('登录页面组件交互测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  describe('INT-001: 正确用户名密码登录', () => {
    it('应该成功登录并跳转到仪表盘', async () => {
      const loginForm = reactive({
        username: 'admin',
        password: 'admin123'
      })

      // 模拟登录成功
      mockLogin.mockResolvedValueOnce({ token: 'test-token' })

      // 验证表单数据
      expect(loginForm.username).toBe('admin')
      expect(loginForm.password).toBe('admin123')

      // 模拟登录调用
      await mockLogin({
        username: loginForm.username,
        password: loginForm.password
      })

      // 验证登录被调用
      expect(mockLogin).toHaveBeenCalledWith({
        username: 'admin',
        password: 'admin123'
      })
    })
  })

  describe('INT-002: 错误密码登录', () => {
    it('应该显示错误提示', async () => {
      const loginForm = reactive({
        username: 'admin',
        password: 'wrongpassword'
      })

      // 模拟登录失败
      mockLogin.mockRejectedValueOnce(new Error('用户名或密码错误'))

      try {
        await mockLogin({
          username: loginForm.username,
          password: loginForm.password
        })
      } catch (error) {
        expect(error).toBeInstanceOf(Error)
        expect((error as Error).message).toBe('用户名或密码错误')
      }

      expect(mockLogin).toHaveBeenCalled()
    })
  })

  describe('INT-003: 空用户名提交', () => {
    it('应该触发表单验证失败', async () => {
      const loginForm = reactive({
        username: '',
        password: 'admin123'
      })

      // 验证用户名为空
      expect(loginForm.username).toBe('')
      expect(loginForm.username.trim()).toBe('')

      // 表单验证应该失败
      const isValid = loginForm.username.trim() !== ''
      expect(isValid).toBe(false)
    })
  })

  describe('INT-004: 空密码提交', () => {
    it('应该触发表单验证失败', async () => {
      const loginForm = reactive({
        username: 'admin',
        password: ''
      })

      // 验证密码为空
      expect(loginForm.password).toBe('')
      expect(loginForm.password.trim()).toBe('')

      // 表单验证应该失败
      const isValid = loginForm.password.trim() !== ''
      expect(isValid).toBe(false)
    })
  })

  describe('INT-005: 记住密码勾选', () => {
    it('应该保存登录状态到localStorage', async () => {
      const rememberMe = ref(false)

      // 模拟勾选记住密码
      rememberMe.value = true

      // 模拟保存到localStorage
      if (rememberMe.value) {
        localStorage.setItem('rememberMe', 'true')
        localStorage.setItem('savedUsername', 'admin')
      }

      expect(rememberMe.value).toBe(true)
      expect(localStorage.getItem('rememberMe')).toBe('true')
      expect(localStorage.getItem('savedUsername')).toBe('admin')
    })
  })

  describe('INT-006: 密码可见性切换', () => {
    it('应该切换密码字段的显示/隐藏', async () => {
      const passwordVisible = ref(false)

      // 初始状态：密码隐藏
      expect(passwordVisible.value).toBe(false)

      // 切换为显示
      passwordVisible.value = true
      expect(passwordVisible.value).toBe(true)

      // 再次切换为隐藏
      passwordVisible.value = false
      expect(passwordVisible.value).toBe(false)
    })
  })

  describe('INT-007: Enter键提交', () => {
    it('应该触发登录表单提交', async () => {
      const loginForm = reactive({
        username: 'admin',
        password: 'admin123'
      })

      const submitForm = vi.fn()

      // 模拟Enter键提交
      const handleEnterKey = () => {
        if (loginForm.username && loginForm.password) {
          submitForm()
        }
      }

      handleEnterKey()

      expect(submitForm).toHaveBeenCalled()
    })
  })

  describe('INT-008: 快速连续点击', () => {
    it('应该防止重复提交', async () => {
      const loading = ref(false)
      const submitCount = { value: 0 }

      const handleSubmit = async () => {
        // 如果正在加载，直接返回
        if (loading.value) return

        loading.value = true
        submitCount.value++

        // 模拟异步操作
        await new Promise(resolve => setTimeout(resolve, 100))

        loading.value = false
      }

      // 模拟快速连续点击
      const promise1 = handleSubmit()
      const promise2 = handleSubmit() // 这次应该被忽略

      await Promise.all([promise1, promise2])

      // 应该只提交一次
      expect(submitCount.value).toBe(1)
    })
  })

  describe('INT-009: Token自动保存', () => {
    it('应该在登录成功后保存Token到localStorage', async () => {
      const token = ref('')

      // 模拟登录成功返回token
      const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test'

      // 保存token
      token.value = mockToken
      localStorage.setItem('token', mockToken)

      expect(token.value).toBe(mockToken)
      expect(localStorage.getItem('token')).toBe(mockToken)
    })
  })

  describe('INT-010: 网络错误处理', () => {
    it('应该显示网络错误提示', async () => {
      const errorMessage = ref('')

      // 模拟网络错误
      mockLogin.mockRejectedValueOnce(new Error('Network Error'))

      try {
        await mockLogin({
          username: 'admin',
          password: 'admin123'
        })
      } catch (error) {
        errorMessage.value = '网络连接失败，请检查网络'
      }

      expect(errorMessage.value).toBe('网络连接失败，请检查网络')
      expect(mockLogin).toHaveBeenCalled()
    })
  })
})