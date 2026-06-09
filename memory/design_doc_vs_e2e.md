---
name: design-doc-vs-e2e
description: How to judge an E2E failure against design docs in MigraMetric — what to fix in test, what to fix in product, what to delete
metadata:
  type: project
---

E2E 失败 → 对照设计文档判定的标准工作流（2026-06-08 第一次完整跑后总结）。

**Why:** 第一次跑 `tests/e2e/auth/login.spec.ts` 6 败 4 过；不靠文档判定就分不清"产品没做"和"测试写错"。

**How to apply:** 任何 E2E 失败先按这 3 步走，再决定改什么。

---

### 设计文档权威章节速查

| 主题 | 文档 | 章节 |
|---|---|---|
| 登录认证 | 产品设计文档.md | §9.1.1 |
| 会话管理 / Token | 产品设计文档.md | §9.1.2 |
| 安全规范 / JWT | 技术架构文档.md | §8.1.1 |
| 审计日志 | 产品设计文档.md | §9.3.1 |
| 用户角色 | 需求说明文档.md | §1.3 |
| 安全性需求 | 需求说明文档.md | §4.3 |
| API 路径 | 技术架构文档.md | §8 / API 表 |

### 判定流程

```
E2E 失败
  ↓
① 设计文档有硬性要求吗？
   ├─ 是 → 产品没实现（修复产品 + 保留测试）
   └─ 否 ↓
② 测试断言的功能在前端代码里吗？
   ├─ 在 → 测试 bug（修测试代码，如 URL glob、确认弹窗）
   └─ 不在 → 测试越界（删测试 OR 补需求 → 再补实现）
```

### 第一阶段实战结果

- **E2E-001 完整登录**：路由 `/` → `/dashboard` 重定向，glob 改 `**/dashboard` 即过
- **E2E-003 Session 过期**：原测注入 expired-token，后端返 403 而非 401，前端不清 token。改成"清空 token"才能触发路由守卫
- **E2E-004 登出**：缺 `user-menu` / `logout-button` 两个 testid（`layouts/index.vue` 已实现登出入口）+ 登出有确认弹窗。最小补丁：在 el-dropdown 和登出 dropdown-item 上加 testid
- **E2E-005/009/010**：产品设计文档 §9.1.1 未要求"记住密码/忘记密码/重置"，**删除**

### 经验

- **不要因为"前人写过"就保留用例**。E2E 数量不是 KPI，覆盖设计文档要求才是
- 改动产品代码要克制：CLAUDE.md "Never refactor, improve, or polish passing code"，补 testid 也只补到必要为止
- 一旦发现测试与文档脱节，**整套审计一次**，不要逐条修（会反复触发同一类问题）
