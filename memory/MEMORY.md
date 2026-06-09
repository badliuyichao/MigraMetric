# MigraMetric 项目记忆索引

## 开发规范与流程
- [业务核心模型](business_core.md) — 工作量计算公式、计算流程、五大模块
- [关键文件路径](key_files.md) — 后端前端核心文件快速定位
- [设计文档vs E2E判定](design_doc_vs_e2e.md) — E2E 失败时按设计文档判定改测试还是改产品

## E2E 测试
- [Playwright E2E 踩坑集](playwright_e2e_patterns.md) — 17 条实战踩坑：Session/登出/Vite冷启动/v-show/@PathVariable name/UNIQUE 长度/checked 等
- [E2E 硬约定](e2e_must_use_playwright_headed_with_screenshots.md) — 必须 Playwright + headed + 每步截图
- [E2E 暴露的产品 Bug 清单](known_product_bugs_from_e2e.md) — 已修复 9 个，仍剩 CFG-006 testuser 缺失
- [本机 E2E 环境要点](env_setup_facts.md) — MySQL/后端/前端起服务顺序、pnpm 缺失绕过、产物路径
