---
name: e2e-must-use-playwright-headed-with-screenshots
description: All E2E in MigraMetric must use Playwright, must run headed (browser visible) locally, and must screenshot every key step. Hard rule set by the user on 2026-06-08.
metadata:
  type: feedback
---

E2E 测试在本项目里有 3 条硬约定：

1. **必须用 Playwright**（不用 Cypress / Selenium / 自研脚本）。
2. **本地必须 headed**（浏览器窗口在界面显示），禁止 `--headless` / `headless: true`；CI 环境才允许 headless。
3. **每一步关键操作都要截图**到 `test-results/<specName>/<NN>-<name>.png`，并在 `test.info().annotations` 推一条 `screenshot` 注解。

参考实现见 [[playwright-e2e-patterns]]（`evaluation/full-flow.spec.ts:21-24` 的 `shot()` helper 是事实标准）。

**Why：** 用户 2026-06-08 明确要求。E2E 既要自动化跑通也要能"在界面看到"，方便人工肉眼观察每一步、定位偶发 UI 问题、发现自动化跑过去但产品逻辑错的盲点。每步截图作为复盘依据（不靠事后重现失败现场）。

**How to apply：**

- 写新 E2E spec 时：先在 spec 顶部定义一个 `async function shot(page, name)`，参考 `evaluation/full-flow.spec.ts:21-24`，把 `page.screenshot({ path: 'test-results/<specName>/' + name + '.png', fullPage: true })` + `test.info().annotations.push({ type: 'screenshot', description: name })` 写进去。
- 截图命名规范：`<两位序号>-<动作或断言的简短中文拼音>.png`，如 `01-login-filled.png`、`07-eval-step2-modules-loaded.png`。
- 跑测试命令必须带 `--headed --workers=1`（`workers=1` 是为了避免 Vite 冷启动并发撞首屏超时，见 [[playwright-e2e-patterns]] 第 4 条）。
- 详细 checklist 见 `migrametric-web/tests/README.md` §E2E 测试约定，那是事实源；本条 memory 是跨会话的硬规则索引。
- 视觉回归测试（`tests/visual/`）也归这条管——同样 Playwright + headed + 截图（视觉回归本身就是靠截图对比）。

**关联 bug / 现象：**

- 在跑 PR `75d8d75 test(e2e): 新增P0核心流程E2E测试，补关键data-testid` 之前，8 个 spec 中只有 `evaluation/full-flow.spec.ts` 一个用了 `shot()` 主动截图，**违反新约定**。`migrametric-web/tests/e2e/project.spec.ts` / `evaluation.spec.ts` / `statistics.spec.ts` / `system-config.spec.ts` / `user.spec.ts` / `auth/login.spec.ts` 都需要按本规则补上 `shot()` + 分步截图调用。任务 `Task #4` 跟踪这次补强。
