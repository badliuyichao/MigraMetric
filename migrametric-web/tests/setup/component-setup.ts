import { config } from '@vue/test-utils'
import { vi, beforeEach, afterEach } from 'vitest'

// Mock localStorage for tests
const localStorageMock = (() => {
  let store: Record<string, string> = {}
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, value: string) => {
      store[key] = value.toString()
    }),
    removeItem: vi.fn((key: string) => {
      delete store[key]
    }),
    clear: vi.fn(() => {
      store = {}
    }),
    get length() {
      return Object.keys(store).length
    },
    key: vi.fn((index: number) => Object.keys(store)[index] || null)
  }
})()

// Assign to global scope
if (typeof globalThis.localStorage === 'undefined') {
  globalThis.localStorage = localStorageMock as any
}

if (typeof globalThis.sessionStorage === 'undefined') {
  globalThis.sessionStorage = localStorageMock as any
}

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
  createRouter: vi.fn(() => ({
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
  })),
  createWebHistory: vi.fn(),
}))

// Mock router module
vi.mock('@/router', () => ({
  default: {
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
  },
}))

// Mock request utility
vi.mock('@/utils/request', () => ({
  request: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn(),
  },
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn(),
  },
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