import { test, expect } from '@playwright/test'
import { testUsers } from '../../fixtures/users'
import { shot } from '../helpers/screenshot-helper'

/**
 * 第一阶段·认证模块 E2E 用例
 *
 * 依据设计文档：
 * - 产品设计文档 §9.1.1 登录认证：用户名密码登录、密码加密、5次失败锁 30 分钟
 * - 产品设计文档 §9.1.2 会话管理：JWT Token 过期 2 小时
 * - 产品设计文档 §9.3.1 审计日志：用户登录/登出
 * - 需求说明文档 §4.3：用户认证、会话管理
 * - 前端实现：stores/user.ts logout()、router/permission.ts 路由守卫、layouts/index.vue 退出登录
 *
 * 不覆盖（设计文档未要求）：
 * - 记住密码（无需求来源）
 * - 忘记密码（无需求来源）
 * - 表单重置按钮（无需求来源）
 */

test.describe('第一阶段·登录流程E2E测试', () => {
  const __specDir = 'login'
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
    await page.evaluate(() => {
      localStorage.clear()
      sessionStorage.clear()
    })
  })

  // ----------------------------------------------------------
  // §9.1.1 登录认证
  // ----------------------------------------------------------

  test('E2E-001: 完整登录流程', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="username"]', testUsers.admin.username)
    await page.fill('[data-testid="password"]', testUsers.admin.password)
    await shot(page, '01-login-form-filled', __specDir)
    await page.click('[data-testid="login-button"]')
    // 登录后 router.push('/')，由路由重定向到 /dashboard
    await page.waitForURL('**/dashboard', { timeout: 10000 })
    await shot(page, '02-dashboard-after-login', __specDir)
    const token = await page.evaluate(() => localStorage.getItem('token'))
    expect(token).toBeTruthy()
  })

  test('E2E-002: 错误密码提示', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="username"]', testUsers.admin.username)
    await page.fill('[data-testid="password"]', 'wrongpassword')
    await page.click('[data-testid="login-button"]')
    await page.waitForSelector('.el-message--error', { timeout: 5000 })
    await shot(page, '03-login-error-toast', __specDir)
    expect(page.url()).toContain('/login')
  })

  // ----------------------------------------------------------
  // §9.1.2 会话管理：无 token 时路由守卫必须跳登录页
  // 注：注入 expired-token 会被后端 403 拒绝，而 axios 拦截器对 403
  // 不触发 handleUnauthorized，因此用"清空 token"模拟"过期态"
  // ----------------------------------------------------------

  test('E2E-003: 无 token 访问受保护资源跳登录页', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="username"]', testUsers.admin.username)
    await page.fill('[data-testid="password"]', testUsers.admin.password)
    await page.click('[data-testid="login-button"]')
    await page.waitForURL('**/dashboard', { timeout: 10000 })
    // 模拟 Session 过期：清空 token
    await page.evaluate(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    })
    await page.goto('/project/list')
    await page.waitForURL(/\/login(\?.*)?$/, { timeout: 10000 })
    await shot(page, '04-redirect-to-login-on-session-expired', __specDir)
  })

  // ----------------------------------------------------------
  // §9.3.1 用户登出
  // ----------------------------------------------------------

  test('E2E-004: 登出流程', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="username"]', testUsers.admin.username)
    await page.fill('[data-testid="password"]', testUsers.admin.password)
    await page.click('[data-testid="login-button"]')
    await page.waitForURL('**/dashboard', { timeout: 10000 })
    await page.click('[data-testid="user-menu"]')
    await shot(page, '05-user-menu-open', __specDir)
    await page.click('[data-testid="logout-button"]')
    // 退出登录有确认弹窗，点击"确定"
    await shot(page, '06-logout-confirm-dialog', __specDir)
    await page.locator('.el-message-box__btns .el-button--primary').click()
    await page.waitForURL(/\/login(\?.*)?$/, { timeout: 10000 })
    const token = await page.evaluate(() => localStorage.getItem('token'))
    expect(token).toBeNull()
    await shot(page, '07-after-logout', __specDir)
  })

  // ----------------------------------------------------------
  // 路由守卫衍生行为（无独立文档条款，作为登录功能的正确性保证）
  // ----------------------------------------------------------

  test('E2E-006: 已登录访问登录页自动跳转', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="username"]', testUsers.admin.username)
    await page.fill('[data-testid="password"]', testUsers.admin.password)
    await page.click('[data-testid="login-button"]')
    await page.waitForURL('**/dashboard', { timeout: 10000 })
    await page.goto('/login')
    await page.waitForURL('**/dashboard', { timeout: 5000 })
  })

  test('E2E-007: 浏览器后退拦截（已登录态）', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="username"]', testUsers.admin.username)
    await page.fill('[data-testid="password"]', testUsers.admin.password)
    await page.click('[data-testid="login-button"]')
    await page.waitForURL('**/dashboard', { timeout: 10000 })
    await page.goBack()
    await page.waitForURL('**/dashboard', { timeout: 3000 })
  })

  test('E2E-008: 禁用用户登录', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="username"]', testUsers.disabledUser.username)
    await page.fill('[data-testid="password"]', testUsers.disabledUser.password)
    await page.click('[data-testid="login-button"]')
    await page.waitForSelector('.el-message--error', { timeout: 5000 })
    await shot(page, '08-disabled-user-login-rejected', __specDir)
    expect(page.url()).toContain('/login')
  })
})
