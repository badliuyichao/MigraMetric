---
name: 项目概览
description: MigraMetric 项目核心信息和技术栈
type: project
---

MigraMetric 是企业异构系统迁移工作量评估平台，采用 Spring Boot 3.2 + Vue 3.4 全栈架构。

**技术栈**:
- 后端: Java 21, Spring Boot 3.2, MyBatis-Plus 3.5.5, JWT (jjwt 0.12.3), SpringDoc OpenAPI 2.3.0
- 前端: Vue 3.4.15, TypeScript 5.3.3, Vite 5.0.11, Element Plus 2.5.3, ECharts 5.5.0, Pinia 2.1.7, Axios 1.6.5
- 数据库: MySQL 8.0
- 测试: Vitest 1.2.1, Spring Boot Test + H2

**核心架构**:
- 后端: Controller → Service → Mapper 三层架构，统一 Result<T> 响应封装，JWT 认证(2小时过期)
- 前端: 组合式 API，Pinia 状态管理，Axios 请求封装(JWT拦截器+缓存)，自动导入+组件自动注册
- 数据库: 17张表，四大领域(配置域/项目域/评估域/用户域)，逻辑删除(deleted字段)

**开发状态**: 已完成全部七个开发阶段，处于优化完善状态。Git分支: develop(开发分支)，main(发布分支)

**Why**: 提供项目全貌，便于快速理解技术栈和架构决策
**How to apply**: 在讨论技术实现、排查问题、添加新功能时参考此架构