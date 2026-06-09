# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 提供代码库工作指南。

## 项目概述

MigraMetric 是一个全栈 Web 应用，用于评估企业异构系统迁移工作量。通过分析系统架构、功能模块和技术栈，帮助企业估算迁移成本和时间周期。

## 开发rules

- Read ALL files including test file first. The test defines what passes.
- Write the COMPLETE solution in a single file write. Not incrementally.
- Run tests once. If pass: stop immediately. If fail: read error, fix once, retest.
- Never iterate more than once on the same failure. Rethink if stuck.
- Never refactor, improve, or polish passing code.
- For WebSocket: use a Set to track clients manually. Send to sender first, then broadcast to others via setTimeout(0). Never use pub/sub channels.
- For E2E: must use Playwright, must run headed (browser visible) locally, and must screenshot every key step to `test-results/<specName>/<NN>-<name>.png`. See `migrametric-web/tests/README.md` §E2E 测试约定 for the full checklist.
- **后端改了 RBAC / 新 Controller / SecurityConfig 后必须重启后端**（Spring Boot DevTools 在 maven fork 模式不自动重载新加的注解类），kill 当前 8080 java 进程 + `mvn spring-boot:run` 重启，**不询问用户**。
- **后端 Controller `@PathVariable` / `@RequestParam` 必须显式指定 name**（`@PathVariable("userId") Long userId`），pom 配的 `<parameters>true</parameters>` 在本环境不稳定，没显式 name 会导致 `IllegalArgumentException: Name for argument ... not specified` 500。
- **E2E 测试用户名/密码等 fixture 必须满足前端 `validateUsername` 4-20 字母数字下划线规则**。`UNIQUE() = \`e2euser${Date.now()}\`` 会生成 21+ 字符超长，触发表单 rule 失败而非后端业务码。建议 `UNIQUE() = \`e2eu${(Date.now() % 10000000).toString().padStart(7, '0')}\``（11 字符）。
- **评估向导 step panel 优先用 `v-if` 而非 `v-show`**：`v-show` 隐藏的元素在 DOM 里，Playwright 拿到的 input 是 hidden → 命中 first() 后 `not visible` 永真，导致 step 切换后的断言失败。

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

## 架构设计

### 后端 (Spring Boot 3.2)
标准三层架构：`controller/` -> `service/` -> `mapper/`

- `entity/` — 数据库实体类 (JPA 风格，使用 MyBatis-Plus)
- `dto/` — 请求 DTO，用于 API 输入
- `vo/` — 响应视图对象 (View Object)
- `common/` — Result 统一响应包装、ResultCode 枚举、全局异常处理器
  - `GlobalExceptionHandler` 统一映射：`BusinessException` → HTTP 200 + 业务码、`@Valid` 失败 → 400、`AccessDeniedException`（@PreAuthorize 拒绝）→ 403、`AuthenticationException` → 401、其他 → 500
- `config/` — CORS、Swagger/OpenAPI、JWT、Security（`@EnableMethodSecurity` 启用方法级安全）

**RBAC 规范**：admin 专用 API（如 `UserController`）在类级别加 `@PreAuthorize("hasRole('ADMIN')")`。`JwtAuthenticationFilter` 注入的 authority 是 `ROLE_<role>` 形式。

统一响应格式通过 `Result<T>` 类实现。Swagger UI 地址：`/swagger-ui.html`。

### 前端 (Vue 3 + TypeScript + Vite)
- `views/` — 路由级页面组件
- `components/` — 可复用 UI 组件
- `composables/` — Vue Composition API 共享逻辑
- `stores/` — Pinia 状态管理
- `api/` — Axios API 模块 (每个后端控制器对应一个文件)
- `utils/request.ts` — Axios 封装，包含 JWT 拦截器和错误处理
- `router/` — Vue Router 路由配置，包含权限守卫

组件自动导入通过 `unplugin-vue-components` 和 `unplugin-auto-import` 配置实现。

## 业务逻辑

### 工作量计算模型
核心迁移工作量 = Σ (模块基础天数 × 权重系数 × 数据量系数 × 用户数系数)，其中系数通过阶梯范围匹配自动计算。

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
- **提交风格**：使用中文提交信息，描述所做的工作（如 `fix(e2e): 评估 step3 input 可见性等待`、`feat(rbac): UserController 加 ADMIN 权限拦截`）
- **当前分支**：`develop`（main 为发布分支）
- **开发阶段**：当前处于第三阶段（项目管理模块）

## 文档资源

`docs/` 目录下包含详细的项目文档：
- `architecture/需求说明文档.md` — 功能需求说明
- `architecture/产品设计文档.md` — 产品设计文档
- `architecture/技术架构文档.md` — 技术架构设计 (91KB)
- `architecture/数据库设计文档.md` — 数据库设计 (61KB)
- `develop/plan.md` — 七阶段开发计划
- `test-reports/` — E2E 测试报告（按日期归档）
- `init-db.sql` — 完整数据库建表脚本 (44KB)

## 知识库（memory/）

`memory/` 是给 Claude 维护的项目知识库，遇到项目特定踩坑点时优先到这里查：
- `MEMORY.md` — 索引
- `playwright_e2e_patterns.md` — Playwright E2E 模式与 13 个踩坑点
- `known_product_bugs_from_e2e.md` — E2E 暴露的产品 bug 清单
- `e2e_must_use_playwright_headed_with_screenshots.md` — E2E 硬约定
- `coding_standards.md` — 编码规范
- `env_setup_facts.md` — 本机环境要点

## API 客户端（前端）

`migrametric-web/src/utils/request.ts` 是所有 API 模块使用的 Axios 实例，负责：
- 从 Pinia store 注入 JWT token
- 全局错误处理（业务码 → ElMessage 浮动 toast + reject）
- 响应数据解包（`Result<T>` 包装）

`api/` 目录下的 API 模块命名与后端控制器包名对应。
