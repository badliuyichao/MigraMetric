# MigraMetric 项目记忆索引

- [项目概览](project_overview.md) — 技术栈、架构、开发状态全貌
- [业务核心模型](business_core.md) — 工作量计算公式和五大模块
- [关键文件路径](key_files.md) — 后端前端核心文件快速定位
- [开发规范](coding_standards.md) — 编码约定、Git工作流、测试要求
- [开发命令速查](dev_commands.md) — 常用开发和构建命令
- [登录功能验证](login_verification.md) — 前后端登录验证结果（2026-04-03）
- [API路径约定](api_paths.md) — 后端API命名规范和正确用法
- [Playwright E2E 模式](playwright_e2e_patterns.md) — 跑E2E踩坑：Session过期/登出弹窗/URL glob/Vite冷启动/step panel v-show/@PathVariable name/UNIQUE 长度/el-table checked
- [E2E 硬约定](e2e_must_use_playwright_headed_with_screenshots.md) — 必须 Playwright + headed + 每步截图，2026-06-08 用户定的硬规则
- [设计文档vs E2E判定](design_doc_vs_e2e.md) — 失败时按设计文档判定改测试还是改产品
- [本机E2E环境要点](env_setup_facts.md) — MySQL/后端/前端起服务顺序、缺pnpm绕过、产物路径
- [E2E暴露的产品Bug清单](known_product_bugs_from_e2e.md) — 已修复 9 个（2026-06-08 修 3 个、2026-06-09 修 6 个），仍剩 1 个未修（CFG-006 testuser 缺失）