import { beforeAll, afterAll } from 'vitest'

// 设置测试超时时间（毫秒）
const TEST_TIMEOUT = 10000

// 全局错误处理
beforeAll(() => {
  // 设置测试超时
  vi.setConfig({
    testTimeout: TEST_TIMEOUT,
    hookTimeout: TEST_TIMEOUT,
  })

  // 抑制特定警告
  console.warn = (...args: any[]) => {
    if (
      typeof args[0] === 'string' &&
      args[0].includes('[Vue warn]')
    ) {
      return
    }
    console.log('[WARNING]', ...args)
  }
})

afterAll(() => {
  // 清理资源
})

// 导出配置供其他测试文件使用
export const testConfig = {
  timeout: TEST_TIMEOUT,
}