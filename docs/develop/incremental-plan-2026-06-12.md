# 增量开发计划 — 前端功能补全

**日期**：2026-06-12
**范围**：5 个前端未完成功能
**原则**：先检查文档 → 开发 → 测试 → 回归

---

## 一、待开发功能清单

| 序号 | 功能 | 后端状态 | 前端状态 | 优先级 | 估时 |
|------|------|---------|---------|--------|------|
| 1 | PDF/Word 导出前端对接 | ✅ API 可用 | ❌ 显示"开发中" | P0 | 1h |
| 2 | 详情页导出按钮对接 ExportDialog | ✅ | ❌ 显示"开发中" | P0 | 0.5h |
| 3 | 项目列表日期范围筛选 | ✅ | ❌ UI 未实现 | P1 | 1h |
| 4 | 个人资料编辑 | — | ❌ 显示"开发中" | P2 | 1h |
| 5 | 修改密码 | — | ❌ 显示"开发中" | P2 | 1h |

---

## 二、开发步骤

### Step 1: PDF/Word 导出前端对接

**需求文档**：§3.5.2 导出格式、§3.5.3 导出功能
**当前状态**：ExportDialog.vue 中 PDF/Word 分支显示 `ElMessage.info('开发中')`
**改动点**：`src/components/export/ExportDialog.vue` line 128-131
**方案**：移除"开发中"提示，调用 `exportToPdf()` / `exportToWord()` API

### Step 2: 详情页导出按钮对接

**需求文档**：§3.2.3 项目详情 — 导出报告按钮
**当前状态**：`detail/index.vue` 的 `handleExport()` 显示"开发中"
**改动点**：`src/views/project/detail/index.vue` line 442-443
**方案**：改为打开 ExportDialog 组件

### Step 3: 项目列表日期范围筛选

**需求文档**：§3.2.2 查询筛选功能 — "按评估日期范围筛选"
**当前状态**：前端只有 projectName、customerName、status 筛选
**改动点**：`src/views/project/list/index.vue` 搜索表单
**方案**：添加 el-date-picker 日期范围选择器，传参 evaluationDateFrom/evaluationDateTo

### Step 4: 个人资料编辑

**当前状态**：`layouts/index.vue` 的 profile 选项显示"功能开发中"
**改动点**：`src/views/user/index.vue`（已有页面）+ 路由
**方案**：复用已有的用户编辑功能，添加个人资料入口

### Step 5: 修改密码

**当前状态**：`layouts/index.vue` 的 password 选项显示"功能开发中"
**改动点**：需新增密码修改 API + 前端弹窗
**方案**：后端添加 `/api/auth/password` 接口，前端添加密码修改弹窗

---

## 三、验证标准

每个功能完成后：
1. 手动验证 UI 交互正常
2. 补充 E2E 用例（如适用）
3. 运行全量 E2E 回归
4. 更新文档
