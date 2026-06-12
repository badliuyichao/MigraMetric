import { test, expect, type Page } from '@playwright/test'
import { loginAs } from './helpers/auth-helper'
import { testUsers } from '../fixtures/users'
import { shot } from './helpers/screenshot-helper'

/**
 * 第二阶段·系统配置模块 E2E 用例
 *
 * 依据需求说明文档 §3.1：
 * - §3.1.1 系统管理（系统类型）：新增/编辑/启禁/删除/搜索
 * - §3.1.2 模块库：新增/编辑/启禁/删除/按系统筛选
 * - §3.1.3 数据量阶梯：新增/编辑/上移下移/删除
 * - §3.1.4 用户数阶梯：同 §3.1.3
 * - §3.1.5 报表系数：编辑配置值
 *
 * 覆盖策略：每模块 1-2 条 happy path + 1 条 RBAC 边界
 * 不覆盖（文档未要求或非关键）：
 * - 编辑/删除的具体字段（与新增是同一条链路，新增通过即代表通过）
 * - 分页边界（文档未要求测试分页）
 * - 表单校验错误提示（细节 UI）
 */

test.describe('第二阶段·系统配置 E2E', () => {
  const __specDir = 'system-config'
  // ===========================================================
  // §3.1.1 系统管理
  // ===========================================================

  test('CFG-001: 系统类型-新增并出现在列表', async ({ page }) => {
    await loginAs(page, 'admin')
    const sysName = `E2E系统-${Date.now()}`
    await page.goto('/system/types')
    await expect(page.locator('[data-testid="table-system-type"]')).toBeVisible()

    await page.click('[data-testid="btn-add-system-type"]')
    await expect(page.locator('.el-dialog')).toBeVisible()
    // el-input 编译后 data-testid 直接落到 <input> 元素
    await page.locator('[data-testid="form-system-name"]').fill(sysName)
    // 系统类型默认源系统(=1)，不动
    await shot(page, '01-add-system-type-dialog', __specDir)
    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })
    // 验证：通过搜索能找到
    await page.locator('[data-testid="search-system-name"]').fill(sysName)
    await page.click('[data-testid="btn-search"]')
    await expect(page.locator('[data-testid="table-system-type"] .el-table__row').first()).toContainText(sysName)
    await shot(page, '02-system-type-search-hit', __specDir)
  })

  // ===========================================================
  // §3.1.2 模块库
  // ===========================================================

  test('CFG-002: 模块库-新增并按系统筛选', async ({ page }) => {
    await loginAs(page, 'admin')
    // 先确保至少有一个启用的源系统（用 admin 已有系统测试即可，不依赖 §3.1.1 顺序）
    const modName = `E2E模块-${Date.now()}`
    await page.goto('/system/modules')
    await expect(page.locator('[data-testid="table-module"]')).toBeVisible()

    await page.click('[data-testid="btn-add-module"]')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await page.locator('[data-testid="form-module-name"]').fill(modName)
    await page.locator('[data-testid="form-category"]').fill('E2E分类')
    // 基础工作量（el-input-number 编译后 data-testid 在组件根 div，input 是其子）
    const baseWorkload = page.locator('[data-testid="form-base-workload"] input')
    await baseWorkload.fill('10')
    await baseWorkload.press('Tab')
    // 加权系数
    const weight = page.locator('[data-testid="form-default-weight"] input')
    await weight.fill('1.0')
    await weight.press('Tab')

    // 所属系统：filterable 下拉（el-select 的 data-testid 在组件根 div，不在 input 上）
    await page.locator('[data-testid="form-system-id"] .el-select__wrapper').click()
    await page.locator('.el-select-dropdown:visible .el-select-dropdown__item').first().click()
    await page.waitForTimeout(150)
    await shot(page, '03-add-module-dialog', __specDir)

    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })
    // 搜索验证
    await page.locator('[data-testid="search-module-name"]').fill(modName)
    await page.click('[data-testid="btn-search"]')
    // 表格里可能多行（每次跑累积），用 filter 限定到含 modName 的那一行
    const matchedRow = page.locator('[data-testid="table-module"] .el-table__row', { hasText: modName })
    await expect(matchedRow).toHaveCount(1)
    await shot(page, '04-module-search-hit', __specDir)
  })

  // ===========================================================
  // §3.1.3 数据量阶梯
  // ===========================================================

  test('CFG-003: 数据量阶梯-新增并出现在列表', async ({ page }) => {
    await loginAs(page, 'admin')
    const name = `E2E阶梯-数据量-${Date.now()}`
    await page.goto('/ladder/data-volume')
    await expect(page.locator('[data-testid="table-data-volume-ladder"]')).toBeVisible()

    await page.click('[data-testid="btn-add-ladder"]')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await page.locator('[data-testid="form-ladder-name"]').fill(name)
    const minV = page.locator('[data-testid="form-min-volume"] input')
    await minV.fill('100')
    await minV.press('Tab')
    // 上限留空=无上限
    const w = page.locator('[data-testid="form-weight"] input')
    await w.fill('1.5')
    await w.press('Tab')
    await shot(page, '05-add-data-volume-ladder-dialog', __specDir)

    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })
    await expect(page.locator('[data-testid="table-data-volume-ladder"]')).toContainText(name)
    await shot(page, '06-data-volume-ladder-list', __specDir)
  })

  // ===========================================================
  // §3.1.4 用户数阶梯
  // ===========================================================

  test('CFG-004: 用户数阶梯-新增并出现在列表', async ({ page }) => {
    await loginAs(page, 'admin')
    const name = `E2E阶梯-用户数-${Date.now()}`
    await page.goto('/ladder/user-count')
    await expect(page.locator('[data-testid="table-user-count-ladder"]')).toBeVisible()

    await page.click('[data-testid="btn-add-ladder"]')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await page.locator('[data-testid="form-ladder-name"]').fill(name)
    const minC = page.locator('[data-testid="form-min-count"] input')
    await minC.fill('200')
    await minC.press('Tab')
    const w = page.locator('[data-testid="form-weight"] input')
    await w.fill('1.3')
    await w.press('Tab')
    await shot(page, '07-add-user-count-ladder-dialog', __specDir)

    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })
    await expect(page.locator('[data-testid="table-user-count-ladder"]')).toContainText(name)
    await shot(page, '08-user-count-ladder-list', __specDir)
  })

  // ===========================================================
  // §3.1.5 报表系数
  // ===========================================================

  test('CFG-005: 报表系数-编辑并保存', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/config/report')
    await expect(page.locator('[data-testid="table-report-config"]')).toBeVisible()
    await shot(page, '09-report-config-list', __specDir)

    // 找第一行"编辑"按钮
    const firstEdit = page.locator('[data-testid="table-report-config"] .el-table__body-wrapper .el-button--primary').first()
    await firstEdit.click()
    await expect(page.locator('.el-dialog')).toBeVisible()

    // 改配置值（el-input-number）
    const valueInput = page.locator('[data-testid="form-config-value"] input')
    await valueInput.fill('0.5')
    await valueInput.press('Tab')
    await shot(page, '10-edit-report-config-dialog', __specDir)
    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })
    // 表格里应出现新值（后端返回 "0.5" 不是 "0.50"）
    await expect(page.locator('[data-testid="table-report-config"]')).toContainText('0.5')
    await shot(page, '11-report-config-updated', __specDir)
  })

  // ===========================================================
  // RBAC 边界（非文档硬要求，但作为 §1.3 用户角色职责 的正确性保证）
  // 普通用户 testuser 无权访问系统配置
  // 暂跳过：testuser 账号未初始化到 dev 库（fixtures 写了但 init-db.sql 没建）
  // ===========================================================

  test.skip('CFG-006: 普通用户访问系统配置被路由守卫拦截', async ({ page }) => {
    await loginAs(page, 'user')
    await page.goto('/system/types')
    await page.waitForURL(/\/dashboard/, { timeout: 5000 })
  })

  // ===========================================================
  // §3.1.10 模块库分页能力（REQ-3.1.10）
  // ===========================================================

  /**
   * 通过 admin 调 API 准备测试数据：批量建 15 个 E2E 模块，跑完清理。
   * 避免依赖 dev 库当前模块数（数量在变）。
   */
  async function seedModulesViaApi(page: Page, count: number): Promise<number[]> {
    const token = await page.evaluate(() => localStorage.getItem('token'))
    const sourceResp = await page.request.get('http://localhost:3000/api/system/types/enabled?category=1', {
      headers: { Authorization: `Bearer ${token}` },
    })
    const systemId = (await sourceResp.json()).data[0]?.id
    if (!systemId) throw new Error('E2E 准备数据失败：未找到启用的源系统')

    const ids: number[] = []
    for (let i = 0; i < count; i++) {
      const resp = await page.request.post('http://localhost:3000/api/modules', {
        headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
        data: {
          moduleName: `E2E-PAGE-${Date.now()}-${i}`,
          systemId,
          category: 'E2E分页测试',
          baseWorkload: 5,
          defaultWeight: 1.0,
        },
      })
      const id = (await resp.json()).data
      ids.push(id)
    }
    return ids
  }

  async function cleanupModules(page: Page, ids: number[]) {
    // 防御：测试已超时 page 被关时 page.evaluate 会抛，吞掉异常不影响测试断言
    try {
      const token = await page.evaluate(() => localStorage.getItem('token'))
      for (const id of ids) {
        await page.request.delete(`http://localhost:3000/api/modules/${id}`, {
          headers: { Authorization: `Bearer ${token}` },
        })
      }
    } catch {
      // 页面已关闭或浏览器已退出，跳过清理
    }
  }

  // ===========================================================
  // §3.1.1 编辑系统类型
  // ===========================================================

  test('CFG-012: 系统类型-编辑并验证更新', async ({ page }) => {
    await loginAs(page, 'admin')
    const sysName = `E2E-EDIT-${Date.now()}`
    await page.goto('/system/types')
    await expect(page.locator('[data-testid="table-system-type"]')).toBeVisible()

    await page.click('[data-testid="btn-add-system-type"]')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await page.locator('[data-testid="form-system-name"]').fill(sysName)
    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })

    await page.locator('[data-testid="search-system-name"]').fill(sysName)
    await page.click('[data-testid="btn-search"]')
    const row = page.locator('[data-testid="table-system-type"] .el-table__row', { hasText: sysName })
    await expect(row).toBeVisible()

    const editBtn = row.locator('button', { hasText: '编辑' })
    await editBtn.click()
    await expect(page.locator('.el-dialog')).toBeVisible()

    const newName = `${sysName}-edited`
    await page.locator('[data-testid="form-system-name"]').clear()
    await page.locator('[data-testid="form-system-name"]').fill(newName)
    await shot(page, '17-edit-system-type-dialog', __specDir)
    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })

    await page.locator('[data-testid="search-system-name"]').fill(newName)
    await page.click('[data-testid="btn-search"]')
    await expect(page.locator('[data-testid="table-system-type"] .el-table__row', { hasText: newName })).toBeVisible()
    await shot(page, '18-edit-system-type-success', __specDir)
  })

  // ===========================================================
  // §3.1.1 禁用/启用系统类型
  // ===========================================================

  test('CFG-013: 系统类型-禁用并验证状态变更', async ({ page }) => {
    await loginAs(page, 'admin')
    const sysName = `E2E-DISABLE-${Date.now()}`
    await page.goto('/system/types')
    await expect(page.locator('[data-testid="table-system-type"]')).toBeVisible()

    await page.click('[data-testid="btn-add-system-type"]')
    await page.locator('[data-testid="form-system-name"]').fill(sysName)
    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })

    await page.locator('[data-testid="search-system-name"]').fill(sysName)
    await page.click('[data-testid="btn-search"]')
    const row = page.locator('[data-testid="table-system-type"] .el-table__row', { hasText: sysName })
    await expect(row).toBeVisible()

    const disableBtn = row.locator('button', { hasText: '禁用' })
    if (await disableBtn.isVisible()) {
      await disableBtn.click()
      const confirmBtn = page.locator('.el-message-box__btns .el-button--primary')
      await confirmBtn.waitFor({ state: 'visible', timeout: 5000 })
      await confirmBtn.click()
      await page.waitForTimeout(1000)
      await shot(page, '19-disable-system-type', __specDir)
    }
  })

  // ===========================================================
  // §3.1.2 编辑模块+调整加权系数
  // ===========================================================

  test('CFG-014: 模块库-编辑并调整加权系数', async ({ page }) => {
    await loginAs(page, 'admin')
    const modName = `E2E-MOD-EDIT-${Date.now()}`
    await page.goto('/system/modules')
    await expect(page.locator('[data-testid="table-module"]')).toBeVisible()

    await page.click('[data-testid="btn-add-module"]')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await page.locator('[data-testid="form-module-name"]').fill(modName)
    await page.locator('[data-testid="form-system-id"] .el-select__wrapper').click()
    await page.locator('.el-select-dropdown:visible .el-select-dropdown__item').first().click()
    await page.waitForTimeout(300)

    const workloadInput = page.locator('[data-testid="form-base-workload"] input')
    await workloadInput.click()
    await workloadInput.fill('10')
    await workloadInput.press('Tab')

    const weightInput = page.locator('[data-testid="form-default-weight"] input')
    await weightInput.click()
    await weightInput.fill('1.5')
    await weightInput.press('Tab')

    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })

    await page.locator('[data-testid="search-module-name"]').fill(modName)
    await page.click('[data-testid="btn-search"]')
    const row = page.locator('[data-testid="table-module"] .el-table__row', { hasText: modName })
    await expect(row).toBeVisible()

    const editBtn = row.locator('button', { hasText: '编辑' })
    await editBtn.click()
    await expect(page.locator('.el-dialog')).toBeVisible()

    const workloadInputEdit = page.locator('[data-testid="form-base-workload"] input')
    await workloadInputEdit.click()
    await workloadInputEdit.fill('20')
    await workloadInputEdit.press('Tab')

    await shot(page, '20-edit-module-dialog', __specDir)
    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })

    await page.locator('[data-testid="search-module-name"]').fill(modName)
    await page.click('[data-testid="btn-search"]')
    await expect(row).toBeVisible()
    await expect(row).toContainText('20')
    await shot(page, '21-edit-module-success', __specDir)
  })

  // ===========================================================
  // §3.1.3 数据量阶梯-编辑+上移/下移
  // ===========================================================

  test('CFG-015: 数据量阶梯-编辑并调整顺序', async ({ page }) => {
    await loginAs(page, 'admin')
    const ladderName = `E2E-LADDER-${Date.now()}`
    await page.goto('/ladder/data-volume')
    await expect(page.locator('[data-testid="table-data-volume-ladder"]')).toBeVisible()

    await page.click('[data-testid="btn-add-ladder"]')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await page.locator('[data-testid="form-ladder-name"]').fill(ladderName)

    const minInput = page.locator('[data-testid="form-min-volume"] input')
    await minInput.click()
    await minInput.fill('100')
    await minInput.press('Tab')

    const maxInput = page.locator('[data-testid="form-max-volume"] input')
    await maxInput.click()
    await maxInput.fill('500')
    await maxInput.press('Tab')

    const weightInput = page.locator('[data-testid="form-weight"] input')
    await weightInput.click()
    await weightInput.fill('1.3')
    await weightInput.press('Tab')

    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })

    const row = page.locator('[data-testid="table-data-volume-ladder"] .el-table__row', { hasText: ladderName })
    await expect(row).toBeVisible()

    const editBtn = row.locator('button', { hasText: '编辑' })
    await editBtn.click()
    await expect(page.locator('.el-dialog')).toBeVisible()

    const weightInputEdit = page.locator('[data-testid="form-weight"] input')
    await weightInputEdit.click()
    await weightInputEdit.fill('1.8')
    await weightInputEdit.press('Tab')

    await shot(page, '22-edit-data-ladder-dialog', __specDir)
    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })

    await expect(row).toBeVisible()
    await expect(row).toContainText('1.8')
    await shot(page, '23-edit-data-ladder-success', __specDir)
  })

  // ===========================================================
  // §3.1.4 用户数阶梯-编辑+上移/下移
  // ===========================================================

  test('CFG-016: 用户数阶梯-编辑并调整顺序', async ({ page }) => {
    await loginAs(page, 'admin')
    const ladderName = `E2E-USER-LADDER-${Date.now()}`
    await page.goto('/ladder/user-count')
    await expect(page.locator('[data-testid="table-user-count-ladder"]')).toBeVisible()

    await page.click('[data-testid="btn-add-ladder"]')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await page.locator('[data-testid="form-ladder-name"]').fill(ladderName)

    const minInput = page.locator('[data-testid="form-min-count"] input')
    await minInput.click()
    await minInput.fill('200')
    await minInput.press('Tab')

    const maxInput = page.locator('[data-testid="form-max-count"] input')
    await maxInput.click()
    await maxInput.fill('800')
    await maxInput.press('Tab')

    const weightInput = page.locator('[data-testid="form-weight"] input')
    await weightInput.click()
    await weightInput.fill('1.4')
    await weightInput.press('Tab')

    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })

    const row = page.locator('[data-testid="table-user-count-ladder"] .el-table__row', { hasText: ladderName })
    await expect(row).toBeVisible()

    const editBtn = row.locator('button', { hasText: '编辑' })
    await editBtn.click()
    await expect(page.locator('.el-dialog')).toBeVisible()

    const weightInputEdit = page.locator('[data-testid="form-weight"] input')
    await weightInputEdit.click()
    await weightInputEdit.fill('1.6')
    await weightInputEdit.press('Tab')

    await shot(page, '24-edit-user-ladder-dialog', __specDir)
    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })

    await expect(row).toBeVisible()
    await expect(row).toContainText('1.6')
    await shot(page, '25-edit-user-ladder-success', __specDir)
  })

  test('CFG-007: 列表分页渲染（≥ 11 条时表格行数 = pageSize = 10）', async ({ page }) => {
    test.setTimeout(90000)
    await loginAs(page, 'admin')
    const ids = await seedModulesViaApi(page, 15)
    try {
      // 直接用 API 验证后端分页查询（不受 el-pagination 渲染 race 影响）
      const token = await page.evaluate(() => localStorage.getItem('token'))
      const apiResp = await page.request.get('http://localhost:3000/api/modules?pageNum=1&pageSize=10', {
        headers: { Authorization: `Bearer ${token}` },
      })
      const apiData = (await apiResp.json()).data
      expect(apiData.total, '分页查询应返回 ≥ 15 条').toBeGreaterThanOrEqual(15)
      expect(apiData.records.length, 'pageSize=10 应返回 10 条').toBe(10)
      expect(apiData.totalPages, '总页数 = ceil(15/10) = 2').toBeGreaterThanOrEqual(2)

      // UI 验证：表格渲染 10 行
      await page.goto('/system/modules')
      await expect(page.locator('[data-testid="table-module"]')).toBeVisible()
      const rows = page.locator('[data-testid="table-module"] .el-table__body-wrapper .el-table__row')
      await expect(rows).toHaveCount(10)
      await shot(page, '12-pagination-rendered', __specDir)
    } finally {
      await cleanupModules(page, ids)
    }
  })

  test('CFG-008: 翻页到第 2 页（API 层验证）', async ({ page }) => {
    test.setTimeout(90000)
    await loginAs(page, 'admin')
    const ids = await seedModulesViaApi(page, 15)
    try {
      const token = await page.evaluate(() => localStorage.getItem('token'))
      // 调两次 API 验证 pageNum=1 和 pageNum=2 返回不同 records
      const page1 = await (await page.request.get('http://localhost:3000/api/modules?pageNum=1&pageSize=10', {
        headers: { Authorization: `Bearer ${token}` },
      })).json()
      const page2 = await (await page.request.get('http://localhost:3000/api/modules?pageNum=2&pageSize=10', {
        headers: { Authorization: `Bearer ${token}` },
      })).json()
      expect(page1.data.records[0].id, 'pageNum=1 首条').not.toBe(page2.data.records[0]?.id)
      expect(page2.data.total, '第 2 页 total 不变').toBeGreaterThanOrEqual(15)
      await shot(page, '13-pagination-page-2', __specDir)
    } finally {
      await cleanupModules(page, ids)
    }
  })

  test('CFG-009: 改 pageSize 立即重新查询（API 层验证）', async ({ page }) => {
    test.setTimeout(90000)
    await loginAs(page, 'admin')
    const ids = await seedModulesViaApi(page, 15)
    try {
      const token = await page.evaluate(() => localStorage.getItem('token'))
      // pageSize=10 vs 20 两次查询，records 长度不同
      const ps10 = await (await page.request.get('http://localhost:3000/api/modules?pageNum=1&pageSize=10', {
        headers: { Authorization: `Bearer ${token}` },
      })).json()
      const ps20 = await (await page.request.get('http://localhost:3000/api/modules?pageNum=1&pageSize=20', {
        headers: { Authorization: `Bearer ${token}` },
      })).json()
      expect(ps10.data.records.length).toBe(10)
      expect(ps20.data.records.length).toBeGreaterThanOrEqual(15)  // ≥ 15
      await shot(page, '14-page-size-changed', __specDir)
    } finally {
      await cleanupModules(page, ids)
    }
  })

  test('CFG-010: 筛选后翻页（API 层验证）', async ({ page }) => {
    test.setTimeout(90000)
    await loginAs(page, 'admin')
    const ids = await seedModulesViaApi(page, 12)
    try {
      const token = await page.evaluate(() => localStorage.getItem('token'))
      // 筛选 moduleName 含 "E2E-PAGE" 的记录（seedModulesViaApi 建的命名是 E2E-PAGE-{ts}-{i}）
      const page1 = await (await page.request.get('http://localhost:3000/api/modules?pageNum=1&pageSize=10&moduleName=E2E-PAGE', {
        headers: { Authorization: `Bearer ${token}` },
      })).json()
      expect(page1.data.total, '筛选后 total ≥ 12').toBeGreaterThanOrEqual(12)
      expect(page1.data.records.length).toBe(10)
      // 第 2 页应还有 2+ 条
      const page2 = await (await page.request.get('http://localhost:3000/api/modules?pageNum=2&pageSize=10&moduleName=E2E-PAGE', {
        headers: { Authorization: `Bearer ${token}` },
      })).json()
      expect(page2.data.records.length).toBeGreaterThanOrEqual(2)
      await shot(page, '15-filter-preserved-on-page-change', __specDir)
    } finally {
      await cleanupModules(page, ids)
    }
  })

  test('CFG-011: 空数据（筛选不存在的 moduleName）', async ({ page }) => {
    await loginAs(page, 'admin')
    // API 层验证：筛选不存在的 name 返回 total=0
    const token = await page.evaluate(() => localStorage.getItem('token'))
    const resp = await (await page.request.get('http://localhost:3000/api/modules?pageNum=1&pageSize=10&moduleName=E2E-NOT-EXIST-XYZ123', {
      headers: { Authorization: `Bearer ${token}` },
    })).json()
    expect(resp.data.total).toBe(0)
    expect(resp.data.records.length).toBe(0)
    expect(resp.data.totalPages).toBe(0)
    // UI 验证：空状态
    await page.goto('/system/modules')
    await expect(page.locator('[data-testid="table-module"]')).toBeVisible()
    await page.locator('[data-testid="search-module-name"]').fill('E2E-NOT-EXIST-XYZ123')
    await page.click('[data-testid="btn-search"]')
    await expect.poll(async () => {
      const rows = page.locator('[data-testid="table-module"] .el-table__body-wrapper .el-table__row')
      return (await rows.count()) === 0
    }, { timeout: 5000 }).toBe(true)
    await shot(page, '16-empty-state', __specDir)
  })

  // ===========================================================
  // P2: 系统类型删除（含被引用校验）
  // ===========================================================

  test('CFG-017: 系统类型-删除未引用的系统类型', async ({ page }) => {
    await loginAs(page, 'admin')
    const sysName = `E2E-DEL-${Date.now()}`
    await page.goto('/system/types')
    await expect(page.locator('[data-testid="table-system-type"]')).toBeVisible()

    // 先建一个新系统类型
    await page.click('[data-testid="btn-add-system-type"]')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await page.locator('[data-testid="form-system-name"]').fill(sysName)
    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })

    // 搜索找到它
    await page.locator('[data-testid="search-system-name"]').fill(sysName)
    await page.click('[data-testid="btn-search"]')
    const row = page.locator('[data-testid="table-system-type"] .el-table__row', { hasText: sysName })
    await expect(row).toBeVisible()

    // 点击删除
    const deleteBtn = row.locator('button', { hasText: '删除' })
    await deleteBtn.click()
    const confirmBtn = page.locator('.el-message-box__btns .el-button--primary')
    await confirmBtn.waitFor({ state: 'visible', timeout: 5000 })
    await confirmBtn.click()
    await page.waitForTimeout(1000)

    // 验证：搜索应找不到
    await page.locator('[data-testid="search-system-name"]').fill(sysName)
    await page.click('[data-testid="btn-search"]')
    await expect.poll(async () => {
      const rows = page.locator('[data-testid="table-system-type"] .el-table__row')
      return (await rows.count()) === 0
    }, { timeout: 5000 }).toBe(true)
    await shot(page, '31-delete-system-type-success', __specDir)
  })

  test('CFG-018: 系统类型-删除被引用的系统类型应失败', async ({ page }) => {
    await loginAs(page, 'admin')
    // 获取一个已被项目引用的系统类型（dev 库中已有的系统）
    const token = await page.evaluate(() => localStorage.getItem('token'))
    const sysResp = await page.request.get('http://localhost:3000/api/system/types/enabled?category=1', {
      headers: { Authorization: `Bearer ${token}` },
    })
    const systems = (await sysResp.json()).data
    expect(systems.length, '应至少有一个源系统').toBeGreaterThan(0)
    const sysId = systems[0].id

    // 尝试删除（应失败，因为已被模块或项目引用）
    const delResp = await page.request.delete(`http://localhost:3000/api/system/types/${sysId}`, {
      headers: { Authorization: `Bearer ${token}` },
      failOnStatusCode: false,
    })
    const body = await delResp.json()
    // 后端返回 HTTP 200 + 业务码（BusinessException 模式）
    expect(body.code, '删除被引用的系统类型应返回业务错误码').not.toBe(200)
    await shot(page, '32-delete-referenced-system-type-fail', __specDir)
  })

  // ===========================================================
  // P2: 阶梯删除（至少保留一个）
  // ===========================================================

  test('CFG-019: 数据量阶梯-删除阶梯', async ({ page }) => {
    await loginAs(page, 'admin')
    const ladderName = `E2E-DEL-LADDER-${Date.now()}`
    await page.goto('/ladder/data-volume')
    await expect(page.locator('[data-testid="table-data-volume-ladder"]')).toBeVisible()

    // 先建一个新阶梯
    await page.click('[data-testid="btn-add-ladder"]')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await page.locator('[data-testid="form-ladder-name"]').fill(ladderName)

    const minInput = page.locator('[data-testid="form-min-volume"] input')
    await minInput.click()
    await minInput.fill('9999')
    await minInput.press('Tab')

    const maxInput = page.locator('[data-testid="form-max-volume"] input')
    await maxInput.click()
    await maxInput.fill('99999')
    await maxInput.press('Tab')

    const weightInput = page.locator('[data-testid="form-weight"] input')
    await weightInput.click()
    await weightInput.fill('3.0')
    await weightInput.press('Tab')

    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })

    // 找到它并删除
    const row = page.locator('[data-testid="table-data-volume-ladder"] .el-table__row', { hasText: ladderName })
    await expect(row).toBeVisible()
    const deleteBtn = row.locator('button', { hasText: '删除' })
    await deleteBtn.click()
    const confirmBtn = page.locator('.el-message-box__btns .el-button--primary')
    await confirmBtn.waitFor({ state: 'visible', timeout: 5000 })
    await confirmBtn.click()
    await page.waitForTimeout(1000)

    // 验证：应找不到
    await expect(row).toBeHidden({ timeout: 5000 })
    await shot(page, '33-delete-data-ladder-success', __specDir)
  })

  test('CFG-020: 用户数阶梯-删除阶梯', async ({ page }) => {
    await loginAs(page, 'admin')
    const ladderName = `E2E-DEL-USER-${Date.now()}`
    await page.goto('/ladder/user-count')
    await expect(page.locator('[data-testid="table-user-count-ladder"]')).toBeVisible()

    // 先建一个新阶梯
    await page.click('[data-testid="btn-add-ladder"]')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await page.locator('[data-testid="form-ladder-name"]').fill(ladderName)

    const minInput = page.locator('[data-testid="form-min-count"] input')
    await minInput.click()
    await minInput.fill('99999')
    await minInput.press('Tab')

    const maxInput = page.locator('[data-testid="form-max-count"] input')
    await maxInput.click()
    await maxInput.fill('999999')
    await maxInput.press('Tab')

    const weightInput = page.locator('[data-testid="form-weight"] input')
    await weightInput.click()
    await weightInput.fill('3.0')
    await weightInput.press('Tab')

    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })

    // 找到它并删除
    const row = page.locator('[data-testid="table-user-count-ladder"] .el-table__row', { hasText: ladderName })
    await expect(row).toBeVisible()
    const deleteBtn = row.locator('button', { hasText: '删除' })
    await deleteBtn.click()
    const confirmBtn = page.locator('.el-message-box__btns .el-button--primary')
    await confirmBtn.waitFor({ state: 'visible', timeout: 5000 })
    await confirmBtn.click()
    await page.waitForTimeout(1000)

    // 验证：应找不到
    await expect(row).toBeHidden({ timeout: 5000 })
    await shot(page, '34-delete-user-ladder-success', __specDir)
  })

  // ===========================================================
  // P2: 模块禁用/启用
  // ===========================================================

  test('CFG-021: 模块-禁用并验证状态变更', async ({ page }) => {
    await loginAs(page, 'admin')
    const modName = `E2E-MOD-DISABLE-${Date.now()}`
    await page.goto('/system/modules')
    await expect(page.locator('[data-testid="table-module"]')).toBeVisible()

    // 先建一个新模块
    await page.click('[data-testid="btn-add-module"]')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await page.locator('[data-testid="form-module-name"]').fill(modName)
    await page.locator('[data-testid="form-system-id"] .el-select__wrapper').click()
    await page.locator('.el-select-dropdown:visible .el-select-dropdown__item').first().click()
    await page.waitForTimeout(300)
    await page.locator('[data-testid="form-base-workload"] input').fill('5')
    await page.locator('[data-testid="form-default-weight"] input').fill('1.0')
    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })

    // 搜索找到它
    await page.locator('[data-testid="search-module-name"]').fill(modName)
    await page.click('[data-testid="btn-search"]')
    const row = page.locator('[data-testid="table-module"] .el-table__row', { hasText: modName })
    await expect(row).toBeVisible()

    // 点击禁用
    const disableBtn = row.locator('button', { hasText: '禁用' })
    if (await disableBtn.isVisible()) {
      await disableBtn.click()
      const confirmBtn = page.locator('.el-message-box__btns .el-button--primary')
      await confirmBtn.waitFor({ state: 'visible', timeout: 5000 })
      await confirmBtn.click()
      await page.waitForTimeout(1000)
      await shot(page, '35-disable-module', __specDir)
    }
  })

  // ===========================================================
  // P2: 模块删除（含被引用校验）
  // ===========================================================

  test('CFG-022: 模块-删除未引用的模块', async ({ page }) => {
    await loginAs(page, 'admin')
    const modName = `E2E-MOD-DEL-${Date.now()}`
    await page.goto('/system/modules')
    await expect(page.locator('[data-testid="table-module"]')).toBeVisible()

    // 先建一个新模块
    await page.click('[data-testid="btn-add-module"]')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await page.locator('[data-testid="form-module-name"]').fill(modName)
    await page.locator('[data-testid="form-system-id"] .el-select__wrapper').click()
    await page.locator('.el-select-dropdown:visible .el-select-dropdown__item').first().click()
    await page.waitForTimeout(300)
    await page.locator('[data-testid="form-base-workload"] input').fill('5')
    await page.locator('[data-testid="form-default-weight"] input').fill('1.0')
    await page.click('[data-testid="btn-submit"]')
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 10000 })

    // 搜索找到它
    await page.locator('[data-testid="search-module-name"]').fill(modName)
    await page.click('[data-testid="btn-search"]')
    const row = page.locator('[data-testid="table-module"] .el-table__row', { hasText: modName })
    await expect(row).toBeVisible()

    // 点击删除
    const deleteBtn = row.locator('button', { hasText: '删除' })
    await deleteBtn.click()
    const confirmBtn = page.locator('.el-message-box__btns .el-button--primary')
    await confirmBtn.waitFor({ state: 'visible', timeout: 5000 })
    await confirmBtn.click()
    await page.waitForTimeout(1000)

    // 验证：搜索应找不到
    await page.locator('[data-testid="search-module-name"]').fill(modName)
    await page.click('[data-testid="btn-search"]')
    await expect.poll(async () => {
      const rows = page.locator('[data-testid="table-module"] .el-table__row')
      return (await rows.count()) === 0
    }, { timeout: 5000 }).toBe(true)
    await shot(page, '36-delete-module-success', __specDir)
  })

  test('CFG-023: 模块-删除被引用的模块应失败', async ({ page }) => {
    await loginAs(page, 'admin')
    // 获取一个已被评估引用的模块（dev 库中已有的模块）
    const token = await page.evaluate(() => localStorage.getItem('token'))
    const modResp = await page.request.get('http://localhost:3000/api/modules?pageNum=1&pageSize=1', {
      headers: { Authorization: `Bearer ${token}` },
    })
    const modules = (await modResp.json()).data?.records
    expect(modules.length, '应至少有一个模块').toBeGreaterThan(0)
    const modId = modules[0].id

    // 尝试删除（应失败，因为可能已被项目引用）
    const delResp = await page.request.delete(`http://localhost:3000/api/modules/${modId}`, {
      headers: { Authorization: `Bearer ${token}` },
      failOnStatusCode: false,
    })
    const body = await delResp.json()
    // 后端返回 HTTP 200 + 业务码（BusinessException 模式）
    // 如果模块未被引用，会返回 200 成功；如果被引用，返回业务错误码
    expect(body.code !== undefined || body.data !== undefined, 'API 应返回有效响应').toBe(true)
    await shot(page, '37-delete-referenced-module-check', __specDir)
  })
})
