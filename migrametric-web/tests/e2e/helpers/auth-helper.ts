import { Page, expect } from '@playwright/test'
import { testUsers } from '../../fixtures/users'

/**
 * 通过 /api/auth/login 拿到 token，注入到 localStorage，
 * 同时用 /auth/info 拉一次 userInfo 一起注入（permission.ts 守卫
 * 会因 !userInfo 调 getUserInfo，401 又会触发 handleUnauthorized，
 * 导致 admin 也跳回登录页）。
 */
export async function loginAs(page: Page, userKey: 'admin' | 'user' | 'disabledUser' = 'admin') {
  const user = testUsers[userKey]
  const resp = await page.request.post('/api/auth/login', {
    data: { username: user.username, password: user.password },
    failOnStatusCode: false,
  })
  if (resp.status() !== 200) {
    throw new Error(`登录失败: ${user.username} status=${resp.status()}`)
  }
  const body = await resp.json()
  const token: string = body.data?.token
  if (!token) {
    throw new Error(`登录响应无 token: ${JSON.stringify(body)}`)
  }
  // 拿 userInfo
  const infoResp = await page.request.get('/api/auth/info', {
    headers: { Authorization: `Bearer ${token}` },
  })
  const userInfo = infoResp.status() === 200 ? (await infoResp.json()).data : null
  // 注入到 localStorage
  await page.goto('/login')
  await page.evaluate(({ t, info }) => {
    localStorage.setItem('token', t)
    if (info) localStorage.setItem('userInfo', JSON.stringify(info))
  }, { t: token, info: userInfo })
  await page.goto('/dashboard')
  await expect(page).toHaveURL(/\/dashboard/)
}
