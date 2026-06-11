# Bug 追踪清单

- **日期**：2026-06-10
- **测试范围**：项目状态流转 + 数据库一致性回归
- **总用例数**：50（E2E 全量） + 13（新增/修复单测）
- **失败数**：0
- **Bug 数**：1（进行中）

---

## BUG-20260610-02：completeEvaluation 不更新 project.status（项目状态永远卡在 DRAFT）

- **状态**：进行中
- **优先级**：🔴 P0（功能失效 — 归档按钮永远点不到）
- **发现版本**：手动核查 production code（基于 2026-06-10 E2E 回归附带的状态机审计）
- **关联用例**：E2E-FLOW-001 后置断言（待补）
- **关联文档**：
  - 需求说明文档 §3.2.3 项目状态管理
  - 产品设计文档 §3.3.1 步骤四
  - 技术架构文档 §1.2 数据模型

### 错误现象
- **产品需求**（需求 §3.2.3）：
  - 进入评估页面时，**项目**状态自动变为"进行中"
  - 完成评估并生成报告后，状态变为"已完成"
  - 项目归档后，状态变为"已归档"
- **实际表现**：
  - 项目创建后 `project.status = DRAFT`（OK）
  - 进入评估 → `evaluation.evaluationStatus = IN_PROGRESS`，但 **`project.status` 永远不变**
  - 完成评估 → `evaluation.evaluationStatus = COMPLETED`，但 **`project.status` 永远不变**
  - 归档接口依赖 `project.status === 'COMPLETED'` 校验 → **用户永远无法归档项目**
- **影响**：
  - 4 个状态只用了 1 个（DRAFT），IN_PROGRESS/COMPLETED 流转完全失效
  - 归档按钮（`v-if="projectInfo.status === 'COMPLETED'"`）在 UI 上永远隐藏
  - 项目列表筛选"已完成"永远空

### 根因分析
- `EvaluationServiceImpl.completeEvaluation` 仅更新 `evaluation.evaluationStatus`，**未联动更新 `project.status`**
- `EvaluationServiceImpl.saveIndicators` 同样仅更新 `evaluation.evaluationStatus`
- 仓储设计是"两套状态字段并存"（`project.status` + `evaluation.evaluationStatus`），流转逻辑只写一套

### 修复方案（P0）
- 修 `EvaluationServiceImpl.completeEvaluation`：在 evaluation 转 COMPLETED 后，**同步更新 project.status = COMPLETED**
- （后续 P1）修 `saveIndicators`：首次创建 evaluation 时同步更新 project.status = IN_PROGRESS
- （后续 P2）dev 库清理 12 个孤儿 DRAFT 项目

### 修复 commit
1. `8ba5e92` fix(evaluation): completeEvaluation 同步更新 project.status
2. （待 commit）feat(project): 项目状态机重构 + 枚举化 + 详情页分支补全

### 回归结果
- 单测：✅ ProjectStateMachineTest 8/8 通过
- E2E：✅ 49/1/0（13.1 分钟）
  - E2E-FLOW-001 增强断言 `project.status === 'COMPLETED'` 持续生效
