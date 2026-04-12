import { Page } from '@playwright/test'

/**
 * 通过 API 创建测试项目
 */
export async function createProjectViaAPI(page: Page, projectData: any): Promise<number> {
  const response = await page.request.post('/api/project', {
    data: projectData,
  })

  const result = await response.json()
  return result.data.id
}

/**
 * 通过 API 删除测试项目
 */
export async function deleteProjectViaAPI(page: Page, projectId: number) {
  await page.request.delete(`/api/project/${projectId}`)
}

/**
 * 清理测试数据
 */
export async function cleanupTestData(page: Page, options: { projectIds?: number[] } = {}) {
  const { projectIds = [] } = options

  // 删除测试项目
  for (const projectId of projectIds) {
    await deleteProjectViaAPI(page, projectId)
  }
}

/**
 * 通过 API 获取项目列表
 */
export async function getProjectsViaAPI(page: Page, params: any = {}) {
  const response = await page.request.get('/api/project', { params })
  return await response.json()
}

/**
 * 通过 API 更新项目
 */
export async function updateProjectViaAPI(page: Page, projectId: number, data: any) {
  const response = await page.request.put(`/api/project/${projectId}`, {
    data,
  })
  return await response.json()
}

/**
 * 通过 API 获取系统类型列表
 */
export async function getSystemTypesViaAPI(page: Page) {
  const response = await page.request.get('/api/system/type')
  return await response.json()
}

/**
 * 通过 API 获取模块列表
 */
export async function getModulesViaAPI(page: Page, systemTypeId: number) {
  const response = await page.request.get(`/api/module/${systemTypeId}`)
  return await response.json()
}

/**
 * 通过 API 创建评估记录
 */
export async function createEvaluationViaAPI(page: Page, evaluationData: any): Promise<number> {
  const response = await page.request.post('/api/evaluation', {
    data: evaluationData,
  })

  const result = await response.json()
  return result.data.id
}

/**
 * 通过 API 删除评估记录
 */
export async function deleteEvaluationViaAPI(page: Page, evaluationId: number) {
  await page.request.delete(`/api/evaluation/${evaluationId}`)
}