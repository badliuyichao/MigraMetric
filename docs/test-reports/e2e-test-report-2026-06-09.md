# E2E 测试报告

- **报告日期**：2026-06-09
- **测试范围**：MigraMetric 全栈 E2E（前端 Vue 3 + 后端 Spring Boot 3.2）
- **测试工具**：Playwright（headed + 单 worker，遵循 `tests/README.md` §E2E 测试约定）
- **测试命令**：`node node_modules/@playwright/test/cli.js test --project=e2e --headed --workers=1`
- **运行环境**：前端 `http://localhost:3000`、后端 `http://localhost:8080`（均已启动，登录 admin/admin123）
- **总耗时**：约 5.3 分钟

## 一、汇总

| 指标 | 数量 |
| --- | --- |
| 用例总数 | 30 |
| 通过 | 26 |
| 失败 | 3 |
| 跳过 | 1（`CFG-006`，testuser 账号未初始化到 dev 库） |
| 通过率 | 86.7% |
| 截图总数 | 48 |

## 二、覆盖矩阵

| Spec | 阶段 | 通过 / 失败 / 跳过 | 备注 |
| --- | --- | --- | --- |
| `auth/login.spec.ts` | 第一阶段·认证 | 7 / 0 / 0 | E2E-001~004、006、007、008 |
| `evaluation/full-flow.spec.ts` | 第四阶段·P0 流程 | 0 / 1 / 0 | **E2E-FLOW-001 失败** |
| `evaluation.spec.ts` | 第四阶段·评估核心 | 5 / 0 / 0 | EV-001~005 |
| `project.spec.ts` | 第三阶段·项目管理 | 4 / 0 / 0 | PRJ-001~004 |
| `statistics.spec.ts` | 第五阶段·统计展示 | 4 / 0 / 0 | STA-001~004 |
| `system-config.spec.ts` | 第二阶段·系统配置 | 5 / 0 / 1 | CFG-001~005，CFG-006 skip |
| `user.spec.ts` | 第六阶段·用户与权限 | 1 / 2 / 0 | **USER-002、USER-003 失败** |

## 三、失败用例详情

### 1. E2E-FLOW-001（P0 核心流程 · 高优先级）

- **路径**：`登录 → 创建项目 → 四步评估 → 统计报告`
- **失败位置**：`tests/e2e/evaluation/full-flow.spec.ts:135` — `Step 3：填写指标`
- **错误信息**：`Test timeout of 120000ms exceeded` + `locator resolved ... but element is not visible`（`input[placeholder="请输入需要迁移的数据库表数量"]`）
- **根因分析**：
  1. `src/views/project/evaluate/index.vue:143` 用 `v-show="currentStep === 3"` 渲染 step3 panel，`v-show` 仅切 `display:none`，DOM 元素**始终存在**
  2. 测试代码 line 132 `await page.waitForLoadState('networkidle')` 触发时，step2 → step3 的 `POST /evaluations/{id}/modules` 可能仍在飞行中，`currentStep` 尚未 +1
  3. 截图 `test-results/full-flow/09-eval-step3-indicators.png` 中步骤条 active=1（仍是 step2），但 step3 内的 input 已存在于 DOM 中 → 命中 `not visible` 永真分支
  4. 测试代码 124 行等的是 step2 的 `POST /modules` 200 响应，但当 `modulesLoaded` 第一次已为 true 时 `loadAvailableModules()` 不会重新跑，保存模块逻辑只在该分支触发，**`currentStep++` 实际在 `saveModuleConfig` 完成后才执行**，与测试等待语义错位
- **修复建议**：
  - 短期：在测试 `Step 3` 入口前显式 `await expect(page.locator('input[placeholder="..."]')).toBeVisible({ timeout: 15000 })` 替代 `waitForLoadState('networkidle')`
  - 长期：`evaluate/index.vue` 把 step panel 改回 `v-if`（销毁/重建 DOM），避免隐藏元素干扰定位
- **证据**：`test-results/evaluation-full-flow-P0-核心-d1c82-001-登录-→-创建项目-→-四步评估-→-统计报告-e2e/test-failed-1.png` + `trace.zip`

### 2. USER-002（普通用户调 admin-only user API 被拒绝 · 中优先级）

- **路径**：`tests/e2e/user.spec.ts:95`
- **错误信息**：`expect(received).toBeGreaterThanOrEqual(400)` 实际收到 `200`
- **根因分析**：
  - `migrametric-server/src/main/java/com/migrametric/controller/user/UserController.java` 整文件**未配置任何 RBAC 注解**（无 `@PreAuthorize`、`@RequiresRoles`、`@Secured`）
  - 当前实现只验证 JWT 合法性（`JwtAuthenticationFilter`），不校验角色
  - `PERMISSION_DENIED(11001, "权限不足")` 业务码（`ResultCode.java:41`）已定义但无拦截器使用
- **影响**：
  - 普通用户（USER 角色）可列出 / 创建 / 编辑 / 启禁 / 删除用户，**违反 §1.3 用户角色职责**
  - 前端菜单虽未暴露用户管理入口（`src/views/` 无 `user/` 目录），但 API 层是开放的
- **修复建议**：
  - `SecurityConfig` / 自定义拦截器增加 `@PreAuthorize("hasRole('ADMIN')")` 到 `UserController` 全路径
  - 或在 `UserController` 类级别加 `@PreAuthorize("hasRole('ADMIN')")`，方法级覆盖
  - 启用 Spring Security 的方法级安全（`@EnableMethodSecurity`）
- **证据**：`test-results/user-第六阶段·用户与权限-E2E-USER-002-普通用户调-admin-only-user-API-被拒绝-e2e/test-failed-1.png`

### 3. USER-003（禁用用户登录失败 · 中优先级）

- **路径**：`tests/e2e/user.spec.ts:144`
- **错误信息**：`expect(received).toBe(400)` 实际收到 `200`
- **根因分析**：
  - `AuthService.java:45-46` **正确抛出** `BusinessException(ResultCode.USER_DISABLED)`
  - 但 `GlobalExceptionHandler.java:33` `@ResponseStatus(HttpStatus.OK)` 把所有 `BusinessException` 一律返回 HTTP 200，业务码 10005 仅在 `Result` body 的 `code` 字段
  - 测试断言期望 HTTP 400，而当前实现是 HTTP 200 + `code=10005`
  - **结论**：这是**测试期望值与产品 HTTP 语义不一致**——产品用"业务码"区分错误类型，测试用 HTTP 状态码区分
- **影响**：
  - 前端 `request.ts` 拦截器对 HTTP 200 不触发 `handleUnauthorized`（仅 401 触发），前端能正确从 `body.code === 10005` 渲染错误 toast（`tests/e2e/auth/login.spec.ts:127` 的 E2E-008 用例已通过验证）
  - 业务功能**正常**，但测试断言写错
- **修复建议**（二选一）：
  - **修测试**（推荐）：`expect(failLogin.status()).toBe(200)` + `expect(failBody.code).toBe(10005)`，对齐产品"业务码"语义
  - **改产品**：`GlobalExceptionHandler` 对认证类业务码（`USERNAME_PASSWORD_ERROR` / `USER_DISABLED` / `USER_LOCKED`）映射到 401，前端拦截器对 401 触发统一处理
- **证据**：`test-results/user-第六阶段·用户与权限-E2E-USER-003-禁用用户登录失败（端到端：建→禁用→登录拒绝）-e2e/test-failed-1.png`

## 四、跳过用例说明

| 用例 | 原因 |
| --- | --- |
| `CFG-006` | `tests/fixtures/users.ts` 定义了 `testuser`，但 `migrametric-server/src/main/resources/init-db.sql` 未建该账号。跳过避免对线上 dev 库产生污染。 |

## 五、截图清单

完整截图位于 `migrametric-web/test-results/<specName>/`，共 48 张：

- `auth/login.spec.ts` → 8 张（登录/登出/会话过期/禁用用户全流程）
- `project.spec.ts` → 8 张（创建/列表/搜索/筛选/详情/校验）
- `evaluation.spec.ts` → 5 张（公式断言后的统计页）
- `evaluation/full-flow.spec.ts` → 9 张 + 1 张失败截图 + 1 个 trace.zip（step 1-2 完整，step 3 起失败）
- `statistics.spec.ts` → 4 张（总工作量/饼图/概览/风险）
- `system-config.spec.ts` → 11 张（系统类型/模块/阶梯/报表配置）
- `user.spec.ts` → 失败用例 2 张失败截图（USER-001 通过无断言截图）

## 六、环境核查

| 项 | 状态 |
| --- | --- |
| MySQL 8.0 dev 库 | 已就绪（admin 用户、表/阶梯/项目数据可见） |
| 后端 8080 | 200/403 正常响应 |
| 前端 3000 | 200 正常响应 |
| Playwright 浏览器 | headed 模式启动成功 |

## 七、与昨日对比

未找到 `2026-06-08` 日期命名的测试报告（仓库内全文搜索 "测试报告" 关键字无结果，`docs/test-reports/` 目录首次创建），无法进行版本对比。本报告为首份 E2E 报告。

## 八、结论

- 6 个 spec 中 4 个完全通过，2 个存在缺陷
- **E2E-FLOW-001 是 P0 阻塞项**：P0 主流程跑不通，发布前必须修复
- **USER-002 是安全阻塞项**：用户管理 API 无 RBAC 拦截，违反 §1.3 角色职责
- **USER-003 是测试代码 bug**：业务码语义与 HTTP 状态码断言错位，产品功能实际正常
- 26 个非失败用例的截图均可作为视觉/行为基线

## 九、修复后回归（2026-06-09 下午）

### 改动清单

| 文件 | 改动 | 性质 |
| --- | --- | --- |
| `src/views/project/evaluate/index.vue:660` | `handleSelectionChange` 给 selection 显式设 `checked=true` | **产品 bug**（E2E-FLOW-001 真实根因） |
| `src/views/project/evaluate/index.vue:30/82/143/283` | 4 个 step panel `v-show` → `v-if` | 防御性 |
| `tests/e2e/evaluation/full-flow.spec.ts` | step3 入口加 `btn-calculate` 显式 visible 等待 | 测试健壮性 |
| `server/.../controller/user/UserController.java` | 类级别 `@PreAuthorize("hasRole('ADMIN')")` | **USER-002 安全修复** |
| `server/.../controller/user/UserController.java` | 所有 `@PathVariable` / `@RequestParam` 显式 name | 编译兼容性 |
| `server/.../common/GlobalExceptionHandler.java` | 加 `AccessDeniedException` → 403 + `AuthenticationException` → 401 handler | 错误响应规范化 |
| `tests/e2e/user.spec.ts:172-175` | USER-003 断言改业务码 (HTTP 200 + code=10005) | **USER-003 测试断言错位修复** |
| `tests/e2e/user.spec.ts:26` | `UNIQUE()` 缩短到 11 字符内，避开 `validateUsername` 4-20 字符限制 | **新发现的隐性 bug** |

### 回归结果

**29 通过 / 0 失败 / 1 跳过**（4.2 分钟跑完）

```
✓ E2E-001~004, E2E-006, E2E-007, E2E-008   7/7  login
✓ E2E-FLOW-001                             1/1  full-flow
✓ EV-001~005                               5/5  evaluation
✓ PRJ-001~004                              4/4  project
✓ STA-001~004                              4/4  statistics
✓ CFG-001~005                              5/5  system-config
- CFG-006                                  0/0  skip（testuser 未在 init-db.sql 初始化）
✓ USER-001~003                             3/3  user
```

### 修复过程中挖出的额外问题

1. **`<parameters>true</parameters>` 在 maven-compiler-plugin 未生效** —— 表现是 PUT / RequestParam 路径变量拿不到方法参数名 → `IllegalArgumentException` 500。**已用显式 `@PathVariable("name")` 兜底**。建议后续对其他 10 个 Controller 统一 replace_all 修一遍，避免下次再踩。
2. **测试 fixture `UNIQUE()` 生成的用户名 > 20 字符** —— `validateUsername` 规则是 4-20 字母数字下划线，触发 form rule 校验失败而非后端业务码。改短后通过。
