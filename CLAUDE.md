# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 提供代码库工作指南。

## 项目概述

MigraMetric 是一个全栈 Web 应用，用于评估企业异构系统迁移工作量。通过分析系统架构、功能模块和技术栈，帮助企业估算迁移成本和时间周期。

## 开发规则

### 通用编码规则

- 先读取所有相关文件（含测试文件），测试定义了通过标准。
- 一次性写完整解决方案，不要增量修改。
- 测试只跑一次：通过则立即停止；失败则读错误信息，修复一次，重新测试。
- 同一个失败不要迭代超过一次，卡住时重新思考思路。
- 通过的代码不要重构、优化或润色。
- WebSocket：用 Set 手动追踪客户端，先发给发送者，再通过 setTimeout(0) 广播给其他人，禁止使用 pub/sub 通道。

### 后端规则

- **后端改了 RBAC / 新 Controller / SecurityConfig 后必须重启后端**（Spring Boot DevTools 在 maven fork 模式不自动重载新加的注解类），kill 当前 8080 java 进程 + `mvn spring-boot:run` 重启，**不询问用户**。
- **后端 Controller `@PathVariable` / `@RequestParam` 必须显式指定 name**（`@PathVariable("userId") Long userId`），pom 配的 `<parameters>true</parameters>` 在本环境不稳定，没显式 name 会导致 `IllegalArgumentException: Name for argument ... not specified` 500。
- **评估向导步骤面板优先用 `v-if` 而非 `v-show`**：`v-show` 隐藏的元素仍在 DOM 中，Playwright 拿到的 input 是 hidden → 命中 first() 后 `not visible` 永真，导致步骤切换后的断言失败。

### E2E 测试规则

- E2E 必须使用 Playwright，本地必须以有头模式（浏览器窗口可见）运行，每一步关键操作都要截图到 `test-results/<specName>/<NN>-<name>.png`。完整清单见 `migrametric-web/tests/README.md` §E2E 测试约定。
- **E2E 测试用户名/密码等测试夹具必须满足前端 `validateUsername` 4-20 位字母数字下划线规则**。`UNIQUE() = \`e2euser${Date.now()}\`` 会生成 21+ 字符超长，触发表单规则校验失败而非后端业务码。建议 `UNIQUE() = \`e2eu${(Date.now() % 10000000).toString().padStart(7, '0')}\``（11 字符）。

## 环境要求

- Java 17+ / Maven 3.8+
- Node.js 18+ / pnpm 8+
- MySQL 8.0

## 开发命令

### 后端 (migrametric-server)
```bash
cd migrametric-server
mvn spring-boot:run              # 启动开发服务器 (端口 8080)
mvn test                          # 运行所有测试
mvn test -Dtest=ClassName        # 运行指定测试类
mvn test jacoco:report            # 生成覆盖率报告
mvn clean package -DskipTests     # 构建 JAR 包
```

### 前端 (migrametric-web)
```bash
cd migrametric-web
pnpm install                      # 安装依赖
pnpm dev                         # 启动开发服务器 (端口 3000，自动打开浏览器)
pnpm build                       # 生产环境构建
pnpm lint                        # ESLint 检查并自动修复
pnpm type-check                  # TypeScript 类型检查
pnpm test:unit                    # 运行单元测试 (单次执行)
pnpm test:coverage               # 生成覆盖率报告
```

### E2E (migrametric-web)
```bash
cd migrametric-web
node node_modules/@playwright/test/cli.js test --project=e2e --headed --workers=1
```

报告落到 `docs/test-reports/e2e-test-report-YYYY-MM-DD.md`，截图到 `migrametric-web/test-results/<specName>/`。

## 测试与 Bug 修复流程

```
阶段 1            阶段 2            阶段 3                阶段 4            阶段 5
全场景执行  ──→  Bug 登记  ──→  逐个修复+回归  ──→  全量回归  ──→  归档
  │                │                │                    │                │
  │ 0失败?──→ 跳到阶段5 │                │                    │ 100%通过?      │
  │                │ 全部登记完?      │ 每修1个立即回归       │  ├─ 是→已关闭   │
  │                │  ├─ 否→禁止修   │  ├─ 通过→回归通过     │  └─ 否→回阶段2  │
  │                │  └─ 是→开始修   │  └─ 失败→回修复中    │                │
  ▼                ▼                ▼                    ▼                ▼
记录失败        写bug-tracker     独立提交              跑全量E2E        附加到测试报告
                                  fix(scope):BUG-id   +mvn test        更新plan.md
```

| 阶段 | 动作 | 产出 | 门禁条件 |
|------|------|------|----------|
| **1. 全场景执行** | E2E `--headed --workers=1` + `mvn test` + `pnpm test:unit` | 失败用例清单（spec/用例/错误/截图） | 无失败→跳到阶段 5 |
| **2. Bug 登记** | 每个失败分配 `BUG-{YYYYMMDD}-{NN}`，写入 `docs/test-reports/bug-tracker-YYYY-MM-DD.md` | Bug 追踪文档（状态：待修复） | **全部登记完才能进阶段 3** |
| **3. 逐个修复+回归** | 按 P0阻塞 > P1安全/功能 > P2界面 顺序修，每修一个立即 `-g "用例名"` 回归 | 独立 commit `fix({scope}): BUG-{id} {简述}`，状态→回归通过 | 同一点失败>2次→升级分析 |
| **4. 全量回归** | 全量 E2E + 单测，必须 100% 通过（已知跳过除外） | 全量通过→所有 Bug 状态改为已关闭 | **不通过→回阶段 2 重新登记** |
| **5. 归档** | Bug 追踪文档附加到测试报告，更新 plan.md | `e2e-test-report-YYYY-MM-DD.md` | — |

**硬规则**：阶段 2 完成前禁止修代码 · 阶段 4 完成前禁止标记功能完成 · Bug 状态流转：`待修复→修复中→回归通过→已关闭` / `不予修复`

## 架构设计

### 后端 (Spring Boot 3.2)
标准三层架构：`controller/` → `service/` → `mapper/`

- `entity/` — 数据库实体类（JPA 风格，使用 MyBatis-Plus）
- `dto/` — 请求 DTO，用于 API 输入
- `vo/` — 响应视图对象
- `common/` — Result 统一响应包装、ResultCode 枚举、全局异常处理器
  - `GlobalExceptionHandler` 统一映射：`BusinessException` → HTTP 200 + 业务码、`@Valid` 失败 → 400、`AccessDeniedException`（@PreAuthorize 拒绝）→ 403、`AuthenticationException` → 401、其他 → 500
- `config/` — CORS、Swagger/OpenAPI、JWT、Security（`@EnableMethodSecurity` 启用方法级安全）

**RBAC 规范**：admin 专用 API（如 `UserController`）在类级别加 `@PreAuthorize("hasRole('ADMIN')")`。`JwtAuthenticationFilter` 注入的 authority 是 `ROLE_<role>` 形式。

统一响应格式通过 `Result<T>` 类实现。Swagger UI 地址：`/swagger-ui.html`。

### 前端 (Vue 3 + TypeScript + Vite)
- `views/` — 路由级页面组件
- `components/` — 可复用 UI 组件
- `composables/` — Vue 组合式函数共享逻辑
- `stores/` — Pinia 状态管理
- `api/` — Axios API 模块（每个后端控制器对应一个文件）
- `utils/request.ts` — Axios 封装，包含 JWT 拦截器和错误处理
- `router/` — Vue Router 路由配置，包含权限守卫

组件自动导入通过 `unplugin-vue-components` 和 `unplugin-auto-import` 配置实现。

## 业务逻辑

### 工作量计算模型
核心迁移工作量 = Σ（模块基础天数 × 权重系数 × 数据量系数 × 用户数系数），其中系数通过阶梯范围匹配自动计算。

项目生命周期：草稿 → 进行中 → 已完成 → 已归档

### 五大核心模块
1. 系统管理 — 系统类型、模块库、阶梯配置、报表配置、用户管理
2. 项目管理 — 创建/列表项目、状态跟踪
3. 工作量评估 — 四步评估向导
4. 统计可视化 — ECharts 图表仪表盘
5. 报表导出 — Excel/PDF/Word 生成

## 关键约定

- **软删除**：所有实体使用 `deleted` 字段进行逻辑删除，由 MyBatis-Plus 管理
- **JWT 认证**：基于 Token 的身份认证，可配置过期时间（默认 2 小时）。`USER_DISABLED` 业务码 10005 走 `BusinessException` → HTTP 200 + body.code
- **Git 工作流**：功能分支合并到 `develop`，然后 PR 到 `main`
- **提交风格**：使用中文提交信息，描述所做的工作（如 `fix(e2e): 评估步骤3输入可见性等待`、`feat(rbac): UserController 加 ADMIN 权限拦截`）
- **当前分支**：`develop`（main 为发布分支）
- **开发阶段**：当前处于第八阶段（增量需求任务）

## 文档资源

`docs/` 目录下包含详细的项目文档：
- `architecture/需求说明文档.md` — 功能需求说明
- `architecture/产品设计文档.md` — 产品设计文档
- `architecture/技术架构文档.md` — 技术架构设计
- `architecture/数据库设计文档.md` — 数据库设计
- `develop/plan.md` — 八阶段开发计划（含增量需求）
- `develop/测试方案及测试计划（合集）.md` — 测试方案与计划
- `implementation/API接口文档.md` — API 接口文档
- `test-reports/` — E2E 测试报告与 Bug 追踪清单（按日期归档）
- `init-db.sql` — 完整数据库建表脚本

## 知识库

Claude 维护的项目知识库分布在两个位置，遇到项目特定踩坑点时优先到这里查：

### 项目目录 memory/（提交到 git，团队共享）
- `MEMORY.md` — 索引
- `playwright_e2e_patterns.md` — Playwright E2E 踩坑集（17 条实战经验）
- `known_product_bugs_from_e2e.md` — E2E 暴露的产品 Bug 清单（已修复 9 个）
- `e2e_must_use_playwright_headed_with_screenshots.md` — E2E 硬约定（Playwright + 有头模式 + 截图）
- `env_setup_facts.md` — 本机 E2E 环境要点（服务启动顺序、pnpm 缺失绕过）
- `business_core.md` — 业务核心模型（工作量计算公式、五大模块）
- `key_files.md` — 后端前端核心文件路径速查
- `design_doc_vs_e2e.md` — E2E 失败时按设计文档判定改测试还是改产品

### Claude 自动记忆（会话级行为反馈，不提交 git）
- `workflow-new-feature-full-cycle.md` — 12 步开发流程 + 5 阶段测试/Bug 修复闭环
- `workflow-bug-tracking-template.md` — Bug 追踪文档模板与编号规范
- `feedback-restart-backend-after-rbac.md` — 后端注解类改动后自行重启
- `feedback-pathvariable-needs-explicit-name.md` — @PathVariable 必须显式 name（已两次踩坑）

## API 客户端（前端）

`migrametric-web/src/utils/request.ts` 是所有 API 模块使用的 Axios 实例，负责：
- 从 Pinia store 注入 JWT token
- 全局错误处理（业务码 → ElMessage 浮动 toast + 拒绝）
- 响应数据解包（`Result<T>` 包装）

`api/` 目录下的 API 模块命名与后端控制器包名对应。
