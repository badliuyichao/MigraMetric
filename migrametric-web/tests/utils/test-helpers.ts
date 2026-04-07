import { Page } from '@playwright/test'

/**
 * 等待页面加载完成
 */
export async function waitForPageLoad(page: Page) {
  await page.waitForLoadState('networkidle')
  await page.waitForLoadState('domcontentloaded')
}

/**
 * 等待表格数据加载完成
 */
export async function waitForTableLoad(page: Page, tableSelector: string = '.el-table') {
  await page.waitForSelector(tableSelector)
  await page.waitForSelector('.el-table__row', { state: 'visible' })
  await page.waitForTimeout(500) // 等待数据渲染
}

/**
 * 截取整页屏幕快照
 */
export async function takeFullPageScreenshot(page: Page, name: string) {
  await page.screenshot({
    path: `tests/visual/snapshots/${name}.png`,
    fullPage: true,
  })
}

/**
 * 清除浏览器存储（localStorage、sessionStorage、cookies）
 */
export async function clearBrowserStorage(page: Page) {
  await page.evaluate(() => {
    localStorage.clear()
    sessionStorage.clear()
  })
  const context = page.context()
  await context.clearCookies()
}

/**
 * 检查元素是否可见
 */
export async function isElementVisible(page: Page, selector: string): Promise<boolean> {
  const element = await page.$(selector)
  if (!element) return false
  return await element.isVisible()
}

/**
 * 等待 Toast 消息出现
 */
export async function waitForToast(page: Page, timeout: number = 5000) {
  await page.waitForSelector('.el-message', { state: 'visible', timeout })
}

/**
 * 关闭所有对话框
 */
export async function closeAllDialogs(page: Page) {
  const closeButtons = await page.$$('.el-dialog__headerbtn')
  for (const button of closeButtons) {
    await button.click()
    await page.waitForTimeout(300)
  }
}