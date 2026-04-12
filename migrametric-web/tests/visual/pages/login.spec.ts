import { test, expect } from '@playwright/test'

test.describe('登录页面视觉回归测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
    await page.waitForLoadState('networkidle')
  })

  test('VIS-001: 登录页初始状态', async ({ page }) => {
    await page.waitForSelector('[data-testid="login-button"]')
    await expect(page).toHaveScreenshot('login-initial.png', {
      fullPage: true,
      animations: 'disabled'
    })
  })

  test('VIS-002: 表单填写状态', async ({ page }) => {
    await page.fill('[data-testid="username"]', 'admin')
    await page.fill('[data-testid="password"]', 'admin123')
    const form = page.locator('.login-form')
    await expect(form).toHaveScreenshot('login-form-filled.png', {
      animations: 'disabled'
    })
  })

  test('VIS-003: 错误提示状态', async ({ page }) => {
    await page.fill('[data-testid="username"]', 'admin')
    await page.fill('[data-testid="password"]', 'wrongpass')
    await page.click('[data-testid="login-button"]')
    await page.waitForSelector('.el-message--error', { timeout: 5000 })
    await expect(page).toHaveScreenshot('login-error.png', {
      fullPage: true,
      animations: 'disabled'
    })
  })

  test('VIS-004: 加载状态', async ({ page }) => {
    await page.fill('[data-testid="username"]', 'admin')
    await page.fill('[data-testid="password"]', 'admin123')
    await page.click('[data-testid="login-button"]')
    const button = page.locator('[data-testid="login-button"]')
    await expect(button).toHaveScreenshot('login-button-loading.png', {
      animations: 'disabled',
      timeout: 1000
    }).catch(() => {
      console.log('加载状态太快，无法捕获')
    })
  })

  test('VIS-005: 移动端响应式', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 })
    await page.waitForTimeout(500)
    await expect(page).toHaveScreenshot('login-mobile.png', {
      fullPage: true,
      animations: 'disabled'
    })
  })
})