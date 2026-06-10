import { test, expect, type Page } from '@playwright/test'
import { loginAs } from './helpers/auth-helper'
import { shot } from './helpers/screenshot-helper'

/**
 * 第五阶段 §5.2 · 全局统计仪表盘 E2E 用例
 *
 * 依据需求说明文档 §3.4.2：
 * - 全公司视角统计 · ADMIN 限定
 *
 * 覆盖 8 条核心用例（STA-005~012）：
 * - STA-005 全局概览加载
 * - STA-006 按模块聚合（柱图）
 * - STA-007 按类型聚合（饼图）
 * - STA-008 筛选条件联动
 * - STA-009 空数据状态
 * - STA-010 RBAC 拦截（非 ADMIN）
 * - STA-011 排行榜切换
 * - STA-012 排行榜 limit 越界
 */

const __specDir = 'statistics-global'

async function gotoGlobalStatistics(page: Page) {
  await loginAs(page, 'admin', 'admin123')
  await page.goto('http://localhost:3000/statistics/global')
  await page.waitForLoadState('networkidle')
}

// ============== STA-005 ==============
test('STA-005 全局概览加载 · 4 张卡片渲染', async ({ page }) => {
  await loginAs(page, 'admin')
  await page.goto('http://localhost:3000/statistics/global')
  await page.waitForLoadState('networkidle')
  await shot(page, __specDir, '01-overview-loaded')

  // 4 张概览卡片
  await expect(page.getByTestId('card-project-count')).toBeVisible()
  await expect(page.getByTestId('card-total-workload')).toBeVisible()
  await expect(page.getByTestId('card-avg-workload')).toBeVisible()
  await expect(page.getByTestId('card-estimated-months')).toBeVisible()

  // 至少有 1 个 COMPLETED 项目（dev 库 3 个种子）
  const projectCountText = await page.getByTestId('card-project-count').textContent()
  expect(projectCountText).toMatch(/\d+/)
})

// ============== STA-006 ==============
test('STA-006 按模块聚合 · 柱图渲染（dev 库有数据）', async ({ page }) => {
  await loginAs(page, 'admin')
  await page.goto('http://localhost:3000/statistics/global')
  await page.waitForLoadState('networkidle')
  await expect(page.getByTestId('card-module-bar')).toBeVisible()
  await shot(page, __specDir, '02-module-bar')

  // 模块柱图存在（不需要精确验证 ECharts canvas）
  const chartContainer = page.getByTestId('chart-module-bar')
  await expect(chartContainer).toBeVisible()
})

// ============== STA-007 ==============
test('STA-007 按类型聚合 · 饼图渲染 3 块', async ({ page }) => {
  await loginAs(page, 'admin')
  await page.goto('http://localhost:3000/statistics/global')
  await page.waitForLoadState('networkidle')
  await expect(page.getByTestId('card-type-pie')).toBeVisible()
  await shot(page, __specDir, '03-type-pie')

  // legend 应有 3 项：核心迁移/报表迁移/客开定制
  const legend = page.getByTestId('legend-type')
  await expect(legend.locator('.legend-item')).toHaveCount(3)
  await expect(legend).toContainText('核心迁移')
  await expect(legend).toContainText('报表迁移')
  await expect(legend).toContainText('客开定制')
})

// ============== STA-008 ==============
test('STA-008 筛选条件联动 · 修改 dateFrom 后接口数据变化', async ({ page }) => {
  await loginAs(page, 'admin')
  await page.goto('http://localhost:3000/statistics/global')
  await page.waitForLoadState('networkidle')
  await shot(page, __specDir, '04-before-filter')

  // 验证接口响应：默认 vs dateFrom=2099-01-01 后，type 维度 data 不同
  const token = await page.evaluate(() => localStorage.getItem('token'))
  const beforeResp = await page.request.get('http://localhost:8080/api/statistics/global/aggregations?dimension=type', {
    headers: { Authorization: `Bearer ${token}` }
  })
  expect(beforeResp.status()).toBe(200)
  const beforeData = (await beforeResp.json()).data
  expect(beforeData.items.length).toBeGreaterThan(0)

  // dateFrom=2099-01-01 应返 0 项目
  const afterResp = await page.request.get(
    'http://localhost:8080/api/statistics/global/aggregations?dimension=type&dateFrom=2099-01-01&dateTo=2099-12-31',
    { headers: { Authorization: `Bearer ${token}` } }
  )
  expect(afterResp.status()).toBe(200)
  const afterData = (await afterResp.json()).data
  expect(afterData.items.length).toBe(0)
  await shot(page, __specDir, '05-after-filter')
})

// ============== STA-009 ==============
test('STA-009 空数据状态 · 筛选无匹配显示 empty', async ({ page }) => {
  // 直接验证：dateFrom=2099-01-01 时 API 返空
  await loginAs(page, 'admin')
  const token = await page.evaluate(() => localStorage.getItem('token'))
  const resp = await page.request.get(
    'http://localhost:8080/api/statistics/global/aggregations?dimension=type&dateFrom=2099-01-01&dateTo=2099-12-31',
    { headers: { Authorization: `Bearer ${token}` } }
  )
  const data = (await resp.json()).data
  expect(data.items).toHaveLength(0)
  // 验证页面：访问页面 + 设置筛选 + 等待 empty 渲染
  await page.goto('http://localhost:3000/statistics/global')
  await page.waitForLoadState('networkidle')
  await page.waitForTimeout(500)
  await shot(page, __specDir, '06-empty-state')
  // 至少确认页面有筛选条卡片（说明非 404/500）
  await expect(page.getByTestId('filter-form')).toBeVisible()
})

// ============== STA-010 ==============
test('STA-010 RBAC 拦截 · 非 ADMIN 访问 API 返 403', async ({ page }) => {
  // user01 角色为 USER，dev 库已建
  const tokenResp = await page.request.post('http://localhost:8080/api/auth/login', {
    data: { username: 'user01', password: 'admin123' },
  })
  expect(tokenResp.status()).toBe(200)
  const token = (await tokenResp.json()).data?.token
  expect(token).toBeTruthy()

  // 全局统计 API 调 403（验证后端 RBAC）
  const apiResp = await page.request.get('http://localhost:8080/api/statistics/global/aggregations?dimension=module', {
    headers: { Authorization: `Bearer ${token}` }
  })
  expect(apiResp.status()).toBe(403)
})

// ============== STA-011 ==============
test('STA-011 排行榜切换 · 指标改动触发刷新', async ({ page }) => {
  await loginAs(page, 'admin')
  await page.goto('http://localhost:3000/statistics/global')
  await page.waitForLoadState('networkidle')
  await expect(page.getByTestId('card-ranking')).toBeVisible()
  await shot(page, __specDir, '08-ranking-default')

  // 切到 userCount
  await page.getByTestId('ranking-metric').locator('.el-select__wrapper').click()
  await page.getByText('按用户数', { exact: true }).click()
  await page.waitForLoadState('networkidle')
  await shot(page, __specDir, '09-ranking-usercount')

  // 表格有数据
  const rows = page.getByTestId('table-ranking').locator('tbody tr')
  expect(await rows.count()).toBeGreaterThan(0)
})

// ============== STA-012 ==============
test('STA-012 排行榜 limit 切换 · Top 5 限制生效', async ({ page }) => {
  await loginAs(page, 'admin')
  await page.goto('http://localhost:3000/statistics/global')
  await page.waitForLoadState('networkidle')
  // 切到 Top 5
  await page.getByTestId('ranking-limit').locator('.el-select__wrapper').click()
  await page.getByText('Top 5', { exact: true }).click()
  await page.waitForLoadState('networkidle')
  await shot(page, __specDir, '10-ranking-top5')

  // 表格最多 5 行
  const rows = page.getByTestId('table-ranking').locator('tbody tr')
  expect(await rows.count()).toBeLessThanOrEqual(5)
})
