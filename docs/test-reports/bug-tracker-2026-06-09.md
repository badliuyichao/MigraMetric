# Bug 追踪清单

- **日期**：2026-06-09
- **测试范围**：首页仪表盘 + 项目统计页 + 分页能力
- **总用例数**：35
- **失败数**：0
- **Bug 数**：2（均已 CLOSED）
- **全量回归**：34 passed / 0 failed / 1 skipped（4.5 分钟）

---

## BUG-20260609-01：首页 4 个 widget 统计数据全为 0

- **状态**：CLOSED
- **优先级**：P1（功能）
- **发现版本**：用户手动登录发现
- **关联用例**：手动测试 - 首页仪表盘
- **错误现象**：登录后首页 4 个 widget（项目总数、评估次数、总工作量、用户数）数据均显示为 0
- **根因分析**：`dashboard/index.vue` 的 `onMounted` 是空 TODO（`// TODO: 从API获取数据`），从未调 API。后端也没有全局概览接口。
- **修复方案**：新建 `GET /api/statistics/overview` 接口（StatisticsController + StatisticsServiceImpl + DashboardOverview VO）+ 前端 `getDashboardOverview()` 调用
- **修复 commit**：TBD
- **回归结果**：API 返回 projectCount=163, evaluationCount=112, totalWorkload=3686.68, userCount=17

---

## BUG-20260609-02：项目统计页 GET /api/statistics/{projectId} 返回 500

- **状态**：CLOSED
- **优先级**：P1（功能）
- **发现版本**：全量 E2E 回归暴露（已有 bug）
- **关联用例**：STA-001, STA-002, STA-003, STA-004
- **错误现象**：`GET /api/statistics/{projectId}` 返回 HTTP 500 + "系统繁忙"
- **根因分析**：`StatisticsController.getStatistics()` 的 `@PathVariable Long projectId` 缺少显式 name 属性，触发 `IllegalArgumentException`（CLAUDE.md 已记录 `<parameters>true</parameters>` 在本环境不稳定）。附带修复：`getStatistics()` 对无评估记录的项目返回空默认结果（原代码抛 BusinessException）。
- **修复方案**：
  1. `@PathVariable` → `@PathVariable(name = "projectId")`
  2. `getEvaluationByProjectId()` 返回 null 而非抛异常
  3. `getStatistics()` 对 null evaluation 返回空默认 StatisticsResultVO
- **修复 commit**：TBD
- **回归结果**：无评估项目返回 200 + 空数据；有评估项目正常返回。全量 E2E 34/34 通过。

---

## 状态汇总

| Bug ID | 标题 | 优先级 | 状态 | 根因 |
|--------|------|--------|------|------|
| BUG-20260609-01 | 首页 widget 数据全为 0 | P1 | CLOSED | onMounted 空 TODO + 无概览 API |
| BUG-20260609-02 | 项目统计页返回 500 | P1 | CLOSED | @PathVariable 缺显式 name |

---

## 阶段记录

- [x] Phase 1：全场景执行完成
- [x] Phase 2：所有 Bug 登记完成（2 个）
- [x] Phase 3：所有 Bug 逐个修复 + 逐个回归通过
- [x] Phase 4：全量回归 100% 通过（34/34 passed, 0 failed）
- [ ] Phase 5：归档（附加到测试报告 + 更新 plan.md）
