# Bug 追踪清单

- **日期**：2026-06-09
- **测试范围**：评估工作量计算（客开场景）
- **总用例数**：43
- **失败数**：0
- **Bug 数**：4（均已 CLOSED）
- **全量回归**：42 passed / 0 failed / 1 skipped（5.7 分钟）

---

## BUG-20260609-04：客开工作量计算为 0 + 后端报错

- **状态**：CLOSED
- **优先级**：P1（功能）
- **发现版本**：用户手动测试"江西国泰"项目发现
- **关联用例**：手动测试 - 评估步骤三（客开=是）
- **错误现象**：选择客开=有，填入客开模块数和工作量，点击计算 → 后端报错 + 计算结果客开工作量为 0 人天
- **根因分析**：`handleCalculate()` 调用 `saveCurrentStepData()` 后未检查返回值。保存失败时（异常被 catch 吞掉返回 false），计算继续执行 → 读到数据库中的旧数据（客开字段为 null/0）→ 客开工作量算出 0
- **修复方案**：`handleCalculate()` 检查 `saveCurrentStepData()` 返回值，false 时中止计算
- **修复 commit**：TBD
- **回归结果**：EV-004（客开场景）通过，全量 42/42 无回归

## BUG-20260609-03：模块库管理页面多个异常

- **状态**：CLOSED
- **优先级**：P1（功能）
- **发现版本**：用户手动登录发现
- **关联用例**：手动测试 - 模块库管理页面
- **错误现象**：模块分类获取异常、查询区下拉框数据异常、所属系统宽度异常、模块分类无数据、状态下拉框宽度异常
- **根因分析**：
  1. `ModuleController` 的 `/enabled` 和 `/categories` 端点的 `@RequestParam` 缺少显式 `name` 属性，触发 `IllegalArgumentException` → HTTP 500（**第三次踩此坑**）
  2. 所属系统列宽 `width="120"` 对系统名称不够
  3. 搜索表单下拉框无固定宽度，inline 模式下显示异常
- **修复方案**：
  1. `@RequestParam(required = false)` → `@RequestParam(name = "systemId", required = false)`（2 处）
  2. 所属系统列宽 120 → 160
  3. 搜索下拉框加 `style="width: 160px/140px/110px"`
- **修复 commit**：TBD
- **回归结果**：42/42 全过，无回归

---

## 状态汇总

| Bug ID | 标题 | 优先级 | 状态 | 根因 |
|--------|------|--------|------|------|
| BUG-20260609-01 | 首页 widget 数据全为 0 | P1 | CLOSED | onMounted 空 TODO |
| BUG-20260609-02 | 项目统计页返回 500 | P1 | CLOSED | @PathVariable 缺显式 name |
| BUG-20260609-03 | 模块库管理页面多个异常 | P1 | CLOSED | @RequestParam 缺显式 name + 前端宽度 |
| BUG-20260609-04 | 客开工作量计算为 0 | P1 | CLOSED | handleCalculate 未检查保存结果 |

---

## 阶段记录

- [x] Phase 1：全场景执行完成
- [x] Phase 2：所有 Bug 登记完成
- [x] Phase 3：所有 Bug 逐个修复 + 逐个回归通过
- [x] Phase 4：全量回归 100% 通过（42/42 passed, 0 failed）
- [x] Phase 5：归档
