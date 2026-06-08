import { test, expect } from '@playwright/test'
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
    await expect(page.locator('[data-testid="table-system-type"] .el-table__row')).toContainText(sysName)
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
})
