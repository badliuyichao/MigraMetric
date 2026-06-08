# 界面测试目录

## 目录说明

- `e2e/` - E2E自动化测试，测试完整用户流程
- `visual/` - 视觉回归测试，检测UI视觉变化
- `fixtures/` - 测试数据，如测试用户、测试项目等
- `utils/` - 测试工具函数，如认证辅助、API辅助等
- `setup/` - 测试初始化脚本

## 运行测试

```bash
# 运行单元测试
pnpm test:unit

# 运行组件交互测试
pnpm test:interaction

# 运行E2E测试
pnpm test:e2e

# 运行视觉回归测试
pnpm test:visual
```

---

## E2E 测试约定

### 1. 必须用 Playwright
E2E 自动化测试**统一使用 [Playwright](https://playwright.dev/)**，禁止用 Cypress / Selenium / 自研脚本等其他方案。理由：
- 项目已配套 `playwright.config.ts`（含 e2e / visual 双 project + headed 开关）
- helpers（`tests/e2e/helpers/`）+ fixtures（`tests/fixtures/`）已按 Playwright 写好
- 跨浏览器能力 + trace / video / 截图原生支持，便于失败复盘

### 2. 必须 headed（浏览器在界面显示）
E2E 跑测试时**默认以 headed 模式运行**，浏览器窗口必须在界面显示，禁止 headless。

- 本地命令：`node node_modules/@playwright/test/cli.js test --headed --workers=1`
- 不允许加 `--headless` / 设置 `headless: true` / 删 `chromium.launch({ headless: false })`
- CI 环境才允许 headless（环境变量 `CI` 已存在时按 `playwright.config.ts` 自动切到 headless）

**Why**：方便人工肉眼观察每一步操作、定位偶发 UI 问题、发现自动化跑过去但产品逻辑错的盲点。

### 3. 每一步必须截图记录
每个 E2E 用例的**关键步骤**（登录、填表、提交、跳转、断言、错误分支）都要调一次 `page.screenshot()`，落到 `test-results/<specName>/<step>.png`，并在 `test.info().annotations` 推一条 `screenshot` 注解（参考 `tests/e2e/evaluation/full-flow.spec.ts:21-24` 的 `shot()` helper）。

#### 截图命名规范
`<两位序号>-<动作或断言的简短中文拼音>.png`，示例：
- `01-login-filled.png`（登录表单填好）
- `02-dashboard.png`（登录后落到 dashboard）
- `04-project-form-filled.png`（项目表单填好未提交）
- `07-eval-step2-modules-loaded.png`（评估 step 2 模块加载完）
- `11-eval-step4-results.png`（评估 step 4 结果展示）

#### 通用 helper
`tests/e2e/helpers/form-helpers.ts` 已有 `byTestId / pickFromSelect / fillInputNumber / callApi / clickButtonByText`；截图建议照 `full-flow.spec.ts:21` 的 `shot(page, name)` 在每个 spec 内本地定义一份（避免跨文件耦合）。

#### 失败兜底
`playwright.config.ts` 已设 `screenshot: 'only-on-failure'`，失败时 Playwright 会自动截图；本约定的"每一步截图"是**主动记录** happy path + 关键分支，与失败截图互补不冲突。

### 4. 评审 checklist
新写 / 改 E2E 时提交前自查：
- [ ] 用 Playwright（不是其他框架）
- [ ] 命令带 `--headed`（本地），或确认跑在 CI 环境
- [ ] 关键步骤都调了 `shot()`，文件名按 `<序号>-<中文拼音>.png` 规范
- [ ] `shot()` 输出路径形如 `test-results/<specName>/<step>.png`