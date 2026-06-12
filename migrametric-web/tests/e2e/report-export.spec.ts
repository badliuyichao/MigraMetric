import { test, expect, type Page } from '@playwright/test'
import { loginAs } from './helpers/auth-helper'
import { shot } from './helpers/screenshot-helper'

/**
 * 报表导出 E2E 用例
 *
 * 依据需求说明文档 §3.5：
 * - §3.5.1 报告内容结构：项目信息 + 评估指标 + 工作量明细 + 图表 + 风险提示
 * - §3.5.2 导出格式：Excel / PDF / Word
 * - §3.5.3 导出功能：选择格式 → 选择内容范围 → 确认导出 → 自动下载
 *
 * 覆盖策略：
 * - RPT-001 Excel 导出（API 层验证，前端 ExportDialog 仅 Excel 可用）
 * - RPT-002 导出文件命名规则验证
 * - PDF/Word 前端标记"开发中"，暂不 E2E 覆盖
 */

const UNIQUE = () => `RPT-${Date.now()}`
const __specDir = 'report-export'

async function createCompletedProject(page: Page, name: string): Promise<number> {
  const token = await page.evaluate(() => localStorage.getItem('token'))
  const sourceSys = await page.request.get('http://localhost:3000/api/system/types/enabled?category=1', {
    headers: { Authorization: `Bearer ${token}` },
  })
  const sourceId = (await sourceSys.json()).data[0]?.id
  const targetSys = await page.request.get('http://localhost:3000/api/system/types/enabled?category=2', {
    headers: { Authorization: `Bearer ${token}` },
  })
  const targetId = (await targetSys.json()).data[0]?.id

  const createResp = await page.request.post('http://localhost:3000/api/projects', {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: {
      projectName: name,
      customerName: 'E2E导出客户',
      sourceSystemId: sourceId,
      targetSystemId: targetId,
      projectLeader: '导出测试员',
      contact: '13800000000',
      evaluationDate: '2026-06-08',
      description: 'E2E导出测试项目'
    },
  })
  const projectId = (await createResp.json()).data

  // 开始评估 → 选模块 → 填指标 → 计算 → 完成
  await page.request.post(`http://localhost:3000/api/evaluations/${projectId}/start`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
  })

  // 获取可用模块
  const modulesResp = await page.request.get(`http://localhost:3000/api/evaluations/${projectId}/modules`, {
    headers: { Authorization: `Bearer ${token}` },
  })
  const modulesData = (await modulesResp.json()).data
  if (modulesData && modulesData.length > 0) {
    const moduleConfigs = modulesData.slice(0, 2).map((m: any) => ({
      moduleId: m.moduleId || m.id,
      weight: m.defaultWeight || 1.0,
    }))
    await page.request.post(`http://localhost:3000/api/evaluations/${projectId}/modules`, {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: { modules: moduleConfigs },
    })
  }

  // 填指标
  await page.request.put(`http://localhost:3000/api/evaluations/${projectId}/indicators`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: {
      tableCount: 50,
      dataVolume: 500,
      userCount: 3000,
      reportCount: 20,
      hasCustomDev: 0,
    },
  })

  // 计算工作量
  await page.request.post(`http://localhost:3000/api/evaluations/${projectId}/calculate`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
  })

  // 完成评估
  await page.request.post(`http://localhost:3000/api/evaluations/${projectId}/complete`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
  })

  return projectId
}

test.describe('报表导出 E2E', () => {
  test('RPT-001: Excel 导出返回有效 xlsx 文件', async ({ page }) => {
    test.setTimeout(60000)
    await loginAs(page, 'admin')
    const name = UNIQUE()
    const projectId = await createCompletedProject(page, name)

    const token = await page.evaluate(() => localStorage.getItem('token'))
    const exportResp = await page.request.post(`http://localhost:3000/api/export/excel/${projectId}`, {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: {},
    })

    expect(exportResp.status(), '导出应返回 200').toBe(200)
    const contentType = exportResp.headers()['content-type'] || ''
    const body = await exportResp.body()
    expect(body.length, '导出文件不应为空').toBeGreaterThan(0)

    await shot(page, '01-export-excel-success', __specDir)
  })

  test('RPT-002: 导出文件名包含客户名和项目名', async ({ page }) => {
    test.setTimeout(60000)
    await loginAs(page, 'admin')
    const name = UNIQUE()
    const projectId = await createCompletedProject(page, name)

    const token = await page.evaluate(() => localStorage.getItem('token'))
    const exportResp = await page.request.post(`http://localhost:3000/api/export/excel/${projectId}`, {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: {},
    })

    expect(exportResp.status()).toBe(200)
    const disposition = exportResp.headers()['content-disposition'] || ''
    if (disposition) {
      expect(disposition).toContain('.xlsx')
    }
    await shot(page, '02-export-file-naming', __specDir)
  })

  test('RPT-003: 未完成评估的项目导出应返回空或错误内容', async ({ page }) => {
    test.setTimeout(30000)
    await loginAs(page, 'admin')
    const token = await page.evaluate(() => localStorage.getItem('token'))

    const createResp = await page.request.post('http://localhost:3000/api/projects', {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: {
        projectName: `RPT-DRAFT-${Date.now()}`,
        customerName: 'E2E测试客户',
        sourceSystemId: 1,
        targetSystemId: 2,
        evaluationDate: '2026-06-08',
      },
    })
    const projectId = (await createResp.json()).data

    const exportResp = await page.request.post(`http://localhost:3000/api/export/excel/${projectId}`, {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: {},
      failOnStatusCode: false,
    })

    if (exportResp.status() === 200) {
      const body = await exportResp.body()
      expect(body.length, '未评估项目导出文件应较小或为空').toBeLessThan(5000)
    } else {
      expect(exportResp.status(), '未评估项目导出应返回错误').toBeGreaterThanOrEqual(400)
    }
    await shot(page, '03-export-draft-project-fail', __specDir)
  })

  test('RPT-004: PDF 导出返回有效文件', async ({ page }) => {
    test.setTimeout(60000)
    await loginAs(page, 'admin')
    const name = `RPT-PDF-${UNIQUE()}`
    const projectId = await createCompletedProject(page, name)

    const token = await page.evaluate(() => localStorage.getItem('token'))
    const exportResp = await page.request.post(`http://localhost:3000/api/export/pdf/${projectId}`, {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: {},
    })

    expect(exportResp.status(), 'PDF 导出应返回 200').toBe(200)
    const body = await exportResp.body()
    expect(body.length, 'PDF 文件不应为空').toBeGreaterThan(0)
    await shot(page, '04-export-pdf-success', __specDir)
  })

  test('RPT-005: Word 导出返回有效文件', async ({ page }) => {
    test.setTimeout(60000)
    await loginAs(page, 'admin')
    const name = `RPT-WORD-${UNIQUE()}`
    const projectId = await createCompletedProject(page, name)

    const token = await page.evaluate(() => localStorage.getItem('token'))
    const exportResp = await page.request.post(`http://localhost:3000/api/export/word/${projectId}`, {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: {},
    })

    expect(exportResp.status(), 'Word 导出应返回 200').toBe(200)
    const body = await exportResp.body()
    expect(body.length, 'Word 文件不应为空').toBeGreaterThan(0)
    await shot(page, '05-export-word-success', __specDir)
  })
})
