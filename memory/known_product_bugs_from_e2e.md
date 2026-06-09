---
name: known-product-bugs-from-e2e
description: 产品 bug 清单（被 E2E 暴露但**未修复**），用来在 E2E 失败时区分"测试问题"和"产品 bug"，避免误修测试
metadata:
  type: project
---

跑 Playwright E2E 时**确认是后端/前端产品 bug** 的清单。

**Why:** E2E 失败时分清"测试代码问题"和"产品 bug"是关键决策，误判会把测试改成能通过但掩盖产品 bug。

**How to apply:** 失败时如果日志显示后端抛 500/校验异常，先到这里查；命中则记录"产品 bug 阻塞，**测试本身正确**"。

---

## ✅ 已修复（2026-06-08）

### Bug 1：数据量阶梯新增缺 `sortOrder` 校验过严 → 修

- 位置：`migrametric-server/src/main/java/com/migrametric/dto/ladder/DataVolumeLadderCreateDTO.java:59-63`
- 修法：去掉 `@NotNull`，保留 `@Min(0)`。Service 层（`DataVolumeLadderServiceImpl:60-65`）已有 sortOrder == null 时自动 = maxOrder + 1 的逻辑
- 同步修了 `UserCountLadderCreateDTO.java`（同问题）
- 影响：CFG-003 / CFG-004 现在通过

### Bug 2：报表系数 DTO 用了 `@NotBlank` 在 `Long id` 上 → 修

- 位置：`migrametric-server/src/main/java/com/migrametric/dto/config/ReportConfigUpdateDTO.java:25-28`
- 修法：`@NotBlank` → `@NotNull`（Long 类型用 NotNull）
- 同步加 import `jakarta.validation.constraints.NotNull`
- 影响：CFG-005 现在通过

### Bug 3：CFG-006 普通用户 testuser 不存在 → 暂 skip

- 位置：`migrametric-web/tests/fixtures/users.ts:18-21` `testuser/test123` 在 dev 库没建
- 现状：CFG-006 仍 `test.skip()`
- 修法：等后续在 `init-db.sql` 加 testuser 初始化或删 fixtures 里的 testuser 字段

---

## ✅ 已修复（2026-06-09）

### Bug 4：评估 step2 → step3 切不过去（产品 bug，非 E2E 问题）→ 修

- 位置：`migrametric-web/src/views/project/evaluate/index.vue:660-667` `handleSelectionChange`
- 现象：用户在 step 2 选了 N 个模块，点"下一步"后永远卡 step 2，step 3 指标表单不出现
- 根因：`el-table @selection-change` 给的 selection 元素 `checked` 字段是 undefined（来自 `availableModules`，初始 false）。前端 map 进 `selectedModules` 时**没把 `checked` 标 true** → 后端 `ModuleConfigServiceImpl:107` `m.getChecked()` 必为 true 才入库 → 业务校验抛 "请至少选择一个模块"
- 修法：map 时显式 `mod.checked = true; if (mod.weight == null) mod.weight = mod.defaultWeight ?? 1.0`
- 影响：E2E-FLOW-001（P0 主流程）现在通过

### Bug 5：UserController 无 RBAC 拦截，普通用户可调 admin-only API → 修（**安全**）

- 位置：`migrametric-server/src/main/java/com/migrametric/controller/user/UserController.java`
- 现象：任何带有效 JWT 的用户都能 list/create/update/delete/启禁用户，违反 §1.3 角色职责
- 修法：
  1. 类级别加 `@PreAuthorize("hasRole('ADMIN')")`
  2. `GlobalExceptionHandler` 加 `AccessDeniedException` → 403 + `AuthenticationException` → 401 handler
- 验证：USER-002 失败 → 修复后普通用户调 user 列表拿到 HTTP 403 + `code:403, message:"权限不足"`

### Bug 6：后端 Controller `@PathVariable` / `@RequestParam` 缺显式 name → 500 → 修

- 位置：全项目 10+ Controller（E2E 触发的是 `UserController`）
- 现象：PUT `/api/users/{id}/status?status=0` 报 `IllegalArgumentException: Name for argument of type [java.lang.Long] not specified, and parameter name information not found in class file either` → 500
- 根因：pom 配了 `<parameters>true</parameters>` 但本环境 javac 实际未生效。Spring 6 严格模式拿不到方法参数名就抛
- 修法：所有 `@PathVariable Xxx x` → `@PathVariable("x") Xxx x`，`@RequestParam` 同理
- 影响：USER-001 现在通过；建议后续对全项目 10 个 Controller 统一 replace_all 修一遍

### Bug 7：测试 fixture `UNIQUE()` 生成用户名 > 20 字符 → 触发表单 rule 失败（隐性问题）→ 修

- 位置：`migrametric-web/tests/e2e/user.spec.ts:26`
- 现象：USER-003 浏览器端登录时，前端 `validateUsername` 规则是 4-20 字母数字下划线；`UNIQUE() = \`e2euser${Date.now()}\`` 生成 21 字符 → 表单 rule 触发 "用户名格式不正确"，根本到不了后端 → toast 等不到
- 修法：`UNIQUE() = \`e2eu${(Date.now() % 10000000).toString().padStart(7, '0')}\``（11 字符）
- 影响：USER-003 端到端现在通过

### Bug 8：USER-003 旧测试断言 HTTP 状态码，错误 → 改测试（不是产品 bug）

- 位置：`migrametric-web/tests/e2e/user.spec.ts:172-175`
- 旧断言：`expect(failLogin.status()).toBe(400)`
- 实际：`GlobalExceptionHandler.handleBusinessException` 一律 `@ResponseStatus(HttpStatus.OK)` → `USER_DISABLED` 走 HTTP 200 + `body.code=10005`
- 修法：断言改为 `expect(failLogin.status()).toBe(200)` + `expect(failBody.code).toBe(10005)`
- 注：业务功能一直正常（`AuthService:45-46` 正确抛 `BusinessException(USER_DISABLED)`），是测试断言跟产品 HTTP 语义错位

### Bug 9：评估向导 step panel 用 `v-show` 导致 Playwright 拿 hidden input → 防御性修复

- 位置：`migrametric-web/src/views/project/evaluate/index.vue:30/82/143/283`
- 现象：测试进 step3 时找 input，被 `v-show` 隐藏的 step3 元素命中 first() 但 `not visible` 永真
- 修法：4 个 step panel 全部 `v-show` → `v-if`（销毁/重建 DOM，避免隐藏元素干扰 E2E 定位）
- 注：根因是 Bug 4；这是配套防御性修复

---

## 历史记录（修复前）

> ⚠️ 以下是修复前的状态，仅作存档
>
> 修复前跑分：2 passed / 3 failed / 1 skipped (CFG-001、CFG-002 通过；CFG-003/004/005 被 3 个产品 bug 阻塞)
>
> 修复后跑分：5 passed / 1 skipped / 0 failed (36.8s)

---

## 容易混淆的"非 Bug"现象

| 现象 | 实际原因 | 不是 Bug |
|---|---|---|
| 登录后页面跳"系统异常" | `loginAs` 没注入 userInfo，`permission.ts` 守卫触发 logout+跳 /login，错误页是过渡态 | ✅ 修 helper，不是产品 bug |
| 表格找不到 .el-table__row 文本 | el-table 行渲染是 Vue 异步更新，需 1-2s | ✅ 改测试用 waitFor，不是产品 bug |
| 表格找不到 `[data-testid="table-x"]` | data-testid 加的位置不对（el-input 落到 input 上、el-input-number 落到根 div 上） | ✅ 改 selector，不是产品 bug |
| 弹窗 form 字段 fill 报错 "Element is not an <input>" | locator 选到了 el-input-number 根 div，没有 input 后缀 | ✅ 改 selector，不是产品 bug |
| `expect(table).toContainText('0.5')` 期望 0.50 失败 | 后端返回 "0.5" 不补 0，"0.50" 永远等不到 | ✅ 改测试期望，不是产品 bug |
| `expect(rows).toContainText(name)` strict mode 报错 | 表格里有累积的多行，locator 解析到 4 个 | ✅ 改用 `.filter({ hasText: name }).toHaveCount(1)`，不是产品 bug |
