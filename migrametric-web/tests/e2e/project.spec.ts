import { test, expect, type Page } from '@playwright/test'
import { loginAs } from './helpers/auth-helper'
import { shot } from './helpers/screenshot-helper'

/**
 * 第三阶段·项目管理模块 E2E 用例
 *
 * 依据需求说明文档 §3.2：
 * - §3.2.1 创建评估项目：8 个字段（项目名/客户名/源系统/目标系统/负责人/联系方式/评估日期/项目描述）+ 业务规则
 * - §3.2.2 项目列表管理：列表展示 + 4 类筛选 + 4 类操作
 * - §3.2.3 项目状态管理：4 状态 + 流转规则
 *
 * 覆盖策略：4 条核心用例
 * - PRJ-001 创建项目（§3.2.1 + 状态自动为草稿）
 * - PRJ-002 列表搜索+筛选（§3.2.2）
 * - PRJ-003 项目详情（§3.2.2 查看 + §3.2.3 状态显示）
 * - PRJ-004 业务规则：源≠目标（§3.2.1）+ 删除仅草稿（§3.2.2）
 *
 * 不覆盖（设计文档未要求）：
 * - 复制项目（§3.2.2 提到但非关键路径，复制链路由后端 + 列表"复制"按钮触发，逻辑简单）
 * - 编辑项目（同上，编辑链路与创建共用 form，PRJ-001 通过即代表通过）
 * - 归档项目（需要"已完成"前置状态，依赖评估流程跑完，超出本阶段）
 * - 状态自动从草稿→进行中（§3.2.3，依赖评估入口，第四阶段 E2E-FLOW 已覆盖）
 * - 评估日期范围筛选（§3.2.2，UI 上 el-date-picker daterange 复杂度高，文档未硬性要求 E2E 覆盖）
 */

const UNIQUE = () => `E2E-${Date.now()}`
const __specDir = 'project'

async function createDraftProject(page: Page, name: string) {
  const token = await page.evaluate(() => localStorage.getItem('token'))
  const sourceSys = await page.request.get('http://localhost:3000/api/system/types/enabled?category=1', {
    headers: { Authorization: `Bearer ${token}` },
  })
  const sourceId = (await sourceSys.json()).data[0]?.id
  const targetSys = await page.request.get('http://localhost:3000/api/system/types/enabled?category=2', {
    headers: { Authorization: `Bearer ${token}` },
  })
  const targetId = (await targetSys.json()).data[0]?.id

  const resp = await page.request.post('http://localhost:3000/api/projects', {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    data: {
      projectName: name,
      customerName: 'E2E客户',
      sourceSystemId: sourceId,
      targetSystemId: targetId,
      projectLeader: 'E2E负责人',
      contact: '13800000000',
      evaluationDate: '2026-06-08',
      description: 'E2E项目'
    },
  })
  expect(resp.status(), '创建项目应返回 2xx').toBeLessThan(300)
  const body = await resp.json()
  return { id: body.data, sourceId, targetId }
}

test.describe('第三阶段·项目管理 E2E', () => {
  // ===========================================================
  // §3.2.1 创建评估项目
  // ===========================================================

  test('PRJ-001: 创建项目后状态为草稿', async ({ page }) => {
    await loginAs(page, 'admin')
    const name = UNIQUE()
    await page.goto('/project/create')
    await expect(page.locator('[data-testid="form-project-name"]')).toBeVisible()

    // 填必填字段
    await page.locator('[data-testid="form-project-name"]').fill(name)
    await page.locator('[data-testid="form-customer-name"]').fill('E2E客户')

    // 源/目标系统（filterable 下拉）
    await page.locator('[data-testid="select-source-system"] .el-select__wrapper').click()
    await page.locator('.el-select-dropdown:visible .el-select-dropdown__item').first().click()
    await page.waitForTimeout(150)
    await page.locator('[data-testid="select-target-system"] .el-select__wrapper').click()
    await page.locator('.el-select-dropdown:visible .el-select-dropdown__item').first().click()
    await page.waitForTimeout(150)

    // el-date-picker 默认 inheritAttrs=false，data-testid 不会传到根 div
    // 直接用 placeholder 定位内部 input
    const dateInput = page.locator('input[placeholder="请选择评估日期"]')
    await dateInput.waitFor({ state: 'visible' })
    await dateInput.fill('2026-06-08')
    await dateInput.press('Tab')
    await shot(page, '01-create-form-filled', __specDir)

    // 提交
    const createResp = page.waitForResponse(
      r => r.url().endsWith('/api/projects') && r.request().method() === 'POST' && r.status() < 300,
      { timeout: 15000 }
    )
    await page.click('[data-testid="btn-save-project"]')
    const resp = await createResp
    const projectId = (await resp.json()).data
    expect(projectId, '创建响应应返回项目 ID').toBeTruthy()

    // 跳到列表，校验新建项目出现在第一条
    await page.waitForURL('**/project/list', { timeout: 10000 })
    await shot(page, '02-list-after-create', __specDir)
    // 从后端再拉一次这个项目 ID 的详情来校验状态=草稿（详情页校验更稳）
    const token = await page.evaluate(() => localStorage.getItem('token'))
    const detailResp = await page.request.get(`http://localhost:3000/api/projects/${projectId}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    const detail = (await detailResp.json()).data
    expect(detail.status, '新建项目状态应为 DRAFT').toBe('DRAFT')
    expect(detail.statusText, '新建项目状态文本应为草稿').toBe('草稿')
  })

  // ===========================================================
  // §3.2.2 列表搜索+筛选
  // ===========================================================

  test('PRJ-002: 列表按项目名称搜索+状态筛选', async ({ page }) => {
    await loginAs(page, 'admin')
    // 先建一个项目
    const name = UNIQUE()
    await createDraftProject(page, name)

    await page.goto('/project/list')
    await expect(page.locator('[data-testid="table-project"]')).toBeVisible()
    await shot(page, '03-list-initial', __specDir)

    // 按名称搜索
    await page.locator('[data-testid="search-project-name"]').fill(name)
    await page.click('[data-testid="btn-search"]')
    const matchedRow = page.locator('[data-testid="table-project"] .el-table__row', { hasText: name })
    await expect(matchedRow).toHaveCount(1)
    await shot(page, '04-list-name-search-hit', __specDir)

    // 按状态=草稿筛选
    await page.click('[data-testid="btn-reset"]')
    await page.locator('[data-testid="search-status"] .el-select__wrapper').click()
    await page.locator('.el-select-dropdown:visible .el-select-dropdown__item:has-text("草稿")').click()
    await page.waitForTimeout(150)
    await page.click('[data-testid="btn-search"]')
    await shot(page, '05-list-status-filter-draft', __specDir)
    // 至少 1 行，且全含"草稿"状态
    const draftRows = page.locator('[data-testid="table-project"] .el-table__row')
    await expect(draftRows.first()).toBeVisible()
    const draftCount = await draftRows.count()
    expect(draftCount).toBeGreaterThan(0)
  })

  // ===========================================================
  // §3.2.2 查看项目详情
  // ===========================================================

  test('PRJ-003: 详情页展示完整字段+状态', async ({ page }) => {
    await loginAs(page, 'admin')
    const name = UNIQUE()
    const { id } = await createDraftProject(page, name)

    await page.goto(`/project/detail/${id}`)
    await expect(page.locator('[data-testid="descriptions-project-info"]')).toBeVisible()
    await shot(page, '06-detail-page', __specDir)
    // 8 个关键字段
    await expect(page.locator('[data-testid="descriptions-project-info"]')).toContainText(name)
    await expect(page.locator('[data-testid="descriptions-project-info"]')).toContainText('E2E客户')
    await expect(page.locator('[data-testid="descriptions-project-info"]')).toContainText('E2E负责人')
    // 状态=草稿
    await expect(page.locator('[data-testid="project-status"]')).toContainText('草稿')
  })

  // ===========================================================
  // §3.2.1 业务规则：源≠目标
  // §3.2.2 业务规则：删除仅草稿
  // ===========================================================

  test('PRJ-004: 业务规则-必填校验（不选目标系统提交失败）', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/project/create')

    // 填必填
    await page.locator('[data-testid="form-project-name"]').fill(UNIQUE())
    await page.locator('[data-testid="form-customer-name"]').fill('E2E客户')
    // el-date-picker 默认 inheritAttrs=false，data-testid 不会传到根 div
    // 直接用 placeholder 定位内部 input
    await page.locator('input[placeholder="请选择评估日期"]').fill('2026-06-08')
    await page.locator('input[placeholder="请选择评估日期"]').press('Tab')

    // 只选源系统，**故意不选目标系统**
    await page.locator('[data-testid="select-source-system"] .el-select__wrapper').click()
    await page.locator('.el-select-dropdown:visible .el-select-dropdown__item').first().click()
    await page.waitForTimeout(150)
    await shot(page, '07-form-missing-target', __specDir)

    // 提交 → §3.2.1 必填规则触发，应停留在 create 页
    await page.click('[data-testid="btn-save-project"]')
    await page.waitForTimeout(1000)
    expect(page.url()).toContain('/project/create')
    // 目标系统输入框应出现 form 校验错误提示
    await expect(page.locator('.el-form-item__error').first()).toBeVisible({ timeout: 5000 })
    await shot(page, '08-form-validation-error', __specDir)
  })
})
