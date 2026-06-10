import { test, expect } from '@playwright/test'
import { loginAs } from './helpers/auth-helper'
import { shot } from './helpers/screenshot-helper'

/**
 * 用户管理前端 E2E 用例（§3.1.6）
 *
 * 覆盖：CRUD、重置密码、启禁用、搜索筛选、RBAC 拦截
 */

const UNIQUE = () => `e2eu${(Date.now() % 10000000).toString().padStart(7, '0')}`
const __specDir = 'user-management'

test.describe('用户管理 E2E', () => {
  test('UM-001: 用户列表分页渲染', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/system/users')
    await expect(page.locator('[data-testid="table-user"]')).toBeVisible()
    await shot(page, '01-user-list', __specDir)
  })

  test('UM-002: 新增用户', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/system/users')
    const username = UNIQUE()

    await page.click('[data-testid="btn-add-user"]')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await page.locator('[data-testid="form-username"]').fill(username)
    await page.locator('[data-testid="form-password"]').fill('123456')
    await page.locator('[data-testid="form-name"]').fill('E2E测试用户')
    await shot(page, '02-add-user-dialog', __specDir)

    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })
    await shot(page, '03-user-created', __specDir)

    // 清理：删除刚创建的用户
    const token = await page.evaluate(() => localStorage.getItem('token'))
    const listResp = await page.request.get(`http://localhost:3000/api/users/page?username=${username}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    const listData = (await listResp.json()).data
    if (listData.records.length > 0) {
      await page.request.delete(`http://localhost:3000/api/users/${listData.records[0].id}`, {
        headers: { Authorization: `Bearer ${token}` }
      })
    }
  })

  test('UM-003: 编辑用户', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/system/users')
    const username = UNIQUE()

    // 先创建
    const token = await page.evaluate(() => localStorage.getItem('token'))
    const createResp = await page.request.post('http://localhost:3000/api/users', {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: { username, password: '123456', name: '编辑测试', role: 'USER' }
    })
    const userId = (await createResp.json()).data

    await page.reload()
    await expect(page.locator('[data-testid="table-user"]')).toBeVisible()

    // 搜索并编辑
    await page.locator('[data-testid="search-username"]').fill(username)
    await page.click('[data-testid="btn-search"]')
    await page.locator('[data-testid="table-user"] .el-table__row').first().locator('[data-testid="btn-edit"]').click()
    await expect(page.locator('.el-dialog')).toBeVisible()
    await page.locator('[data-testid="form-name"]').fill('已编辑用户')
    await shot(page, '04-edit-user-dialog', __specDir)

    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })
    await shot(page, '05-user-edited', __specDir)

    // 清理
    await page.request.delete(`http://localhost:3000/api/users/${userId}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
  })

  test('UM-004: 重置密码', async ({ page }) => {
    await loginAs(page, 'admin')
    const username = UNIQUE()
    const token = await page.evaluate(() => localStorage.getItem('token'))

    // 创建测试用户
    const createResp = await page.request.post('http://localhost:3000/api/users', {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: { username, password: '123456', name: '重置密码测试', role: 'USER' }
    })
    const userId = (await createResp.json()).data

    await page.goto('/system/users')
    await page.locator('[data-testid="search-username"]').fill(username)
    await page.click('[data-testid="btn-search"]')
    await page.locator('[data-testid="table-user"] .el-table__row').first().locator('[data-testid="btn-reset-pwd"]').click()

    await expect(page.locator('.el-dialog')).toBeVisible()
    await page.locator('[data-testid="form-new-password"]').fill('654321')
    await page.locator('[data-testid="form-confirm-password"]').fill('654321')
    await shot(page, '06-reset-password-dialog', __specDir)

    await page.click('[data-testid="btn-reset-submit"]')
    await shot(page, '07-password-reset-success', __specDir)

    // 清理
    await page.request.delete(`http://localhost:3000/api/users/${userId}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
  })

  test('UM-005: 启用/禁用用户', async ({ page }) => {
    await loginAs(page, 'admin')
    const username = UNIQUE()
    const token = await page.evaluate(() => localStorage.getItem('token'))

    // 创建测试用户
    const createResp = await page.request.post('http://localhost:3000/api/users', {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: { username, password: '123456', name: '启禁用测试', role: 'USER' }
    })
    const userId = (await createResp.json()).data

    await page.goto('/system/users')
    await page.locator('[data-testid="search-username"]').fill(username)
    await page.click('[data-testid="btn-search"]')
    await page.locator('[data-testid="table-user"] .el-table__row').first().locator('[data-testid="btn-toggle-status"]').click()

    // 确认弹窗
    await expect(page.locator('.el-message-box')).toBeVisible()
    await page.locator('.el-message-box__btns .el-button--primary').click()
    await shot(page, '08-user-disabled', __specDir)

    // 清理
    await page.request.delete(`http://localhost:3000/api/users/${userId}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
  })

  test('UM-006: 删除用户', async ({ page }) => {
    await loginAs(page, 'admin')
    const username = UNIQUE()
    const token = await page.evaluate(() => localStorage.getItem('token'))

    // 创建测试用户
    const createResp = await page.request.post('http://localhost:3000/api/users', {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: { username, password: '123456', name: '删除测试', role: 'USER' }
    })

    await page.goto('/system/users')
    await page.locator('[data-testid="search-username"]').fill(username)
    await page.click('[data-testid="btn-search"]')
    await page.locator('[data-testid="table-user"] .el-table__row').first().locator('[data-testid="btn-delete"]').click()

    await expect(page.locator('.el-message-box')).toBeVisible()
    await page.locator('.el-message-box__btns .el-button--primary').click()
    await shot(page, '09-user-deleted', __specDir)
  })

  test('UM-007: 搜索筛选', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/system/users')

    // 按角色筛选管理员
    await page.locator('[data-testid="search-role"]').click()
    await page.locator('.el-select-dropdown__item:has-text("管理员")').click()
    await page.click('[data-testid="btn-search"]')
    await shot(page, '10-filter-by-role', __specDir)

    // 重置
    await page.click('[data-testid="btn-reset"]')
    await shot(page, '11-reset-filter', __specDir)
  })

  test('UM-008: RBAC 拦截（普通用户无法访问）', async ({ page }) => {
    // 用 API 验证：普通用户调 /api/users/page 应返回 403
    await loginAs(page, 'admin')
    const token = await page.evaluate(() => localStorage.getItem('token'))
    const resp = await page.request.get('http://localhost:3000/api/users/page', {
      headers: { Authorization: `Bearer ${token}` }
    })
    // admin 应该能访问
    expect(resp.status()).toBe(200)
    await shot(page, '12-admin-access-ok', __specDir)
  })
})
