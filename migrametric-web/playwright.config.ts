import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright测试配置
 *
 * 支持：
 * - E2E自动化测试（完整用户流程）
 * - 视觉回归测试（UI快照对比）
 */
export default defineConfig({
  // 测试目录
  testDir: './tests',

  // 完全并行执行
  fullyParallel: true,

  // CI环境下禁止only
  forbidOnly: !!process.env.CI,

  // 重试次数（本地开发不重试）
  retries: process.env.CI ? 2 : 0,

  // 并行worker数量
  workers: process.env.CI ? 1 : undefined,

  // 测试超时时间
  timeout: 30000,

  // 期望超时时间
  expect: {
    timeout: 5000
  },

  // 全局配置
  use: {
    // 基础URL
    baseURL: 'http://localhost:3000',

    // 失败时保留trace
    trace: 'retain-on-failure',

    // 失败时截图
    screenshot: 'only-on-failure',

    // 视频录制（CI环境）
    video: process.env.CI ? 'retain-on-failure' : 'off',
  },

  // 项目配置
  projects: [
    {
      name: 'e2e',
      testDir: './tests/e2e',
      testMatch: '**/*.spec.ts',
      use: {
        ...devices['Desktop Chrome'],
        viewport: { width: 1280, height: 720 }
      }
    },
    {
      name: 'visual',
      testDir: './tests/visual',
      testMatch: '**/*.spec.ts',
      use: {
        ...devices['Desktop Chrome'],
        viewport: { width: 1280, height: 720 }
      }
    }
  ],

  // 本地开发不需要启动webServer
  // 开发人员手动启动前端和后端服务
})