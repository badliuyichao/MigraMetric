# Bug 追踪清单

- **日期**：YYYY-MM-DD
- **测试范围**：{本次测试覆盖的 spec / 模块}
- **测试命令**：`node node_modules/@playwright/test/cli.js test --project=e2e --headed --workers=1`
- **总用例数**：{N}
- **失败数**：{N}
- **Bug 数**：{N}

---

## BUG-{YYYYMMDD}-{NN}：{简短标题}

- **状态**：OPEN
- **优先级**：P0/P1/P2（P0=阻塞、P1=安全/功能、P2=UI/体验）
- **发现版本**：{commit hash 或功能描述}
- **关联用例**：{spec文件}:{用例名}
- **错误现象**：{错误信息}
- **截图**：`test-results/{dir}/{file}.png`
- **根因分析**：TBD
- **修复方案**：TBD
- **修复 commit**：TBD
- **回归结果**：TBD

---

<!-- 按编号递增追加，格式同上 -->

## 状态汇总

| Bug ID | 标题 | 优先级 | 状态 | 修复 commit |
|--------|------|--------|------|-------------|
| BUG-{YYYYMMDD}-01 | ... | P0 | OPEN | - |

---

## 阶段记录

- [ ] Phase 1：全场景执行完成
- [ ] Phase 2：所有 Bug 登记完成
- [ ] Phase 3：所有 Bug 逐个修复 + 逐个回归通过
- [ ] Phase 4：全量回归 100% 通过
- [ ] Phase 5：归档（附加到测试报告 + 更新 plan.md）
