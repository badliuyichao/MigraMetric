# Bug 追踪清单

- **日期**：2026-06-11
- **测试范围**：E2E 全量回归 + 新增用例
- **总用例数**：67（E2E）
- **失败数**：0
- **Bug 数**：1（待修复）

---

## BUG-20260611-02：后端未校验源系统=目标系统（需求§5.1.1）

- **状态**：已关闭（非 Bug — 测试期望值与产品 HTTP 语义不一致）
- **优先级**：🟢 P3（测试修正）
- **发现版本**：E2E PRJ-006 用例发现
- **关联用例**：PRJ-006
- **关联文档**：
  - 需求说明文档 §5.1 系统规则第 1 条："源系统和目标系统不能相同"
  - 需求说明文档 §3.2.1 业务规则："源系统和目标系统不能相同"
  - 产品设计文档 §3.2.2 验证规则："目标系统：必填，不能与源系统相同"

### 错误现象
- **产品需求**（需求 §5.1.1）：源系统和目标系统不能相同
- **实际表现**：选择相同系统时，后端返回 HTTP 200 + 业务码 50005（PARAM_INVALID）
- **影响**：无 — 后端校验已存在，业务逻辑正确

### 根因分析
- `ProjectServiceImpl.createProject` **已有** `sourceSystemId == targetSystemId` 校验（line 67-69）
- `GlobalExceptionHandler.handleBusinessException` 使用 `@ResponseStatus(HttpStatus.OK)`，所有 `BusinessException` 返回 HTTP 200
- E2E 测试 PRJ-006 期望 HTTP >= 400，与产品"业务码"语义不一致
- **与 BUG-20260610-02 USER-003 相同模式**：产品用 HTTP 200 + body.code 区分错误类型

### 修复方案
- 修 E2E 测试：断言 response body `code === 50005` 而非 HTTP status

### 修复 commit
- `fix(e2e): PRJ-006 断言改为 body.code 验证源=目标校验`

### 回归结果
- E2E：67/0/0（6.9 分钟）✅
