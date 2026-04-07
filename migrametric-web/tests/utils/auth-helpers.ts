import { Page } from '@playwright/test'
import { testUsers, getTestToken } from '../fixtures/users'

/**
 * 登录指定用户
 */
export async function login(
  page: Page,
  username: string = 'admin',
  password: string = 'admin123'
) {
  await page.goto('/login')

  // 填写登录表单
  await page.fill('input[placeholder="请输入用户名"]', username)
  await page.fill('input[placeholder="请输入密码"]', password)

  // 点击登录按钮
  await page.click('button:has-text("登录")')

  // 等待跳转到首页
  await page.waitForURL('**/')
}

/**
 * 退出登录
 */
export async function logout(page: Page) {
  // 点击用户头像或菜单
  await page.click('.el-dropdown-link')
  await page.waitForTimeout(300)

  // 点击退出按钮
  await page.click('text=退出登录')

  // 等待跳转到登录页
  await page.waitForURL('**/login')
}

/**
 * 检查是否已登录
 */
export async function isLoggedIn(page: Page): Promise<boolean> {
  const token = await page.evaluate(() => {
    return localStorage.getItem('token')
  })
  return token !== null
}

/**
 * 获取当前登录用户信息
 */
export async function getCurrentUser(page: Page) {
  const userInfo = await page.evaluate(() => {
    const userStr = localStorage.getItem('userInfo')
    return userStr ? JSON.parse(userStr) : null
  })
  return userInfo
}

/**
 * 模拟登录状态（不经过登录页面）
 */
export async function mockLoginState(page: Page, userKey: keyof typeof testUsers = 'admin') {
  const user = testUsers[userKey]
  const token = getTestToken(user.id)

  await page.evaluate((userData) => {
    localStorage.setItem('token', userData.token)
    localStorage.setItem(
      'userInfo',
      JSON.stringify({
        id: userData.user.id,
        username: userData.user.username,
        name: userData.user.name,
        role: userData.user.role,
      })
    )
  }, { token, user })
}

/**
 * 使用 API 登录并获取 token
 */
export async function loginViaAPI(page: Page, username: string = 'admin', password: string = 'admin123'): Promise<string> {
  const response = await page.request.post('/api/auth/login', {
    data: { username, password },
  })

  const data = await response.json()
  return data.data.token
}