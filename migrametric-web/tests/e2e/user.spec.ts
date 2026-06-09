import { test, expect } from '@playwright/test'
import { loginAs } from './helpers/auth-helper'
import { shot } from './helpers/screenshot-helper'

/**
 * 第六阶段·用户与权限 E2E 用例
 *
 * 依据需求说明文档 §3.1.6 + §1.3：
 * - §3.1.6 用户管理：新增/编辑/重置密码/启禁/删除用户
 * - §1.3 用户角色：管理员（ADMIN）vs 普通用户（USER）
 * - §9.1.1 登录认证：禁用用户无法登录
 *
 * 现状：后端 UserController 完整（CRUD/重置密码/启禁/分页/查 username），
 * 前端**没有用户管理页**（src/views/ 下无 user 目录），属于产品缺口。
 *
 * 本 spec 用 API + RBAC 边界验证，绕开缺失的前端页面：
 * - USER-001 admin 调 UserController API 全通（创建/启禁/重置密码/删除）
 * - USER-002 普通用户调 admin-only user API 被 403/401 拒绝
 * - USER-003 禁用用户无法登录（端到端：建用户→禁用→登录失败）
 *
 * 不覆盖（设计文档 P1 范围或前端缺失）：
 * - 用户管理 UI 流程（无前端页）
 * - 角色权限矩阵的逐项验证（仅验证最关键的 admin-only 边界）
 */

// 用户名长度必须满足前端 validateUsername 规则（4-20 字母数字下划线）：
// "e2eu" (4) + Date.now() 后 7 位 = 总长 11，留余量
const UNIQUE = () => `e2eu${(Date.now() % 10000000).toString().padStart(7, '0')}`
const __specDir = 'user'

test.describe('第六阶段·用户与权限 E2E', () => {
  // ===========================================================
  // §3.1.6 admin 用户管理 API 全通
  // ===========================================================

  test('USER-001: admin 完整走一遍 user CRUD + 重置密码 + 启禁', async ({ page }) => {
    await loginAs(page, 'admin')
    const token = await page.evaluate(() => localStorage.getItem('token'))
    const username = UNIQUE()
    const auth = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }

    // 1) 创建用户
    const createResp = await page.request.post('http://localhost:3000/api/users', {
      headers: auth,
      data: { username, password: 'initial123', name: 'E2E用户', role: 'USER', email: 'e2e@test.com' },
    })
    if (createResp.status() >= 300) {
      console.error('CREATE USER FAILED:', createResp.status(), await createResp.text())
    }
    expect(createResp.status(), '创建用户应返回 2xx').toBeLessThan(300)
    const userId = (await createResp.json()).data

    // 2) 查询列表能查到
    const page1 = await (await page.request.get('http://localhost:3000/api/users/page?pageNum=1&pageSize=20', {
      headers: auth,
    })).json()
    const found = page1.data.records.some((u: { username: string }) => u.username === username)
    expect(found, '新建用户应在分页列表中').toBe(true)

    // 3) 禁用用户
    const disableResp = await page.request.put(`http://localhost:3000/api/users/${userId}/status?status=0`, {
      headers: auth,
    })
    expect(disableResp.status()).toBeLessThan(300)

    // 4) 重置密码（DTO 同时要求 newPassword 和 confirmPassword）
    const resetResp = await page.request.post('http://localhost:3000/api/users/password/reset', {
      headers: auth,
      data: { userId, newPassword: 'reset456', confirmPassword: 'reset456' },
    })
    expect(resetResp.status()).toBeLessThan(300)

    // 5) 编辑用户（改名）
    const editResp = await page.request.put(`http://localhost:3000/api/users/${userId}`, {
      headers: auth,
      data: { name: 'E2E用户改名', role: 'USER' },
    })
    expect(editResp.status()).toBeLessThan(300)

    // 6) 启用回来
    const enableResp = await page.request.put(`http://localhost:3000/api/users/${userId}/status?status=1`, {
      headers: auth,
    })
    expect(enableResp.status()).toBeLessThan(300)

    // 7) 清理：删除用户
    const delResp = await page.request.delete(`http://localhost:3000/api/users/${userId}`, {
      headers: auth,
    })
    expect(delResp.status()).toBeLessThan(300)
  })

  // ===========================================================
  // §1.3 RBAC 边界：普通用户无 user 管理权限
  // ===========================================================

  test('USER-002: 普通用户调 admin-only user API 被拒绝', async ({ page }) => {
    // 先用 admin 创建一个普通用户（USER 角色）
    await loginAs(page, 'admin')
    const adminToken = await page.evaluate(() => localStorage.getItem('token'))
    const auth = { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' }
    const username = UNIQUE() + 'r'
    const createResp = await page.request.post('http://localhost:3000/api/users', {
      headers: auth,
      data: { username, password: 'pwd12345', name: 'E2E普通用户', role: 'USER' },
    })
    const userId = (await createResp.json()).data

    // 退出 admin
    await page.evaluate(() => { localStorage.clear() })

    // 用刚创建的普通用户登录
    const loginResp = await page.request.post('http://localhost:3000/api/auth/login', {
      data: { username, password: 'pwd12345' },
    })
    const userToken = (await loginResp.json()).data.token

    // 尝试调 user 管理 API（应被拒绝：401/403）
    const userListResp = await page.request.get('http://localhost:3000/api/users/page?pageNum=1&pageSize=10', {
      headers: { Authorization: `Bearer ${userToken}` },
      failOnStatusCode: false,
    })
    expect(userListResp.status(), '普通用户调 user 列表应被拒绝').toBeGreaterThanOrEqual(400)
    expect(userListResp.status()).toBeLessThan(500)

    // 尝试创建另一个用户也应被拒绝
    const createAnother = await page.request.post('http://localhost:3000/api/users', {
      headers: { Authorization: `Bearer ${userToken}`, 'Content-Type': 'application/json' },
      data: { username: 'forbidden', password: 'xxx', name: 'X', role: 'USER' },
      failOnStatusCode: false,
    })
    expect(createAnother.status(), '普通用户创建用户应被拒绝').toBeGreaterThanOrEqual(400)

    // 清理：恢复 admin，删掉这个测试用户
    await loginAs(page, 'admin')
    const admin2Token = await page.evaluate(() => localStorage.getItem('token'))
    await page.request.delete(`http://localhost:3000/api/users/${userId}`, {
      headers: { Authorization: `Bearer ${admin2Token}` },
    })
  })

  // ===========================================================
  // §3.1.6 启禁用户 + §9.1.1 端到端：禁用用户无法登录
  // ===========================================================

  test('USER-003: 禁用用户登录失败（端到端：建→禁用→登录拒绝）', async ({ page }) => {
    await loginAs(page, 'admin')
    const adminToken = await page.evaluate(() => localStorage.getItem('token'))
    const auth = { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' }
    const username = UNIQUE() + 'd'

    // 建用户
    const createResp = await page.request.post('http://localhost:3000/api/users', {
      headers: auth,
      data: { username, password: 'activepwd', name: 'E2E待禁用', role: 'USER' },
    })
    const userId = (await createResp.json()).data

    // 登录应该成功（启用态）
    const okLogin = await page.request.post('http://localhost:3000/api/auth/login', {
      data: { username, password: 'activepwd' },
    })
    expect(okLogin.status(), '启用态用户应能登录').toBe(200)

    // 禁用
    await page.request.put(`http://localhost:3000/api/users/${userId}/status?status=0`, {
      headers: auth,
    })

    // 再登录应失败（USER_DISABLED 业务码 10005：HTTP 200 + body.code=10005）
    const failLogin = await page.request.post('http://localhost:3000/api/auth/login', {
      data: { username, password: 'activepwd' },
      failOnStatusCode: false,
    })
    expect(failLogin.status(), '禁用态用户登录应返回 HTTP 200（业务码走 Result.code，非 HTTP 4xx）').toBe(200)
    const failBody = await failLogin.json()
    expect(failBody.code, '应返回 USER_DISABLED 业务码 10005').toBe(10005)

    // 浏览器端复现：跳登录页用禁用账号提交，截错误 toast
    // 走真实 UI 而非 page.request，证明前端 USER_DISABLED 错误提示确实渲染
    await page.goto('/login')
    await page.evaluate(() => localStorage.clear())
    await page.goto('/login')
    await page.fill('[data-testid="username"]', username)
    await page.fill('[data-testid="password"]', 'activepwd')
    await page.click('[data-testid="login-button"]')
    await page.waitForSelector('.el-message--error', { timeout: 5000 })
    await shot(page, '01-disabled-user-login-error-toast', __specDir)

    // 恢复 + 清理
    await page.request.put(`http://localhost:3000/api/users/${userId}/status?status=1`, {
      headers: auth,
    })
    await page.request.delete(`http://localhost:3000/api/users/${userId}`, {
      headers: auth,
    })
  })
})
