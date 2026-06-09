---
name: env-setup-facts
description: Concrete env quirks for running E2E in MigraMetric on this Windows machine — service start order, missing pnpm, port checks
metadata:
  type: project
---

2026-06-08 第一次完整跑 E2E 时记下的本机环境要点。

**Why:** 跑 E2E 前需要确认 4 件事（MySQL、后端、前端、Playwright 浏览器），踩了 2 个坑，避免下次重蹈。

**How to apply:** 起 E2E 环境时按此清单。

---

### 必备依赖

| 组件 | 检查命令 | 当前状态（2026-06-08） |
|---|---|---|
| MySQL 8.4 | `sc query MySQL84` | RUNNING ✅ |
| Spring Boot 后端 :8080 | `curl localhost:8080/api/auth/login` | 需 `cd migrametric-server && mvn spring-boot:run` |
| Vite 前端 :3000 | `curl localhost:3000/` | 需 `node node_modules/vite/bin/vite.js` |
| Playwright 浏览器 | `ls /c/Users/EASON/AppData/Local/ms-playwright/` | 已有 chromium-1223 ✅ |

### 坑 1：`pnpm` 不在 PATH

```
which pnpm → command not found
```

环境里只有 `npm`。但项目 `package.json` 写 `pnpm@>=8.0.0`，`pnpm-lock.yaml` 也在。

**绕过**：直接用 `node node_modules/vite/bin/vite.js` 启动 vite。`node_modules/` 已存在（之前 `npm install` 装过），不用重装。

### 坑 2：mvn 改 cwd

Bash 调 `mvn spring-boot:run` 后，后续命令 cwd 留在 `migrametric-server/`。下次 `cd migrametric-web` 必须显式 `cd /d/03-CODE/MigraMetric/migrametric-web`。

### 起服务顺序（实测可用）

```bash
# 1. MySQL（已起可跳过）
sc query MySQL84

# 2. 后端（后台）
cd /d/03-CODE/MigraMetric/migrametric-server && mvn spring-boot:run &

# 3. 前端（后台，等 vite 启动后再跑测试）
cd /d/03-CODE/MigraMetric/migrametric-web && node node_modules/vite/bin/vite.js &

# 4. 跑测试
cd /d/03-CODE/MigraMetric/migrametric-web && \
  node node_modules/@playwright/test/cli.js test --project=e2e --headed --workers=1
```

### 调试产物路径

- 失败截图：`migrametric-web/test-results/<test-name>/test-failed-1.png`
- 失败 trace：`migrametric-web/test-results/<test-name>/trace.zip`
- 查看 trace：`cd migrametric-web && npx playwright show-trace test-results/.../trace.zip`
- `.gitignore` 已忽略 `test-results/` 和 `playwright-report/`

### 数据准备

- 登录用户：`admin/admin123`（ADMIN），`testuser/test123`（USER），`disabled/disabled123`（禁用）
- 数据源：见 `migrametric-server/src/main/resources/application.yml`（库 `migrametric_dev`，root/admin123）
- 初始化脚本：`docs/init-db.sql`（44KB 全量建表）
