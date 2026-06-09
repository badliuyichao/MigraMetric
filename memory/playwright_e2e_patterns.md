---
name: playwright-e2e-patterns
description: Playwright E2E in MigraMetric — gotchas for login/Session/登出 scenarios, Vite cold-start timing, and runner flags that actually keep tests green
metadata:
  type: feedback
---

MigraMetric 项目跑 Playwright E2E 时踩过的坑与对应做法。

**Why:** 初次跑 `tests/e2e/auth/login.spec.ts` 10 条 6 败；按下方模式调整后 7/7 全过（37.6s）。这些不是通用最佳实践，是本项目具体实现决定的。

**How to apply:** 写/改 E2E 用例时，遇到登录态、跳转、弹窗、URL glob 等场景优先用以下做法。

---

### 1. 模拟"Session 过期"必须清空 token，**不要**注入 expired-token
- `axios` 拦截器 `request.ts:74-83` 只对 401 触发 `handleUnauthorized()`（清 token + 跳 /login）
- 后端 `JwtAuthenticationFilter` 对 invalid/expired token 返回 **403**（不是 401）
- 注入 `localStorage.setItem('token', 'expired-token')` 不会跳登录页
- 正确做法：`localStorage.removeItem('token')` + `localStorage.removeItem('userInfo')` → 触发 `permission.ts:25-32` 的"无 token 跳 /login"分支

### 2. 登出有"确定/取消"确认弹窗，**不能**只点登出项
- `layouts/index.vue:140-147` `handleCommand('logout')` 调 `ElMessageBox.confirm('确定要退出登录吗？')`
- 弹窗按钮 class：`.el-message-box__btns .el-button--primary`（"确定"）
- 选 `text=确定` 也行，但 class 更稳（不依赖 i18n）

### 3. `waitForURL` glob 对带 query 的 URL 不可靠
- `**/login*` 在 `/login?redirect=%2Fproject%2Flist` 上 Playwright 1.60 表现不稳定
- 改成正则：`/\/login(\?.*)?$/`
- 登录后跳 `/` 再被路由重定向到 `/dashboard`，用 `**/dashboard` glob 是 OK 的（路径无 query）

### 4. Vite 冷启动 + 4 worker 并发会让首条 E2E 偶发 timeout
- Vite 首次按需优化依赖（element-plus 等）会拉长首屏 ~5-10s
- 4 worker 同时起会同时撞冷启动
- 推荐：本地开发用 `node node_modules/@playwright/test/cli.js test --headed --workers=1` 串行
- CI 用 `workers=1` + `retries=2`（playwright.config.ts 已有，但 `expect.timeout` 可考虑调到 8000ms）

### 5. 失败时**先**对照设计文档判定，再决定改测试还是改产品
- `docs/architecture/产品设计文档.md` §9 安全设计 + §8 认证授权规范是登录/会话的权威
- `docs/architecture/需求说明文档.md` §4.3 安全性需求（用户认证、会话管理）
- 判定顺序：①文档有硬性要求但产品没做 → 产品 bug；②文档没要求但测试断言了 → 删/改测试；③两者都做了但 E2E 失败 → 测试代码 bug
- 之前的 E2E-005（记住密码）/E2E-009（重置）/E2E-010（忘记密码）属第②类，文档无依据 → 删除

### 6. 跑命令的"在我这台机器上"的具体路径
- `pnpm` 没装（环境里只有 `npm`），启动 vite 用 `node node_modules/vite/bin/vite.js`
- 跑测试用 `node node_modules/@playwright/test/cli.js test ...`（等价于 `npx playwright test`）
- 看 trace：`npx playwright show-trace test-results/<dir>/trace.zip`
- 截图/trace 输出到 `test-results/`，已在 `.gitignore`

### 7. 设计文档对"前端必须有 X 元素"几乎不硬性规定
- §9.1.1 只要求"用户名密码登录"，**不要求**前端有"记住密码/忘记密码/重置"
- §9.1.2 只要求"JWT 2h 过期"，**不要求**前端必须有"退出按钮的可见 testid"
- 写 E2E 时优先用组件已有的 testid；缺 testid 时**最小化**补一个即可（参考 `layouts/index.vue:77/87` 补 `user-menu` / `logout-button`），不要顺手改产品功能

### 8. element-plus 组件的 data-testid 落点不统一

写 SFC 时给 `el-input` / `el-input-number` / `el-select` / `el-radio-group` / `el-date-picker` / `el-descriptions` 加 `data-testid`，**编译后落点不同**：

| SFC 写法 | 编译后 data-testid 落在 | Playwright selector | inheritAttrs |
|---|---|---|---|
| `<el-input data-testid="form-x">` | `<input class="el-input__inner">` | `[data-testid="form-x"]` 直接 fill | true |
| `<el-input-number data-testid="form-x">` | 组件根 `<div class="el-input-number">` | `[data-testid="form-x"] input` | true |
| `<el-select data-testid="form-x">` | 组件根 `<div class="el-select">` | `[data-testid="form-x"] .el-select__wrapper` | true |
| `<el-radio-group data-testid="form-x">` | 组件根 `<div class="el-radio-group">` | `[data-testid="form-x"] .el-radio` | true |
| `<el-button data-testid="btn-x">` | `<button>` 元素 | `[data-testid="btn-x"]` | true |
| `<el-table data-testid="table-x">` | `<div class="el-table">` | `[data-testid="table-x"]` | true |
| **`<el-date-picker data-testid="form-x">`** | **（testid 丢失！）** | **不要加 testid，用 placeholder 定位 input** | **false** |
| **`<el-descriptions data-testid="desc-x">`** | **（testid 丢失）** | **不要加 testid** | **false** |

**Why:** 第一次写 project.spec.ts 给 el-date-picker 加了 data-testid，运行时 DOM 里完全找不到。el-date-picker 内部 `inheritAttrs: false`（编译产物 `null, 8, ["modelValue"]` 第二参 8 = FALLTHROUGH = false），导致 attribute 被 Vue 吞掉。

**两种解法**：
- A) 不在 el-date-picker 上加 testid，直接用 `page.locator('input[placeholder="请选择评估日期"]')`
- B) 外包一层 `<div data-testid="...">` 兜底

**推荐 A**，更简单。

### 9. 注入 token 时必须同时注入 userInfo
`router/permission.ts:35-46` 的守卫逻辑：若 `!userStore.userInfo` 就调 `getUserInfo()`，401 失败会调 `logout()` + 跳 /login。

`loginAs()` helper 必须：
1. POST `/api/auth/login` 拿 token
2. GET `/api/auth/info` 拿 userInfo
3. **两个一起**写入 localStorage
4. 再 goto /dashboard

只写 token 而不写 userInfo 会让 admin 也被守卫弹回登录页（错误页看起来像"系统异常"），但 root cause 是守卫行为。

### 10. 表格新行渲染有 1-2s 延迟，搜索断言别用 toContainText 改用 waitFor

CFG-002 第一次跑失败原因：搜索接口返回 Total: 1 但 el-table__row 还没渲染（Vue 异步更新）。
改用 `await page.waitForSelector('[data-testid="table-x"] .el-table__row:has-text("...name")', { timeout: 5000 })` 比 `expect(...).toContainText()` 更稳。
但 `:has-text()` 是 Playwright 引擎级伪类，配合 .el-table__row 即可定位。

### 11. 评估模块 API 正确调用顺序（第四阶段实战）

E2E 写评估测试时**容易踩的坑**：

1. `POST /api/projects` 建项目（DRAFT 状态）
2. `POST /api/evaluations/{id}/modules` 保存模块选择
   - DTO 字段是 `{moduleId, weight(必填), checked}` —— **不是 `selected` / `customWeight`**
3. `GET /api/ladder/data-volume/match?volume=X` 和 `GET /api/ladder/user-count/match?count=X` 拿 `ladderId`
4. `PUT /api/evaluations/{id}/indicators` 保存指标
   - **必传 `dataVolumeLadderId` 和 `userCountLadderId`**（不是 `/metrics`）
   - 后端 `EvaluationUpdateDTO` 校验失败返 400
5. `POST /api/evaluations/{id}/calculate` 计算

**workload 返回值字段**：`moduleWorkloads[].moduleWorkload`（不是 `workload`）。

**Why:** 前端 `saveIndicators` 函数名误导（实际是 PUT /indicators），DTO 字段名跟前端 store key 不一致（customWeight → weight, selected → checked）。第一次写时只能一次次踩日志才能发现。

### 12. 后端 saveModuleConfig 不会读模块默认系数

DTO `weight` 是 `@NotNull` 必填。后端 `ModuleConfigServiceImpl.saveModuleConfig` **直接把 dto.getWeight() 写库**，**不查模块默认系数 fallback**。

**Why:** 第一次跑 EV-001 时模块 B 不传 customWeight，期望"用默认 1.5"，但后端收到 `weight: 1.0`（我代码里 `?? 1.0`），算出来 ≠ 公式预期。

**修法**：spec 里所有模块都**显式传 customWeight**，别依赖默认系数。

### 13. el-card `v-loading="loading"` 在加载中时整张卡片不可见

`<el-card v-loading="loading">` 时，**子元素的 visibility/display 不变，但 Playwright 的 `toBeVisible` 仍能看见** —— 实际上 el-card 内部会加 `.is-loading` class + 蒙层，**子元素会被遮挡**。

**Why:** statistics 页加载统计接口时，el-card 处于 loading 态。等接口返回后，loading=false，蒙层消失。spec 直接 `toBeVisible` 元素在 15s 内经常失败。

**修法**：在 toBeVisible 之前 wait for loading 状态消失：
```ts
await page.waitForFunction(
  () => !document.querySelector('.el-card.is-loading'),
  { timeout: 20000 }
).catch(() => { /* 容忍超时 */ })
```

### 14. step panel 用 `v-show` 会让 E2E 命中 hidden input → 永真失败（2026-06-09）

`v-show` 切 `display: none` 但 DOM 元素**始终存在**。`page.locator('input[placeholder="..."]').first()` 会拿到 hidden input，Playwright 报 `element is not visible`，5s/15s timeout 永远命中。

**Why:** 评估向导 4 个 step panel（`src/views/project/evaluate/index.vue:30/82/143/283`）最初用 `v-show`，step 2 → step 3 切完但测试进 step3 时，step3 panel 里的 input 仍 hidden。

**修法**（任选）：
- 改产品：`v-show` → `v-if`（销毁/重建 DOM）
- 改测试：用更精确 selector 限定到当前 step 容器

**推荐改产品**。`v-if` 还减少初始 DOM 体积。

### 15. 后端 Controller `@PathVariable` / `@RequestParam` 必须显式 name（2026-06-09）

Pom 配了 `<parameters>true</parameters>` 期望 javac 写入参数名到 class，Spring 反射时能拿。但本环境实际不生效，PUT `/api/users/{id}/status?status=0` 触发：

```
java.lang.IllegalArgumentException: Name for argument of type [java.lang.Long] not specified,
and parameter name information not found in class file either.
```

**Why:** Spring 6 + Security 6 严格模式，@PathVariable / @RequestParam 必须有显式 name 才能解析；否则抛 500。

**修法**：所有 Controller 方法签名从 `@PathVariable Long userId` 改为 `@PathVariable("userId") Long userId`，`@RequestParam` 同理。

**影响范围**：全项目 10 个 Controller（UserController / ProjectController / ModuleController / Ladder*Controller / SystemTypeController / StatisticsController / EvaluationController / ExportController / Log*Controller）。E2E 跑过的链路里只有 UserController 的 PUT/DELETE 路径被覆盖到（USER-001 失败），其他 Controller **仍有同样隐患**，等下次 E2E 覆盖到对应 PUT/DELETE 路径时会暴露。建议**预防性全项目 replace_all 修一遍**。

### 16. E2E 测试 fixture 用户名长度必须满足前端 `validateUsername` 4-20 规则（2026-06-09）

`UNIQUE() = \`e2euser${Date.now()}\`` 生成 `e2euser1780971659725d` = 21 字符，**超过前端 `validateUsername` 的 4-20 限制**。浏览器端登录时表单 rule 触发 "用户名格式不正确" 红字，**根本到不了后端**，E2E 等的 `.el-message--error` 浮动 toast 永远不出现。

**Why:** 修复 USER-003 时把后端业务码断言改对后，前端这条隐性问题才暴露。后端用户名表是 VARCHAR(50)，不限长度，但前端 form rule 限 4-20。

**修法**：
```ts
// 11 字符总长
const UNIQUE = () => `e2eu${(Date.now() % 10000000).toString().padStart(7, '0')}`
```

**Why 是 Date.now() % 10000000**：保 7 位数字 + padStart，10s 内不冲突（10s 内 Date.now() 末 7 位必不同），跨 spec 隔离靠 spec 内部变量作用域。

### 17. el-table @selection-change 的 selection 元素要显式标 checked=true（2026-06-09）

后端 `ModuleConfigServiceImpl:107` 要求 `m.getChecked() === true` 才入库。但 `el-table @selection-change` 给的 selection 元素**直接来自 `availableModules`**，`checked` 字段初始 false。

**Why:** 前端 `handleSelectionChange` 只是把 selection 元素 push 进 `selectedModules`，没改 `checked` 字段。用户明明勾选了模块（el-table UI 状态 checked），但 selectedModules 数组里对象的 `checked: false`，后端拿到 false → 业务校验抛 "请至少选择一个模块"。

**修法**：
```ts
function handleSelectionChange(selection: ModuleConfigItem[]) {
  selectedModules.value = selection.map(mod => {
    mod.checked = true  // 关键：selection 里的就是用户选中的
    if (mod.weight == null) mod.weight = mod.defaultWeight ?? 1.0
    return mod
  })
}
```
