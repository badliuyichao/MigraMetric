import { test, expect, type Page } from '@playwright/test'
import { loginAs } from './helpers/auth-helper'
import { shot } from './helpers/screenshot-helper'

/**
 * 第四阶段·工作量评估核心 E2E 用例
 *
 * 依据需求说明文档 §3.3：
 * - §3.3.1 评估流程（步骤一~四）
 * - §3.3.2 工作量评估模型（核心迁移 / 报表 / 客开 / 总工作量）
 *
 * 已有 E2E-FLOW-001 覆盖 happy path 完整 4 步 + 完整评估。
 * 本 spec 聚焦 §3.3.2 公式正确性 + §3.3.1 业务规则 + 异常分支。
 *
 * 策略：通过 API 准备数据（建项目、配模块、传指标），调用 /calculate 拿结果，
 * 用 expect 校验返回值与公式一致。这样既快（每条 < 10s）又精确（数值断言）。
 *
 * 公式（§3.3.2）：
 * - 核心迁移 = Σ(模块基础人天 × 最终加权系数 × 数据量阶梯系数 × 用户数阶梯系数)
 * - 报表工作量 = 报表数量 × 报表系数
 * - 客开工作量 = 客开人天（手动输入）
 * - 总工作量 = 核心 + 报表 + 客开
 */

interface ApiResult {
  coreWorkload: number
  reportWorkload: number
  customDevWorkload: number
  totalWorkload: number
  dataVolumeWeight: number
  userCountWeight: number
  reportCoefficient: number
  moduleWorkloads: { moduleId: number; moduleName: string; moduleWorkload: number }[]
}

/**
 * 跑完 API 断言后跳到统计页等 ECharts 渲染，截一张结果页留作证据。
 * statistics.spec.ts §1 同模式：先等 el-card loading 消失再断言再截。
 */
async function shotStatisticsPage(page: Page, projectId: number, name: string, __specDir: string) {
  await page.goto(`/project/statistics/${projectId}`)
  await page.waitForFunction(
    () => !document.querySelector('.el-card.is-loading'),
    { timeout: 20000 }
  ).catch(() => { /* 容忍超时，ECharts 加载可能略晚 */ })
  await page.waitForTimeout(1500)
  await shot(page, name, __specDir)
}

/**
 * 通过 API 准备一个 DRAFT 项目 + 2 个模块（"模块A":15天, "模块B":20天）。
 * 返回 token、projectId、模块 A/B 的 id。
 */
async function setupProjectWithModules(page: Page): Promise<{ token: string; projectId: number; moduleAId: number; moduleBId: number }> {
  const token = await page.evaluate(() => localStorage.getItem('token'))

  // 取源/目标系统 ID
  const sourceResp = await page.request.get('http://localhost:3000/api/system/types/enabled?category=1', {
    headers: { Authorization: `Bearer ${token}` },
  })
  const sourceId = (await sourceResp.json()).data[0].id
  const targetResp = await page.request.get('http://localhost:3000/api/system/types/enabled?category=2', {
    headers: { Authorization: `Bearer ${token}` },
  })
  const targetId = (await targetResp.json()).data[0].id

  // 建项目
  const projectResp = await page.request.post('http://localhost:3000/api/projects', {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: {
      projectName: `E2E-EV-${Date.now()}`,
      customerName: 'E2E客户',
      sourceSystemId: sourceId,
      targetSystemId: targetId,
      evaluationDate: '2026-06-08',
    },
  })
  const projectId = (await projectResp.json()).data

  // 建 2 个模块：基础人天 15 和 20
  const modAResp = await page.request.post('http://localhost:3000/api/modules', {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: {
      moduleName: `E2E模块A-${Date.now()}`,
      systemId: sourceId,
      category: 'E2E分类',
      baseWorkload: 15,
      defaultWeight: 1.0,
    },
  })
  const moduleAId = (await modAResp.json()).data
  const modBResp = await page.request.post('http://localhost:3000/api/modules', {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: {
      moduleName: `E2E模块B-${Date.now()}`,
      systemId: sourceId,
      category: 'E2E分类',
      baseWorkload: 20,
      defaultWeight: 1.5,
    },
  })
  const moduleBId = (await modBResp.json()).data

  return { token, projectId, moduleAId, moduleBId }
}

/**
 * 给项目评估：保存模块配置 + 保存指标 + 触发计算，返回 WorkloadResult。
 */
async function runEvaluation(
  page: Page,
  token: string,
  projectId: number,
  moduleSelections: { moduleId: number; customWeight?: number }[],
  metrics: {
    tableCount: number
    dataVolume: number
    userCount: number
    reportCount: number
    hasCustomDev: boolean
    customDevModules?: number
    customDevWorkload?: number
  }
): Promise<ApiResult> {
  // 保存模块选择（DTO 字段：weight, checked）
  const modulesPayload = moduleSelections.map(m => ({
    moduleId: m.moduleId,
    weight: m.customWeight ?? 1.0,
    checked: true,
  }))
  await page.request.post(`http://localhost:3000/api/evaluations/${projectId}/modules`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: modulesPayload,
  })

  // 阶梯匹配：前端 match 后会把 ladderId 填到 metricsForm.dataVolumeLadderId / userCountLadderId
  // 后端 EvaluationUpdateDTO 校验这两个必填
  let dataVolumeLadderId: number | null = null
  let userCountLadderId: number | null = null
  if (metrics.dataVolume != null) {
    const dvResp = await page.request.get(`http://localhost:3000/api/ladder/data-volume/match`, {
      headers: { Authorization: `Bearer ${token}` },
      params: { volume: metrics.dataVolume },
    })
    dataVolumeLadderId = (await dvResp.json()).data?.ladderId ?? null
  }
  if (metrics.userCount != null) {
    const ucResp = await page.request.get(`http://localhost:3000/api/ladder/user-count/match`, {
      headers: { Authorization: `Bearer ${token}` },
      params: { count: metrics.userCount },
    })
    userCountLadderId = (await ucResp.json()).data?.ladderId ?? null
  }

  // 保存指标（前端 API 实际是 PUT /evaluations/{id}/indicators）
  await page.request.put(`http://localhost:3000/api/evaluations/${projectId}/indicators`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: {
      tableCount: metrics.tableCount,
      dataVolume: metrics.dataVolume,
      dataVolumeLadderId,
      userCount: metrics.userCount,
      userCountLadderId,
      reportCount: metrics.reportCount,
      hasCustomDev: metrics.hasCustomDev,
      customDevCount: metrics.customDevModules ?? 0,
      customDevWorkload: metrics.customDevWorkload ?? 0,
    },
  })

  // 触发计算
  const calcResp = await page.request.post(`http://localhost:3000/api/evaluations/${projectId}/calculate`, {
    headers: { Authorization: `Bearer ${token}` },
  })
  expect(calcResp.status(), '计算接口应返回 2xx').toBeLessThan(300)
  return (await calcResp.json()).data
}

test.describe('第四阶段·工作量评估核心 E2E', () => {
  const __specDir = 'evaluation'
  // ===========================================================
  // §3.3.2 计算公式：核心迁移工作量
  // ===========================================================

  test('EV-001: 核心迁移工作量 = Σ(基础人天×最终系数×数据量系数×用户数系数)', async ({ page }) => {
    await loginAs(page, 'admin')
    const { token, projectId, moduleAId, moduleBId } = await setupProjectWithModules(page)

    // 模块A: 基础人天15, 自定义系数 1.3
    // 模块B: 基础人天20, 使用默认系数 1.5
    // 数据量/用户数阶梯系数从 result 直接取（dev 库阶梯数据可能在初始化时变）
    const result = await runEvaluation(
      page, token, projectId,
      [
        { moduleId: moduleAId, customWeight: 1.3 },
        // 模块B 必须显式传 customWeight=1.5（后端不读模块默认系数，DTO 必填 weight）
        { moduleId: moduleBId, customWeight: 1.5 },
      ],
      { tableCount: 50, dataVolume: 500, userCount: 600, reportCount: 0, hasCustomDev: false }
    )

    const dvW = result.dataVolumeWeight
    const ucW = result.userCountWeight
    // §3.3.2 计算示例：模块A = 15 × 1.3 × dvW × ucW，模块B = 20 × 1.5 × dvW × ucW
    const expectedA = 15 * 1.3 * dvW * ucW
    const expectedB = 20 * 1.5 * dvW * ucW
    const expectedCore = expectedA + expectedB
    expect(result.coreWorkload).toBeCloseTo(expectedCore, 1)
    // 验证模块明细的 moduleWorkload
    const aDetail = result.moduleWorkloads.find(m => m.moduleId === moduleAId)
    const bDetail = result.moduleWorkloads.find(m => m.moduleId === moduleBId)
    expect(aDetail?.moduleWorkload).toBeCloseTo(expectedA, 1)
    expect(bDetail?.moduleWorkload).toBeCloseTo(expectedB, 1)
    await shotStatisticsPage(page, projectId, '01-core-workload-formula', __specDir)
  })

  // ===========================================================
  // §3.3.2 报表工作量
  // ===========================================================

  test('EV-002: 报表工作量 = 报表数量 × 报表系数', async ({ page }) => {
    await loginAs(page, 'admin')
    const { token, projectId, moduleAId } = await setupProjectWithModules(page)

    // 报表系数：dev 库默认 0.5（修复后 cfg）
    // 报表数 20 → 报表工作量 = 20 × 0.5 = 10
    const result = await runEvaluation(
      page, token, projectId,
      [{ moduleId: moduleAId }],
      { tableCount: 10, dataVolume: 100, userCount: 50, reportCount: 20, hasCustomDev: false }
    )

    expect(result.reportCoefficient).toBeCloseTo(0.5, 2)
    expect(result.reportWorkload).toBeCloseTo(10, 0)
    await shotStatisticsPage(page, projectId, '02-report-workload-formula', __specDir)
  })

  // ===========================================================
  // §3.3.2 客开工作量（§3.3.1 指标6：客开情况分支）
  // ===========================================================

  test('EV-003: 客开=否时客开工作量为 0', async ({ page }) => {
    await loginAs(page, 'admin')
    const { token, projectId, moduleAId } = await setupProjectWithModules(page)
    const result = await runEvaluation(
      page, token, projectId,
      [{ moduleId: moduleAId }],
      { tableCount: 10, dataVolume: 50, userCount: 30, reportCount: 0, hasCustomDev: false }
    )
    expect(result.customDevWorkload).toBe(0)
    await shotStatisticsPage(page, projectId, '03-no-custom-dev', __specDir)
  })

  test('EV-004: 客开=是时客开工作量 = 客开人天输入值', async ({ page }) => {
    await loginAs(page, 'admin')
    const { token, projectId, moduleAId } = await setupProjectWithModules(page)
    const result = await runEvaluation(
      page, token, projectId,
      [{ moduleId: moduleAId }],
      { tableCount: 10, dataVolume: 50, userCount: 30, reportCount: 0, hasCustomDev: true, customDevModules: 2, customDevWorkload: 15 }
    )
    expect(result.customDevWorkload).toBeCloseTo(15, 0)
    // 总 = 核心 + 0 + 15
    const expected = result.coreWorkload + result.reportWorkload + 15
    expect(result.totalWorkload).toBeCloseTo(expected, 0)
    await shotStatisticsPage(page, projectId, '04-with-custom-dev', __specDir)
  })

  // ===========================================================
  // §3.3.1 业务规则：数据清洗需求不计入工作量
  // ===========================================================

  test('EV-005: 数据清洗描述不影响总工作量（仅作参考）', async ({ page }) => {
    await loginAs(page, 'admin')
    const { token, projectId, moduleAId } = await setupProjectWithModules(page)

    // 跑两次：一次清洗=简单，一次清洗=复杂，核心/报表/客开应一致
    const r1 = await runEvaluation(
      page, token, projectId,
      [{ moduleId: moduleAId }],
      { tableCount: 10, dataVolume: 50, userCount: 30, reportCount: 0, hasCustomDev: false }
    )
    // 用 API 追加清洗描述（不影响 calculate 公式）
    await page.request.put(`http://localhost:3000/api/evaluations/${projectId}/indicators`, {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: {
        tableCount: 10, dataVolume: 50, userCount: 30, reportCount: 0, hasCustomDev: false,
        dataCleanDescription: '复杂清洗', dataCleanComplexity: 3,
      },
    })
    const r2 = await page.request.post(`http://localhost:3000/api/evaluations/${projectId}/calculate`, {
      headers: { Authorization: `Bearer ${token}` },
    }).then(r => r.json())

    expect(r2.data.coreWorkload).toBeCloseTo(r1.coreWorkload, 0)
    expect(r2.data.totalWorkload).toBeCloseTo(r1.totalWorkload, 0)
    await shotStatisticsPage(page, projectId, '05-data-clean-no-effect', __specDir)
  })
})
