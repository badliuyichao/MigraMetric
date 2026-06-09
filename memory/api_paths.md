---
name: API路径约定
description: 后端API路径命名规范和正确用法
type: feedback
---

**API路径命名规范**:

**正确示例**:
- 系统类型列表: `GET /api/system/types` (不是 `/api/system-types/page`)
- 系统类型详情: `GET /api/system/types/{id}`
- 启用的系统类型: `GET /api/system/types/enabled`
- 登录: `POST /api/auth/login`
- 用户信息: `GET /api/auth/info`

**路径结构**:
```
/api/{module}/{resource}
/api/{module}/{resource}/{id}
/api/{module}/{resource}/{action}
```

**常见错误**:
- ❌ `/api/system-types/page` (路径不存在，应为 `/api/system/types`)
- ❌ `/api/auth/login/` (末尾不要加斜杠)
- ❌ `/api/system/types/page?page=1` (路径冲突，`page`被误认为Long类型ID)

**查询参数传递**:
- 分页查询: `GET /api/system/types?page=1&size=10`
- 条件筛选: `GET /api/system/types?name=SAP&category=1&status=1`

**Controller路径映射**:
- SystemTypeController: `@RequestMapping("/api/system/types")`
- AuthController: `@RequestMapping("/api/auth")`
- UserController: `@RequestMapping("/api/users")`

**Why**: 避免API路径错误导致的404或500错误
**How to apply**: 调用API前先查看Controller的@RequestMapping注解，确认正确路径