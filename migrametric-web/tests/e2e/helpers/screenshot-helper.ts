import { Page, test } from '@playwright/test'

/**
 * 公共截图 helper：每个 spec 在 describe 块内调用 shot(page, '01-login-filled') 即可。
 *
 * 路径：test-results/<specDir>/<name>.png（与 Playwright 失败截图同根，方便查阅）
 * 行为：全页截图 + 推一条 test.info annotation（spec 报告里能看到每一步截图记录）
 *
 * specDir 推荐传 `__specDir` 常量（每个 spec 文件顶部定义），如：
 *   const __specDir = 'project'
 *   shot(page, `${__specDir}-01-list`)
 *
 * 命名规范见 migrametric-web/tests/README.md §E2E 测试约定 §3：
 *   <两位序号>-<动作或断言的简短中文拼音>.png
 *
 * 参考来源：本 helper 从 evaluation/full-flow.spec.ts:21-24 的本地 shot() 抽出。
 */
export async function shot(page: Page, name: string, specDir?: string): Promise<void> {
  const dir = specDir ?? defaultSpecDir()
  await page.screenshot({
    path: `test-results/${dir}/${name}.png`,
    fullPage: true,
  })
  test.info().annotations.push({ type: 'screenshot', description: name })
}

/**
 * 默认从 Playwright 内部 worker info 推导 spec 目录名（spec 文件名去掉 .spec.ts）。
 * 失败兜底用 'misc'。
 */
function defaultSpecDir(): string {
  const file = test.info().file
  if (!file) return 'misc'
  const base = file.split(/[/\\]/).pop() ?? ''
  return base.replace(/\.spec\.ts$/, '')
}
