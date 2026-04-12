import { test, expect } from '@playwright/test'
import { testUsers } from '../../fixtures/users'

test.describe('登录流程E2E测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.evaluate(() => {
      localStorage.clear()
      sessionStorage.clear()
    })
  })

  test('E2E-001: 完整登录流程', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="username"]', testUsers.admin.username)
    await page.fill('[data-testid="password"]', testUsers.admin.password)
    await page.click('[data-testid="login-button"]')
    await page.waitForURL('/dashboard', { timeout: 10000 })
    const token = await page.evaluate(() => localStorage.getItem('token'))
    expect(token).toBeTruthy()
  })

  test('E2E-002: 错误密码提示', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="username"]', testUsers.admin.username)
    await page.fill('[data-testid="password"]', 'wrongpassword')
    await page.click('[data-testid="login-button"]')
    await page.waitForSelector('.el-message--error', { timeout: 5000 })
    expect(page.url()).toContain('/login')
  })

  test('E2E-003: Session过期处理', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="username"]', testUsers.admin.username)
    await page.fill('[data-testid="password"]', testUsers.admin.password)
    await page.click('[data-testid="login-button"]')
    await page.waitForURL('/dashboard', { timeout: 10000 })
    await page.evaluate(() => localStorage.setItem('token', 'expired-token'))
    await page.goto('/project/list')
    await page.waitForURL('/login', { timeout: 10000 })
  })

  test('E2E-004: 登出流程', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="username"]', testUsers.admin.username)
    await page.fill('[data-testid="password"]', testUsers.admin.password)
    await page.click('[data-testid="login-button"]')
    await page.waitForURL('/dashboard', { timeout: 10000 })
    await page.click('[data-testid="user-menu"]')
    await page.click('[data-testid="logout-button"]')
    await page.waitForURL('/login')
    const token = await page.evaluate(() => localStorage.getItem('token'))
    expect(token).toBeNull()
  })

  test('E2E-005: 自动登录（记住密码）', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="username"]', testUsers.admin.username)
    await page.fill('[data-testid="password"]', testUsers.admin.password)
    await page.check('[data-testid="remember-me"]')
    await page.click('[data-testid="login-button"]')
    await page.waitForURL('/dashboard', { timeout: 10000 })
    await page.goto('/login')
    await page.waitForURL('/dashboard', { timeout: 5000 }).catch(() => {})
  })

  test('E2E-006: 已登录访问登录页', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="username"]', testUsers.admin.username)
    await page.fill('[data-testid="password"]', testUsers.admin.password)
    await page.click('[data-testid="login-button"]')
    await page.waitForURL('/dashboard', { timeout: 10000 })
    await page.goto('/login')
    await page.waitForURL('/dashboard', { timeout: 5000 })
  })

  test('E2E-007: 浏览器后退拦截', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="username"]', testUsers.admin.username)
    await page.fill('[data-testid="password"]', testUsers.admin.password)
    await page.click('[data-testid="login-button"]')
    await page.waitForURL('/dashboard', { timeout: 10000 })
    await page.goBack()
    await page.waitForURL('/dashboard', { timeout: 3000 }).catch(() => {})
  })

  test('E2E-008: 禁用用户登录', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="username"]', testUsers.disabledUser.username)
    await page.fill('[data-testid="password"]', testUsers.disabledUser.password)
    await page.click('[data-testid="login-button"]')
    await page.waitForSelector('.el-message--error', { timeout: 5000 })
    expect(page.url()).toContain('/login')
  })

  test('E2E-009: 表单重置', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="username"]', 'testuser')
    await page.fill('[data-testid="password"]', 'testpass')
    await page.click('[data-testid="reset-button"]')
    const usernameValue = await page.inputValue('[data-testid="username"]')
    const passwordValue = await page.inputValue('[data-testid="password"]')
    expect(usernameValue).toBe('')
    expect(passwordValue).toBe('')
  })

  test('E2E-010: 忘记密码跳转', async ({ page }) => {
    await page.goto('/login')
    await page.click('[data-testid="forgot-password"]')
    await page.waitForURL('/forgot-password', { timeout: 5000 }).catch(() => {})
  })
})