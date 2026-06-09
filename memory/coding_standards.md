---
name: 开发规范
description: 编码约定、Git工作流、测试要求
type: feedback
---

**编码规范**:
- 后端统一响应格式: 所有API返回 `Result<T>` 包装类
- 软删除机制: 所有实体使用 `deleted` 字段(0=未删除, 1=已删除)，由 MyBatis-Plus 自动管理
- 服务层接口分离: 每个Service都有接口+实现类 (`XxxService` + `XxxServiceImpl`)
- 业务异常体系: `BusinessException` + `ResultCode` 枚举 + `GlobalExceptionHandler` 统一处理
- 前端TypeScript类型: 完善的类型定义，API模块与后端Controller对应

**Git工作流**:
- 分支策略: 功能分支合并到 `develop`，然后 PR 到 `main`
- 提交风格: 使用中文提交信息，描述所做的工作
- 当前分支: `develop` (开发分支)，`main` (发布分支)

**测试要求**:
- 后端: Spring Boot Test + H2内存数据库，单元测试+集成测试
- 前端: Vitest + @vue/test-utils + jsdom，单元测试覆盖API/组件/composables/utils
- 测试覆盖: 后端28个测试类，前端25个测试文件

**API文档**:
- Swagger UI: `/swagger-ui.html`
- 使用SpringDoc OpenAPI注解标注API接口

**前端特色实践**:
- 组合函数: `useAsync` 统一异步处理，`useResponsive` 响应式布局
- 请求封装: Axios实例 + JWT拦截器 + 请求缓存(5秒TTL) + 防抖工具
- 性能优化: 虚拟列表、懒加载、路由预加载
- 组件自动注册: `unplugin-vue-components` + ElementPlusResolver

**Why**: 保持代码一致性和可维护性，遵循团队约定
**How to apply**: 编写新功能时参考这些规范，确保代码风格统一