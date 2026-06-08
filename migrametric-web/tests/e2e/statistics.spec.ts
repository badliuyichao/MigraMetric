import { test, expect, type Page } from '@playwright/test'
import { loginAs } from './helpers/auth-helper'
import { shot } from './helpers/screenshot-helper'

/**
 * 第五阶段·工作量统计展示 E2E 用例
 *
 * 依据需求说明文档 §3.4：
 * - §3.4.1 多维度统计分析（4 维度：模块/类型/复杂度/总体）
 * - §3.4.2 可视化展示（饼图/柱状图/雷达图/仪表盘/明细表）
 *
 * 覆盖策略：4 条核心用例
 * - STA-001 顶部总览：总工作量/工期/模块数
 * - STA-002 工作量类型分布（饼图 legend + 占比百分比）
 * - STA-003 评估指标概览（数据量/用户数/报表数/客开）
 * - STA-004 风险提示：极高数据量触发风险规则
 *
 * 不覆盖（设计文档 P1 范围，复杂度高）：
 * - ECharts canvas 渲染细节（用 legend 文本间接验证）
 * - 导出报告（§3.5，需真实下载文件，依赖 excel/pdf 库，超出 E2E 范围）
 * - 雷达图 5 个维度的精确数值（ECharts 内部数据，UI 上只显式展示 4 个）
 */

const UNIQUE = () => `E2E-STA-${Date.now()}`
const __specDir = 'statistics'

async function setupEvaluatedProject(page: Page, name: string) {
  const token = await page.evaluate(() => localStorage.getItem('token'))

  // 取源/目标系统
  const sourceId = (await (await page.request.get('http://localhost:3000/api/system/types/enabled?category=1', {
    headers: { Authorization: `Bearer ${token}` },
  })).json()).data[0].id
  const targetId = (await (await page.request.get('http://localhost:3000/api/system/types/enabled?category=2', {
    headers: { Authorization: `Bearer ${token}` },
  })).json()).data[0].id

  // 建项目
  const projectResp = await page.request.post('http://localhost:3000/api/projects', {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: {
      projectName: name, customerName: 'E2E客户',
      sourceSystemId: sourceId, targetSystemId: targetId, evaluationDate: '2026-06-08',
    },
  })
  const projectId = (await projectResp.json()).data

  // 建 2 个模块
  const moduleAId = (await (await page.request.post('http://localhost:3000/api/modules', {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: { moduleName: `STA-A-${Date.now()}`, systemId: sourceId, category: 'STA分类', baseWorkload: 10, defaultWeight: 1.0 },
  })).json()).data
  const moduleBId = (await (await page.request.post('http://localhost:3000/api/modules', {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: { moduleName: `STA-B-${Date.now()}`, systemId: sourceId, category: 'STA分类', baseWorkload: 20, defaultWeight: 1.5 },
  })).json()).data

  // 选模块
  await page.request.post(`http://localhost:3000/api/evaluations/${projectId}/modules`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: [
      { moduleId: moduleAId, weight: 1.0, checked: true },
      { moduleId: moduleBId, weight: 1.5, checked: true },
    ],
  })

  // 阶梯匹配
  const dvMatch = await (await page.request.get('http://localhost:3000/api/ladder/data-volume/match', {
    headers: { Authorization: `Bearer ${token}` },
    params: { volume: 100 },
  })).json()
  const ucMatch = await (await page.request.get('http://localhost:3000/api/ladder/user-count/match', {
    headers: { Authorization: `Bearer ${token}` },
    params: { count: 100 },
  })).json()

  // 保存指标
  await page.request.put(`http://localhost:3000/api/evaluations/${projectId}/indicators`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: {
      tableCount: 20,
      dataVolume: 100,
      dataVolumeLadderId: dvMatch.data.ladderId,
      userCount: 100,
      userCountLadderId: ucMatch.data.ladderId,
      reportCount: 5,
      hasCustomDev: false,
      customDevCount: 0,
      customDevWorkload: 0,
    },
  })

  // 触发计算（让后端有结果）
  const calc = await page.request.post(`http://localhost:3000/api/evaluations/${projectId}/calculate`, {
    headers: { Authorization: `Bearer ${token}` },
  })
  return { token, projectId, moduleAId, moduleBId, totalWorkload: (await calc.json()).data.totalWorkload }
}

test.describe('第五阶段·统计展示 E2E', () => {
  // ===========================================================
  // §3.4.1 维度 4：总体汇总
  // ===========================================================

  test('STA-001: 顶部统计展示总工作量/工期/模块数', async ({ page }) => {
    await loginAs(page, 'admin')
    const { projectId, totalWorkload } = await setupEvaluatedProject(page, UNIQUE())

    // 直接调 statistics 接口（避免 echarts 渲染等待）
    const token = await page.evaluate(() => localStorage.getItem('token'))
    const statsResp = await page.request.get(`http://localhost:3000/api/statistics/${projectId}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    expect(statsResp.status()).toBe(200)
    const stats = (await statsResp.json()).data

    // §3.4.1 维度 4 总体汇总：总工作量（人天）、预估工期（人月）、关键指标
    expect(stats.totalWorkload).toBeCloseTo(totalWorkload, 1)
    // 预估工期 = 总工作量 / 22（按 22 人天/人月）
    const expectedMonths = stats.totalWorkload / 22
    expect(stats.estimatedMonths).toBeCloseTo(expectedMonths, 1)
    // 关键指标概览：已选模块数 = 2
    expect(stats.evaluationOverview?.moduleCount).toBe(2)

    // 同时校验 UI 能加载到统计页（el-card 出现）
    await page.goto(`/project/statistics/${projectId}`)
    // 等待 loading 结束（el-card v-loading 包裹，子元素在 loading=true 时不可见）
    await page.waitForFunction(
      () => !document.querySelector('.el-card.is-loading'),
      { timeout: 20000 }
    ).catch(() => { /* 容忍超时，继续断言 */ })
    await expect(page.locator('[data-testid="section-total-workload"]')).toBeVisible({ timeout: 15000 })
    await expect(page.locator('[data-testid="section-total-workload"]')).toContainText('人天')
    await shot(page, '01-total-workload', __specDir)
  })

  // ===========================================================
  // §3.4.1 维度 2：按工作量类型统计
  // ===========================================================

  test('STA-002: 工作量类型分布饼图（核心/报表/客开占比）', async ({ page }) => {
    await loginAs(page, 'admin')
    const { projectId } = await setupEvaluatedProject(page, UNIQUE())

    const token = await page.evaluate(() => localStorage.getItem('token'))
    const statsResp = await page.request.get(`http://localhost:3000/api/statistics/${projectId}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    const stats = (await statsResp.json()).data

    // workloadTypeDistribution 至少 3 个类型（核心/报表/客开）
    expect(Array.isArray(stats.workloadTypeDistribution)).toBe(true)
    const types = stats.workloadTypeDistribution.map((d: { type: string }) => d.type)
    expect(types.length).toBeGreaterThanOrEqual(3)
    // 占比百分比合计应接近 100
    const totalPercent = stats.workloadTypeDistribution.reduce((sum: number, d: { percentage: number }) => sum + d.percentage, 0)
    expect(totalPercent).toBeCloseTo(100, 0)

    // UI 验证：饼图卡片 + legend
    await page.goto(`/project/statistics/${projectId}`)
    await expect(page.locator('[data-testid="card-workload-type-pie"]')).toBeVisible({ timeout: 15000 })
    await expect(page.locator('[data-testid="legend-pie"]')).toBeVisible()
    await shot(page, '02-workload-type-pie', __specDir)
  })

  // ===========================================================
  // §3.4.1 维度 1：按模块统计
  // ===========================================================

  test('STA-003: 评估指标概览（数据量/用户数/报表数/客开）', async ({ page }) => {
    await loginAs(page, 'admin')
    const { projectId } = await setupEvaluatedProject(page, UNIQUE())

    const token = await page.evaluate(() => localStorage.getItem('token'))
    const statsResp = await page.request.get(`http://localhost:3000/api/statistics/${projectId}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    const stats = (await statsResp.json()).data

    // evaluationOverview 关键指标
    expect(stats.evaluationOverview?.moduleCount).toBe(2)
    expect(stats.evaluationOverview?.dataVolume).toBe(100)
    expect(stats.evaluationOverview?.userCount).toBe(100)
    expect(stats.evaluationOverview?.reportCount).toBe(5)
    expect(stats.evaluationOverview?.hasCustomDev).toBe(false)
    // 阶梯名称应被填充（dev 库初始阶梯有匹配项）
    expect(stats.evaluationOverview?.dataVolumeLadder).toBeTruthy()
    expect(stats.evaluationOverview?.userCountLadder).toBeTruthy()

    // UI 验证：评估指标概览卡片
    await page.goto(`/project/statistics/${projectId}`)
    await expect(page.locator('[data-testid="section-overview"]')).toBeVisible({ timeout: 15000 })
    await expect(page.locator('[data-testid="section-overview"]')).toContainText('100 万条')
    await expect(page.locator('[data-testid="section-overview"]')).toContainText('100 人')
    await expect(page.locator('[data-testid="section-overview"]')).toContainText('5 个')
    await expect(page.locator('[data-testid="section-overview"]')).toContainText('无客开')
    await shot(page, '03-evaluation-overview', __specDir)
  })

  // ===========================================================
  // §3.4.1 维度 3：按复杂度统计（高数据量触发风险）
  // ===========================================================

  test('STA-004: 极高数据量触发风险提示', async ({ page }) => {
    await loginAs(page, 'admin')
    const name = UNIQUE()
    const { token, projectId } = await setupEvaluatedProject(page, name)

    // 重新保存指标：把数据量改成超大值（9999 万条）
    const dvMatch = await (await page.request.get('http://localhost:3000/api/ladder/data-volume/match', {
      headers: { Authorization: `Bearer ${token}` },
      params: { volume: 9999 },
    })).json()
    const ucMatch = await (await page.request.get('http://localhost:3000/api/ladder/user-count/match', {
      headers: { Authorization: `Bearer ${token}` },
      params: { count: 100 },
    })).json()
    await page.request.put(`http://localhost:3000/api/evaluations/${projectId}/indicators`, {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: {
        tableCount: 20, dataVolume: 9999, dataVolumeLadderId: dvMatch.data.ladderId,
        userCount: 100, userCountLadderId: ucMatch.data.ladderId,
        reportCount: 5, hasCustomDev: false, customDevCount: 0, customDevWorkload: 0,
      },
    })
    await page.request.post(`http://localhost:3000/api/evaluations/${projectId}/calculate`, {
      headers: { Authorization: `Bearer ${token}` },
    })

    // 校验 riskWarnings 至少 1 条
    const statsResp = await page.request.get(`http://localhost:3000/api/statistics/${projectId}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    const stats = (await statsResp.json()).data
    expect(Array.isArray(stats.riskWarnings)).toBe(true)
    expect(stats.riskWarnings.length).toBeGreaterThanOrEqual(1)

    // UI 验证：风险卡片至少一条 alert
    await page.goto(`/project/statistics/${projectId}`)
    await expect(page.locator('[data-testid="card-risk-warnings"]')).toBeVisible({ timeout: 15000 })
    const riskItems = page.locator('[data-testid^="risk-item-"]')
    await expect(riskItems.first()).toBeVisible({ timeout: 10000 })
    await shot(page, '04-risk-warnings', __specDir)
  })
})
