---
name: 关键文件路径
description: 后端和前端核心文件的位置
type: reference
---

**后端核心文件**:
- 主配置: `migrametric-server/src/main/resources/application.yml`
- 工作量计算: `migrametric-server/src/main/java/com/migrametric/service/evaluation/impl/WorkloadCalculationServiceImpl.java`
- 阶梯匹配: `migrametric-server/src/main/java/com/migrametric/service/ladder/impl/LadderMatchingServiceImpl.java`
- 统一响应: `migrametric-server/src/main/java/com/migrametric/common/Result.java`
- 全局异常处理: `migrametric-server/src/main/java/com/migrametric/common/GlobalExceptionHandler.java`
- Excel导出: `migrametric-server/src/main/java/com/migrametric/service/export/impl/ExcelExportServiceImpl.java`
- 评估实体: `migrametric-server/src/main/java/com/migrametric/entity/evaluation/Evaluation.java`
- JWT认证过滤: `migrametric-server/src/main/java/com/migrametric/filter/JwtAuthenticationFilter.java`

**前端核心文件**:
- 路由配置: `migrametric-web/src/router/index.ts`
- 权限守卫: `migrametric-web/src/router/permission.ts`
- 状态管理: `migrametric-web/src/stores/user.ts`
- 请求封装: `migrametric-web/src/utils/request.ts` (JWT拦截器、缓存、错误处理)
- 评估向导: `migrametric-web/src/views/project/evaluate/index.vue` (四步评估流程)
- 主布局: `migrametric-web/src/layouts/index.vue`
- 异步组合函数: `migrametric-web/src/composables/useAsync.ts`
- Vite配置: `migrametric-web/vite.config.ts`

**文档路径**:
- 开发计划: `docs/develop/plan.md`
- 数据库设计: `docs/architecture/数据库设计文档.md` (61KB, 17张表)
- 技术架构: `docs/architecture/技术架构文档.md` (91KB, 详细开发规范)
- API接口文档: `docs/implementation/API接口文档.md`
- Claude指南: `CLAUDE.md`

**数据库脚本**:
- 完整建表脚本: `docs/init-db.sql` (44KB)

**Why**: 快速定位核心代码位置，提高开发效率
**How to apply**: 需要修改或查看核心功能实现时直接定位文件