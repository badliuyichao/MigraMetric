import { test, expect, type Page } from '@playwright/test'
import { testUsers } from '../../fixtures/users'
import { callApi, pickFromSelect, fillInputNumber, byTestId } from '../helpers/form-helpers'
import { shot } from '../helpers/screenshot-helper'

/**
 * E2E-FLOW-001: P0 核心流程
 * 路径：登录 → 创建项目 → 四步评估 → 查看统计报告
 *
 * 截图风格：每个关键节点全页截图，保留到 Playwright 自带的
 * test-results/ 目录（失败时 trace/截图也会被 Playwright 收集）。
 */

const PROJECT_NAME = `E2E-P0-${Date.now()}`
const CUSTOMER_NAME = 'E2E测试客户'
const PROJECT_LEADER = '测试员'
const CONTACT = '13800138000'
const EVAL_DATE = '2026-06-08'
const __specDir = 'full-flow'

let capturedProjectId: number | null = null

test.describe('P0 核心流程 E2E', () => {
  test('E2E-FLOW-001: 登录 → 创建项目 → 四步评估 → 统计报告', async ({ page }) => {
    test.setTimeout(120_000)

    // 捕获浏览器 console 用于 E2E 诊断
    page.on('console', msg => {
      const text = msg.text()
      if (text.includes('E2E-DIAG') || msg.type() === 'error') {
        console.log(`[browser-${msg.type()}]`, text)
      }
    })
    page.on('pageerror', err => console.log('[browser-pageerror]', err.message))

    // ============================================================
    // 阶段 1：登录
    // ============================================================
    await test.step('登录', async () => {
      await page.goto('/login')
      await byTestId(page, 'username').fill(testUsers.admin.username)
      await byTestId(page, 'password').fill(testUsers.admin.password)
      await shot(page, '01-login-filled', __specDir)

      const loginResp = page.waitForResponse(
        r => r.url().includes('/auth/login') && r.status() === 200,
        { timeout: 15000 }
      )
      await byTestId(page, 'login-button').click()
      await loginResp
      await page.waitForURL('**/dashboard', { timeout: 10000 })
      await shot(page, '02-dashboard', __specDir)
    })

    // ============================================================
    // 阶段 2：创建项目
    // ============================================================
    let projectId: number
    await test.step('创建项目', async () => {
      await page.goto('/project/list')
      await shot(page, '03-project-list', __specDir)

      await byTestId(page, 'btn-create-project').click()
      await page.waitForURL('**/project/create')
      await page.waitForLoadState('networkidle')

      // 基础字段
      await page.getByPlaceholder('请输入项目名称').fill(PROJECT_NAME)
      await page.getByPlaceholder('请输入客户名称').fill(CUSTOMER_NAME)
      await page.getByPlaceholder('请输入项目负责人').fill(PROJECT_LEADER)
      await page.getByPlaceholder('请输入联系方式').fill(CONTACT)
      await page.locator('input[placeholder="请选择评估日期"]').fill(EVAL_DATE)
      await page.locator('input[placeholder="请选择评估日期"]').press('Tab')
      await page.getByPlaceholder('请输入项目描述').fill('E2E 自动化测试项目')

      // 源/目标系统：filterable 下拉
      await pickFromSelect(page, 'select-source-system', 0)
      await pickFromSelect(page, 'select-target-system', 0)
      await shot(page, '04-project-form-filled', __specDir)

      // 捕获创建响应拿项目 ID
      const createResp = page.waitForResponse(
        r => r.url().endsWith('/api/projects') && r.request().method() === 'POST',
        { timeout: 15000 }
      )
      await byTestId(page, 'btn-save-project').click()
      const resp = await createResp
      expect(resp.status(), '创建项目应返回 2xx').toBeLessThan(300)
      const body = await resp.json()
      capturedProjectId = body.data
      expect(capturedProjectId, '创建项目应返回 ID').toBeTruthy()
      projectId = capturedProjectId!

      await page.waitForURL('**/project/list', { timeout: 10000 })
      await shot(page, '05-project-list-after-create', __specDir)
    })

    // ============================================================
    // 阶段 3：四步评估
    // ============================================================
    await test.step('进入评估 Step 1：系统确认', async () => {
      await page.goto(`/project/detail/${projectId}`)
      await byTestId(page, 'btn-start-evaluation').click()
      await page.waitForURL(`**/project/evaluate/${projectId}`)
      await page.waitForLoadState('networkidle')
      await shot(page, '06-eval-step1-system-confirmation', __specDir)
    })

    await test.step('Step 1 → Step 2：选择模块', async () => {
      await byTestId(page, 'btn-next-step').click()
      // 等模块列表接口返回
      await page.waitForResponse(
        r => r.url().includes('/evaluations/') && r.url().includes('/modules') && r.status() === 200,
        { timeout: 15000 }
      ).catch(() => { /* ignore — list 接口形态可能不是该 URL */ })
      await page.waitForSelector('[data-testid="module-table"] .el-table__row', { timeout: 15000 })
      await shot(page, '07-eval-step2-modules-loaded', __specDir)

      // 选前 3 个模块
      const checkboxes = page.locator('[data-testid="module-table"] .el-table__body-wrapper .el-checkbox')
      const cbCount = await checkboxes.count()
      expect(cbCount, '模块表至少应有 1 行').toBeGreaterThan(0)
      const selectCount = Math.min(3, cbCount)
      for (let i = 0; i < selectCount; i++) {
        await checkboxes.nth(i).click()
        await page.waitForTimeout(150)
      }
      await shot(page, '08-eval-step2-modules-selected', __specDir)

      // 保存模块配置（点下一步触发）
      const saveModulesResp = page.waitForResponse(
        r => r.url().includes('/evaluations/') && r.url().includes('/modules') &&
             r.request().method() === 'POST' && r.status() === 200,
        { timeout: 15000 }
      )
      await byTestId(page, 'btn-next-step').click()
      await saveModulesResp
    })

    await test.step('Step 3：填写指标', async () => {
      // 等步骤切到 step 3：btn-calculate 只在 currentStep===3 时渲染，是 step 3 落地的最强信号
      await byTestId(page, 'btn-calculate').waitFor({ state: 'visible', timeout: 15000 })
      const tableCountInput = page.locator('input[placeholder="请输入需要迁移的数据库表数量"]')
      await tableCountInput.waitFor({ state: 'visible', timeout: 15000 })
      await shot(page, '09-eval-step3-indicators', __specDir)

      await fillInputNumber(page, '请输入需要迁移的数据库表数量', 50)
      await fillInputNumber(page, '请输入数据总量', 500)
      // 等阶梯匹配 tag 出现（数据量 / 用户数都有 onChange 异步匹配）
      await page.waitForTimeout(800)
      await fillInputNumber(page, '请输入系统用户总数', 3000)
      await page.waitForTimeout(800)
      await fillInputNumber(page, '请输入需要迁移的报表数量', 20)
      await shot(page, '10-eval-step3-indicators-filled', __specDir)
    })

    await test.step('Step 4：计算工作量', async () => {
      const calcResp = page.waitForResponse(
        r => r.url().includes('/evaluations/') && r.url().includes('/calculate') && r.status() === 200,
        { timeout: 30000 }
      )
      await byTestId(page, 'btn-calculate').click()
      await calcResp
      await byTestId(page, 'eval-result').waitFor({ state: 'visible', timeout: 10000 })
      await shot(page, '11-eval-step4-results', __specDir)

      // 校验：结果区应出现总工作量
      await expect(byTestId(page, 'workload-summary')).toBeVisible()
    })

    await test.step('完成评估', async () => {
      const completeResp = page.waitForResponse(
        r => r.url().includes('/evaluations/') && r.url().includes('/complete') && r.status() === 200,
        { timeout: 15000 }
      )
      await byTestId(page, 'btn-complete-evaluation').click()

      // 可能出现的确认弹窗
      const confirmBtn = page.locator('.el-message-box__btns').getByRole('button', { name: '确认' })
      if (await confirmBtn.isVisible().catch(() => false)) {
        await confirmBtn.click()
      }
      await completeResp
      await page.waitForURL(`**/project/detail/${projectId}`, { timeout: 10000 })
      await shot(page, '12-project-detail-after-eval', __specDir)

      // BUG-20260610-02 修复回归断言：project.status 应变为 COMPLETED
      const projectResp = await callApi(page, 'GET', `/api/projects/${projectId}`)
      expect(projectResp.status).toBe(200)
      expect(projectResp.data?.data?.status, '完成评估后 project.status 应为 COMPLETED')
        .toBe('COMPLETED')
    })

    // ============================================================
    // 阶段 4：统计报告
    // ============================================================
    await test.step('查看统计图表', async () => {
      await page.goto(`/project/statistics/${projectId}`)
      await page.waitForLoadState('networkidle')
      // ECharts 渲染需要时间
      await page.waitForTimeout(2500)
      await shot(page, '13-statistics-overview', __specDir)

      await page.evaluate(() => window.scrollTo(0, 600))
      await page.waitForTimeout(600)
      await shot(page, '14-statistics-charts', __specDir)

      await page.evaluate(() => window.scrollTo(0, 1200))
      await page.waitForTimeout(600)
      await shot(page, '15-statistics-risk-warnings', __specDir)
    })
  })

  test.afterAll(async () => {
    // 清理：尝试删除创建的项目，避免污染开发库
    if (!capturedProjectId) return
    // 用一个独立 page 调删除（避免与已关闭的 page 绑定）
    // 注：依赖后端删除接口为草稿可删，流程完成后项目为 IN_PROGRESS，不应删除
    // 此处仅记录，供后续手工清理
    console.log(`[cleanup] created projectId=${capturedProjectId} (manual cleanup needed if IN_PROGRESS)`)
  })
})
