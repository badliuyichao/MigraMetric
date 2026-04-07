import { config } from '@vue/test-utils'
import { vi, beforeEach, afterEach } from 'vitest'

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn(),
  },
  ElMessageBox: {
    confirm: vi.fn(),
    alert: vi.fn(),
    prompt: vi.fn(),
  },
  ElNotification: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn(),
  },
}))

// Mock Vue Router
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
    go: vi.fn(),
    back: vi.fn(),
    currentRoute: {
      value: {
        path: '/',
        params: {},
        query: {},
      },
    },
  }),
  useRoute: () => ({
    params: {},
    query: {},
    path: '/',
  }),
}))

// 配置全局组件 Mock
config.global.stubs = {
  RouterLink: {
    template: '<a><slot /></a>',
  },
  RouterView: {
    template: '<div><slot /></div>',
  },
}

// 每个测试前重置所有 Mock
beforeEach(() => {
  vi.clearAllMocks()
})

// 每个测试后清理
afterEach(() => {
  vi.restoreAllMocks()
})