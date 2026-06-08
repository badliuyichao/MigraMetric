import { Page, Locator } from '@playwright/test'

/**
 * 浏览器端 API 辅助：在已登录页面上下文里调 REST 接口，
 * 自动注入 localStorage 里的 JWT，避免走 form 登录。
 */
export async function callApi(
  page: Page,
  method: string,
  url: string,
  body?: unknown
): Promise<{ status: number; data: any }> {
  return page.evaluate(
    async ({ method, url, body }) => {
      const token = localStorage.getItem('token')
      const opts: RequestInit = {
        method,
        headers: { 'Content-Type': 'application/json' },
      }
      if (token) (opts.headers as Record<string, string>)['Authorization'] = `Bearer ${token}`
      if (body !== undefined) opts.body = JSON.stringify(body)
      const resp = await fetch(url, opts)
      const text = await resp.text()
      let parsed: any
      try { parsed = JSON.parse(text) } catch { parsed = text }
      return { status: resp.status, data: parsed }
    },
    { method, url, body }
  )
}

/**
 * 打开 el-select 包裹的 filterable 下拉，并按索引选中一项。
 * data-testid 指向 el-select 根节点。
 */
export async function pickFromSelect(
  page: Page,
  testId: string,
  optionIndex: number = 0
): Promise<void> {
  const select = page.locator(`[data-testid="${testId}"]`).first()
  await select.locator('.el-select__wrapper').click()
  await page.locator('.el-select-dropdown:visible').first().waitFor({ state: 'visible' })
  const items = page.locator('.el-select-dropdown:visible .el-select-dropdown__item')
  await items.nth(optionIndex).click()
  await page.waitForTimeout(150)
}

/**
 * el-input-number 的填写：先聚焦，再 keyboard 输入 + 失焦触发 change。
 * placeholder 用于精确定位（input-number 内部仍是 <input>）。
 */
export async function fillInputNumber(
  page: Page,
  placeholder: string,
  value: number
): Promise<void> {
  const input = page.locator(`input[placeholder="${placeholder}"]`).first()
  await input.click()
  await input.fill(String(value))
  await input.press('Tab')
  await page.waitForTimeout(200)
}

/**
 * 等待一个包含指定文本的按钮可点击。
 */
export async function clickButtonByText(
  page: Page,
  text: string | RegExp,
  testId?: string
): Promise<void> {
  const locator = testId
    ? page.locator(`[data-testid="${testId}"]`)
    : page.getByRole('button', { name: text })
  await locator.first().click()
}

/**
 * 等待指定 data-testid 元素出现（可见），返回 Locator 不点。
 */
export function byTestId(page: Page, testId: string): Locator {
  return page.locator(`[data-testid="${testId}"]`)
}
