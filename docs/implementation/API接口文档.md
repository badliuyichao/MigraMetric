# API 接口文档

## 文档信息

| 项目名称 | 异构系统升迁工作量评估系统 (MigraMetric) |
|---------|----------------------------------------|
| 文档版本 | V1.0 |
| 创建日期 | 2026-03-25 |
| 文档作者 | 开发团队 |

---

## 一、接口概述

### 1.1 接口说明

本系统提供 RESTful API 接口，支持项目管理、评估管理、阶梯配置、系统管理等核心功能。所有接口采用 JSON 格式进行数据交换，并使用 JWT Token 进行身份认证。

### 1.2 基础信息

| 配置项 | 值 |
|-------|-----|
| 基础URL | `http://localhost:8080/api` |
| 数据格式 | application/json |
| 字符编码 | UTF-8 |
| 认证方式 | JWT Bearer Token |
| API文档 | `/swagger-ui.html` |

### 1.3 统一响应格式

所有接口响应统一使用以下格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2026-03-25T10:00:00"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200表示成功 |
| message | String | 响应消息 |
| data | Object | 响应数据 |
| timestamp | String | 时间戳 |

### 1.4 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "totalPages": 10
  },
  "timestamp": "2026-03-25T10:00:00"
}
```

---

## 二、认证接口

### 2.1 用户登录

**请求**

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**响应**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "admin",
    "roles": ["ADMIN"]
  }
}
```

### 2.2 获取当前用户信息

**请求**

```http
GET /api/auth/current
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "username": "admin",
    "nickname": "管理员",
    "email": "admin@example.com",
    "roles": ["ADMIN"]
  }
}
```

---

## 三、项目管理接口

### 3.1 创建项目

**请求**

```http
POST /api/projects
Authorization: Bearer <token>
Content-Type: application/json

{
  "projectName": "XX集团ERP迁移项目",
  "customerName": "XX集团",
  "sourceSystemId": 1,
  "targetSystemId": 2,
  "projectLeader": "张三",
  "contact": "13800138000",
  "description": "ERP系统从SAP迁移到用友NC"
}
```

**响应**

```json
{
  "code": 200,
  "message": "创建成功",
  "data": 1
}
```

### 3.2 分页查询项目

**请求**

```http
GET /api/projects?pageNum=1&pageSize=10&projectName=ERP&status=DRAFT
Authorization: Bearer <token>
```

**参数说明**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |
| projectName | String | 否 | 项目名称（模糊查询） |
| customerName | String | 否 | 客户名称（模糊查询） |
| status | String | 否 | 项目状态 |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "projectName": "XX集团ERP迁移项目",
        "customerName": "XX集团",
        "sourceSystemId": 1,
        "sourceSystemName": "SAP",
        "targetSystemId": 2,
        "targetSystemName": "用友NC",
        "projectLeader": "张三",
        "status": "DRAFT",
        "statusText": "草稿",
        "evaluationDate": "2026-03-25",
        "createTime": "2026-03-20 10:00:00"
      }
    ],
    "total": 50,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 3.3 获取项目详情

**请求**

```http
GET /api/projects/1
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "projectName": "XX集团ERP迁移项目",
    "customerName": "XX集团",
    "sourceSystemId": 1,
    "sourceSystemName": "SAP",
    "targetSystemId": 2,
    "targetSystemName": "用友NC",
    "projectLeader": "张三",
    "contact": "13800138000",
    "description": "ERP系统迁移",
    "status": "DRAFT",
    "statusText": "草稿",
    "evaluationDate": "2026-03-25",
    "createTime": "2026-03-20 10:00:00"
  }
}
```

### 3.4 更新项目

**请求**

```http
PUT /api/projects/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "projectName": "XX集团ERP迁移项目(修改版)",
  "projectLeader": "李四"
}
```

### 3.5 复制项目

**请求**

```http
POST /api/projects/1/copy
Authorization: Bearer <token>
```

### 3.6 删除项目

**请求**

```http
DELETE /api/projects/1
Authorization: Bearer <token>
```

### 3.7 归档项目

**请求**

```http
POST /api/projects/1/archive
Authorization: Bearer <token>
```

---

## 四、评估管理接口

### 4.1 创建评估记录

**请求**

```http
POST /api/evaluations
Authorization: Bearer <token>
Content-Type: application/json

{
  "projectId": 1
}
```

### 4.2 获取评估详情

**请求**

```http
GET /api/evaluations/1
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "projectId": 1,
    "tableCount": 100,
    "dataVolume": 500,
    "dataVolumeLadderId": 2,
    "dataVolumeLadderName": "中型",
    "dataVolumeWeight": 1.0,
    "userCount": 200,
    "userCountLadderId": 2,
    "userCountLadderName": "中规模",
    "userCountWeight": 1.2,
    "reportCount": 50,
    "hasCustomDev": true,
    "customDevCount": 10,
    "customDevWorkload": 50,
    "coreWorkload": 156.38,
    "reportWorkload": 25,
    "totalWorkload": 231.38
  }
}
```

### 4.3 保存评估指标

**请求**

```http
PUT /api/evaluations/1/indicators
Authorization: Bearer <token>
Content-Type: application/json

{
  "tableCount": 100,
  "dataVolume": 500,
  "userCount": 200,
  "reportCount": 50,
  "hasCustomDev": true,
  "customDevCount": 10,
  "customDevWorkload": 50,
  "dataCleanDesc": "需要清洗历史数据",
  "dataCleanComplexity": 2
}
```

### 4.4 获取项目可选模块

**请求**

```http
GET /api/evaluations/1/modules
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "moduleId": 1,
      "moduleName": "财务会计",
      "category": "财务",
      "baseWorkload": 20,
      "defaultWeight": 1.0,
      "weight": 1.0,
      "checked": false
    }
  ]
}
```

### 4.5 保存模块配置

**请求**

```http
POST /api/evaluations/1/modules
Authorization: Bearer <token>
Content-Type: application/json

[
  {
    "moduleId": 1,
    "moduleName": "财务会计",
    "category": "财务",
    "baseWorkload": 20,
    "defaultWeight": 1.0,
    "weight": 1.2,
    "checked": true
  }
]
```

### 4.6 计算工作量

**请求**

```http
POST /api/evaluations/1/calculate
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "projectId": 1,
    "coreWorkload": 156.38,
    "reportWorkload": 25.0,
    "customDevWorkload": 50.0,
    "totalWorkload": 231.38,
    "moduleWorkloads": [
      {
        "moduleId": 1,
        "moduleName": "财务会计",
        "baseWorkload": 20,
        "weight": 1.2,
        "dataVolumeWeight": 1.0,
        "userCountWeight": 1.2,
        "moduleWorkload": 28.8
      }
    ],
    "dataVolumeWeight": 1.0,
    "userCountWeight": 1.2,
    "reportCoefficient": 0.5
  }
}
```

### 4.7 完成评估

**请求**

```http
POST /api/evaluations/1/complete
Authorization: Bearer <token>
```

---

## 五、阶梯匹配接口

### 5.1 数据量阶梯匹配

**请求**

```http
GET /api/ladder/data-volume/match?volume=500
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "ladderId": 2,
    "ladderName": "中型",
    "weight": 1.0,
    "ladderType": "DATA_VOLUME",
    "inputValue": "500万条",
    "rangeText": "10万-1000万条"
  }
}
```

### 5.2 用户数阶梯匹配

**请求**

```http
GET /api/ladder/user-count/match?count=200
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "ladderId": 2,
    "ladderName": "中规模",
    "weight": 1.2,
    "ladderType": "USER_COUNT",
    "inputValue": "200人",
    "rangeText": "100-500人"
  }
}
```

---

## 六、系统类型接口

### 6.1 分页查询系统类型

**请求**

```http
GET /api/system/types?pageNum=1&pageSize=10&typeName=SAP
Authorization: Bearer <token>
```

### 6.2 获取启用的系统类型

**请求**

```http
GET /api/system/types/enabled
Authorization: Bearer <token>
```

### 6.3 创建系统类型

**请求**

```http
POST /api/system/types
Authorization: Bearer <token>
Content-Type: application/json

{
  "typeName": "Oracle EBS",
  "typeCode": "ORACLE_EBS",
  "description": "Oracle电子商务套件",
  "sortOrder": 10,
  "enabled": true
}
```

### 6.4 更新系统类型

**请求**

```http
PUT /api/system/types/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "typeName": "Oracle EBS Pro",
  "description": "Oracle电子商务套件专业版"
}
```

### 6.5 删除系统类型

**请求**

```http
DELETE /api/system/types/1
Authorization: Bearer <token>
```

### 6.6 启用/禁用系统类型

**请求**

```http
PATCH /api/system/types/1/enable
Authorization: Bearer <token>
```

---

## 七、模块管理接口

### 7.1 分页查询模块

**功能**：分页查询模块列表（REQ-3.1.10），支持按名称模糊、按系统/分类/状态筛选，固定 `ORDER BY create_time DESC` 排序。

**请求**

```http
GET /modules?pageNum=1&pageSize=10&moduleName=财务&systemId=1&category=HR&status=1
Authorization: Bearer <token>
```

**Query 参数**：

| 字段 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| pageNum | int | 否 | 1 | 页码，< 1 截断为 1，> totalPages 时前端自动跳到最后一页 |
| pageSize | int | 否 | 10 | 每页条数，< 1 截断为 10，> 200 截断为 200 |
| moduleName | string | 否 | - | 模块名称模糊匹配（`LIKE '%xxx%'`） |
| systemId | long | 否 | - | 所属系统 ID，精确匹配 |
| category | string | 否 | - | 模块分类，精确匹配 |
| status | int | 否 | - | 状态：0=禁用，1=启用 |

**响应（成功）**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "moduleName": "财务会计",
        "systemId": 1,
        "systemName": "SAP",
        "category": "财务模块",
        "baseWorkload": 15.00,
        "defaultWeight": 1.00,
        "description": "财务核算",
        "status": 1,
        "createTime": "2026-03-19T20:30:14",
        "updateTime": "2026-04-03T11:57:50"
      }
    ],
    "total": 25,
    "pageNum": 1,
    "pageSize": 10,
    "totalPages": 3,
    "hasPrevious": false,
    "hasNext": true,
    "fromIndex": 1,
    "toIndex": 10
  },
  "timestamp": "2026-06-09T10:00:00"
}
```

**响应字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| records | ModuleVO[] | 当前页数据 |
| total | long | 满足筛选条件的总记录数 |
| pageNum | int | 当前页码 |
| pageSize | int | 当前每页大小 |
| totalPages | int | 总页数 = ceil(total/pageSize) |
| hasPrevious | boolean | 是否有上一页 |
| hasNext | boolean | 是否有下一页 |
| fromIndex | int | 当前页首条记录在全集的索引（从 1 开始） |
| toIndex | int | 当前页末条记录在全集的索引 |

**业务规则**：
- 所有筛选条件（moduleName / systemId / category / status）AND 组合
- 不传筛选参数 = 全量（受 status 软删除过滤）
- `deleted = 1` 的软删除记录**不返回**（MyBatis-Plus `@TableLogic` 自动加条件）
- 排序固定 `ORDER BY create_time DESC`，不可配置

**错误响应**：

| HTTP | code | message | 触发条件 |
|------|------|---------|----------|
| 200 | 50005 | 参数无效 | pageNum / pageSize 非整数（如传 "abc"） |
| 200 | 10001 | 鉴权失败 | JWT 无效或过期（由 GlobalExceptionHandler 统一处理） |

**性能预期**：单次 queryPage < 100ms（千级数据）。

**E2E 覆盖**：见 `docs/develop/测试方案及测试计划（合集）.md` §12.4。

### 7.2 获取启用的模块

**请求**

```http
GET /modules/enabled
Authorization: Bearer <token>
```

### 7.3 获取所有模块分类

**请求**

```http
GET /modules/categories
Authorization: Bearer <token>
```

### 7.4 创建模块

**请求**

```http
POST /modules
Authorization: Bearer <token>
Content-Type: application/json

{
  "moduleName": "人力资源",
  "category": "HR",
  "baseWorkload": 15,
  "defaultWeight": 1.0,
  "description": "人力资源管理模块",
  "enabled": true
}
```

---

## 八、阶梯配置接口

### 8.1 数据量阶梯管理

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/ladder/data-volume` | 获取所有数据量阶梯 |
| GET | `/api/ladder/data-volume/{id}` | 获取阶梯详情 |
| POST | `/api/ladder/data-volume` | 创建阶梯 |
| PUT | `/api/ladder/data-volume/{id}` | 更新阶梯 |
| DELETE | `/api/ladder/data-volume/{id}` | 删除阶梯 |

### 8.2 用户数阶梯管理

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/ladder/user-count` | 获取所有用户数阶梯 |
| GET | `/api/ladder/user-count/{id}` | 获取阶梯详情 |
| POST | `/api/ladder/user-count` | 创建阶梯 |
| PUT | `/api/ladder/user-count/{id}` | 更新阶梯 |
| DELETE | `/api/ladder/user-count/{id}` | 删除阶梯 |

---

## 九、报表配置接口

### 9.1 获取报表配置

**请求**

```http
GET /api/config/report
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "reportCoefficient": 0.5,
    "smallReportThreshold": 20,
    "mediumReportThreshold": 50,
    "largeReportThreshold": 100,
    "customDevDailyRate": 1500
  }
}
```

### 9.2 更新报表配置

**请求**

```http
PUT /api/config/report
Authorization: Bearer <token>
Content-Type: application/json

{
  "reportCoefficient": 0.6,
  "smallReportThreshold": 25,
  "mediumReportThreshold": 60,
  "largeReportThreshold": 120,
  "customDevDailyRate": 1800
}
```

---

## 十、统计分析接口

> 权限要求：仪表盘接口（10.1）对所有登录用户开放；项目级统计（10.2）需要项目访问权限；全局聚合（10.3、10.4）需 `ADMIN` 角色。

### 10.1 获取首页仪表盘概览

**请求**

```http
GET /api/statistics/overview
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "projectCount": 12,
    "evaluationCount": 15,
    "totalWorkload": 1830.50,
    "userCount": 3
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| projectCount | Long | 项目总数（含草稿/进行中/已完成/已归档） |
| evaluationCount | Long | 评估记录总数 |
| totalWorkload | BigDecimal | 全部已评估项目工作量之和（人天） |
| userCount | Long | 系统注册用户数 |

**业务规则**：
- 数据统计包含软删除（`deleted=0`）的全部记录
- `totalWorkload` 为 null 时返回 0，不抛异常
- 前端进入首页时调用，结果缓存 60 秒

### 10.2 获取项目统计结果

**请求**

```http
GET /api/statistics/{projectId}
Authorization: Bearer <token>
```

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| projectId | Path | Long | 是 | 项目ID |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "projectId": 1,
    "totalWorkload": 231.38,
    "estimatedMonths": 10.52,
    "workloadTypeDistribution": [
      { "type": "核心迁移", "workload": 156.38, "percentage": 67.60 },
      { "type": "报表迁移", "workload": 50.00, "percentage": 21.61 },
      { "type": "客开定制", "workload": 25.00, "percentage": 10.79 }
    ],
    "moduleWorkloads": [
      {
        "moduleName": "财务管理",
        "category": "财务",
        "baseWorkload": 15.00,
        "weight": 1.30,
        "workload": 43.88,
        "percentage": 28.06
      }
    ],
    "multiDimensionIndicators": {
      "dataVolumeValue": 65.00,
      "dataVolumeActual": "650万条",
      "dataVolumeLadder": "大型",
      "userCountValue": 60.00,
      "userCountActual": "600人",
      "userCountLadder": "中大型",
      "moduleCountValue": 25.00,
      "moduleCountActual": 5,
      "reportCountValue": 30.00,
      "reportCountActual": 30,
      "customDevValue": 80.00,
      "hasCustomDev": true,
      "customDevWorkload": 25.00
    },
    "riskWarnings": [
      {
        "type": "MODULE_COMPLEXITY",
        "level": "高",
        "description": "模块【财务管理】复杂度较高（系数：1.3）",
        "suggestion": "建议增加该模块的测试时间和数据验证工作量"
      }
    ],
    "evaluationOverview": {
      "moduleCount": 5,
      "dataVolume": 650.00,
      "dataVolumeLadder": "大型",
      "userCount": 600,
      "userCountLadder": "中大型",
      "reportCount": 30,
      "hasCustomDev": true
    }
  }
}
```

**业务规则**：
- 项目无评估记录时返回 200 + 零值 VO，不抛 404（前端据此渲染"待评估"占位）
- `estimatedMonths = totalWorkload / 22`（22 人天/人月）
- 维度值采用 0-100 归一化（参考 MAX_DATA_VOLUME=10000 万条、MAX_USER_COUNT=1000 人、MAX_MODULE_COUNT=20、MAX_REPORT_COUNT=100）

**错误码**：
| code | 含义 |
|------|------|
| 200 | 成功（含空数据） |
| 10003 | 项目不存在（仅当项目ID格式错误时，业务逻辑不返回 404） |

### 10.3 全局聚合统计

**请求**

```http
GET /api/statistics/global/aggregations?dimension=module&dateFrom=2026-01-01&dateTo=2026-12-31&sourceSystemId=1&status=COMPLETED
Authorization: Bearer <token>
```

| 参数 | 位置 | 类型 | 必填 | 取值范围 | 说明 |
|------|------|------|------|---------|------|
| dimension | Query | String | 是 | `module` \| `type` \| `complexity` | 聚合维度 |
| dateFrom | Query | Date | 否 | yyyy-MM-dd | 评估日期起点 |
| dateTo | Query | Date | 否 | yyyy-MM-dd | 评估日期终点 |
| sourceSystemId | Query | Long | 否 | - | 源系统ID过滤 |
| targetSystemId | Query | Long | 否 | - | 目标系统ID过滤 |
| status | Query | String | 否 | `DRAFT`/`IN_PROGRESS`/`COMPLETED`/`ARCHIVED` | 项目状态过滤 |

**dimension=module 响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dimension": "module",
    "total": 8,
    "items": [
      { "name": "财务管理", "value": 320.50, "percentage": 25.30, "projectCount": 5 },
      { "name": "供应链管理", "value": 280.00, "percentage": 22.10, "projectCount": 4 }
    ]
  }
}
```

**dimension=type 响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dimension": "type",
    "total": 1267.00,
    "items": [
      { "name": "核心迁移", "value": 856.50, "percentage": 67.60, "projectCount": 10 },
      { "name": "报表迁移", "value": 285.00, "percentage": 22.50, "projectCount": 8 },
      { "name": "客开定制", "value": 125.50, "percentage": 9.90, "projectCount": 3 }
    ]
  }
}
```

**dimension=complexity 响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dimension": "complexity",
    "dataVolumeDistribution": [
      { "ladderName": "小型", "projectCount": 3, "totalWorkload": 250.00 },
      { "ladderName": "大型", "projectCount": 5, "totalWorkload": 800.00 }
    ],
    "userCountDistribution": [
      { "ladderName": "小型", "projectCount": 2, "totalWorkload": 180.00 },
      { "ladderName": "中大型", "projectCount": 4, "totalWorkload": 520.00 }
    ],
    "highComplexityModules": [
      { "moduleName": "财务核算", "weight": 1.80, "projectCount": 6 }
    ]
  }
}
```

**业务规则**：
- 仅聚合 `status='COMPLETED'` 的项目（默认行为；通过 `status` 查询参数可调整）
- 空数据时 `items: []`，HTTP 仍返回 200
- 过滤条件可组合，`dateFrom`/`dateTo` 基于 `evaluation.create_time`

**错误码**：
| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | dimension 非法值 |
| 11001 | 权限不足（非 ADMIN） |

### 10.4 全局排行榜

**请求**

```http
GET /api/statistics/global/ranking?metric=workload&limit=10
Authorization: Bearer <token>
```

| 参数 | 位置 | 类型 | 必填 | 取值范围 | 说明 |
|------|------|------|------|---------|------|
| metric | Query | String | 是 | `workload` \| `userCount` \| `dataVolume` | 排序指标 |
| limit | Query | Integer | 否 | 1-50，默认 10 | 返回条数 |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "metric": "workload",
    "items": [
      {
        "projectId": 12,
        "projectName": "XX集团ERP迁移",
        "customerName": "XX集团",
        "value": 320.50,
        "unit": "人天"
      }
    ]
  }
}
```

**业务规则**：
- 仅返回已评估（存在 evaluation 记录）且 `status='COMPLETED'` 的项目
- `value` 单位：`workload`→人天 / `userCount`→人 / `dataVolume`→万条
- 同值按 `project_id` 升序

**错误码**：
| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | metric/limit 非法值 |
| 11001 | 权限不足（非 ADMIN） |

---

## 十一、导出接口

### 11.1 导出评估报告

**请求**

```http
POST /api/export/report/1?format=excel
Authorization: Bearer <token>
```

**响应**

- Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
- 文件下载

---

## 十二、用户管理接口

> 权限要求：以下所有接口均需要管理员权限（`@PreAuthorize("hasRole('ADMIN')")`）。

### 12.1 分页查询用户

**请求**

```http
GET /api/users/page?current=1&pageSize=10&username=admin&role=ADMIN&status=1
Authorization: Bearer <token>
```

**Query 参数**

| 字段 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| username | String | 否 | - | 用户名模糊匹配 |
| name | String | 否 | - | 姓名模糊匹配 |
| role | String | 否 | - | 角色精确匹配：`ADMIN` / `USER` |
| status | Integer | 否 | - | 状态精确匹配：0=禁用，1=启用 |
| current | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页条数 |

**响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "name": "系统管理员",
        "role": "ADMIN",
        "roleName": "管理员",
        "status": 1,
        "statusName": "启用",
        "createTime": "2026-03-19T20:30:14"
      }
    ],
    "total": 2,
    "pageNum": 1,
    "pageSize": 10,
    "totalPages": 1
  }
}
```

### 12.2 获取用户详情

**请求**

```http
GET /api/users/1
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "系统管理员",
    "email": null,
    "phone": null,
    "role": "ADMIN",
    "roleName": "管理员",
    "status": 1,
    "statusName": "启用",
    "createTime": "2026-03-19T20:30:14",
    "lastLoginTime": "2026-06-09T16:00:00",
    "remark": "系统管理员账号"
  }
}
```

### 12.3 创建用户

**请求**

```http
POST /api/users
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "zhangsan",
  "password": "123456",
  "name": "张三",
  "role": "USER",
  "remark": "普通用户"
}
```

**请求体字段**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| username | String | 是 | 3-50 字符 | 登录账号，创建后不可修改 |
| password | String | 是 | 6-100 字符 | 登录密码 |
| name | String | 是 | 最大 50 字符 | 用户姓名 |
| role | String | 是 | `ADMIN` / `USER` | 用户角色 |
| email | String | 否 | 邮箱格式，最大 100 字符 | 邮箱 |
| phone | String | 否 | 最大 20 字符 | 手机号 |
| remark | String | 否 | 最大 500 字符 | 备注 |

**响应**

```json
{ "code": 200, "message": "操作成功", "data": 5 }
```

### 12.4 更新用户

**请求**

```http
PUT /api/users/5
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "张三（修改）",
  "role": "ADMIN"
}
```

> 不可修改 `username` 和 `password`，`id` 由路径参数自动设置。

### 12.5 删除用户

**请求**

```http
DELETE /api/users/5
Authorization: Bearer <token>
```

> 管理员账号不能删除。

### 12.6 重置密码

**请求**

```http
POST /api/users/password/reset
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": 5,
  "newPassword": "123456",
  "confirmPassword": "123456"
}
```

> `newPassword` 与 `confirmPassword` 必须一致，由服务层校验。

### 12.7 启用/禁用用户

**请求**

```http
PUT /api/users/5/status?status=0
Authorization: Bearer <token>
```

> `status`：0=禁用，1=启用。管理员账号不能禁用（至少保留一个启用的管理员）。

### 12.8 检查用户名是否可用

**请求**

```http
GET /api/users/check/username?username=zhangsan
Authorization: Bearer <token>
```

**响应**

```json
{ "code": 200, "message": "操作成功", "data": false }
```

> `data` 为 `true` 表示用户名已存在，`false` 表示可用。编辑时可传 `excludeId` 排除自身。

---

## 十二 x、项目状态历史接口（§3.2.4）

> 权限要求：所有登录用户可查询；仅 ADMIN 可补录。
> 业务说明：项目状态（草稿/进行中/已完成/已归档）每次自动变更都产生一条历史记录，append-only。

### 12.x.1 分页查询项目状态历史

**请求**

```http
GET /api/projects/{projectId}/status-history?pageNum=1&pageSize=10&operator=admin&event=EVAL_COMPLETE
Authorization: Bearer <token>
```

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| projectId | Path | Long | 是 | 项目ID |
| pageNum | Query | Integer | 否 | 页码，默认 1 |
| pageSize | Query | Integer | 否 | 每页大小，默认 10，最大 50 |
| operator | Query | String | 否 | 按操作人精确过滤 |
| event | Query | String | 否 | 按事件类型过滤：CREATE/EVAL_START/EVAL_COMPLETE/ARCHIVE/MANUAL_EDIT |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 17,
        "projectId": 365,
        "fromStatus": "COMPLETED",
        "toStatus": "ARCHIVED",
        "event": "ARCHIVE",
        "operator": "admin",
        "reason": "客户验收完成",
        "changeTime": "2026-06-11T14:32:15",
        "manualEdit": false
      },
      {
        "id": 15,
        "projectId": 365,
        "fromStatus": "IN_PROGRESS",
        "toStatus": "COMPLETED",
        "event": "EVAL_COMPLETE",
        "operator": "admin",
        "reason": null,
        "changeTime": "2026-06-11T12:25:33",
        "manualEdit": false
      }
    ],
    "total": 4,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| records[].id | Long | 历史记录ID |
| records[].projectId | Long | 项目ID |
| records[].fromStatus | String | 变更前状态（CREATE 事件时为 null） |
| records[].toStatus | String | 变更后状态 |
| records[].event | String | 事件：CREATE/EVAL_START/EVAL_COMPLETE/ARCHIVE/MANUAL_EDIT |
| records[].operator | String | 操作人 |
| records[].reason | String | 备注（可空） |
| records[].changeTime | String | 变更时间 |
| records[].manualEdit | Boolean | 是否人工补录 |

**业务规则**：
- 按 `change_time DESC` 排序，最新变更在顶
- 普通用户只能查自己创建的项目；ADMIN 可查所有项目（依赖业务权限中间件）
- 0 条数据时返 200 + 空数组

**错误码**：
| code | 含义 |
|------|------|
| 200 | 成功 |
| 401 | 未登录 |
| 403 | 无权访问（非创建者且非 ADMIN）|

### 12.x.2 人工补录状态历史（仅 ADMIN）

**请求**

```http
POST /api/projects/{projectId}/status-history
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "fromStatus": "DRAFT",
  "toStatus": "IN_PROGRESS",
  "event": "MANUAL_EDIT",
  "reason": "存量项目状态机上线前补录",
  "changeTime": "2026-04-15T10:30:00"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fromStatus | String | 是 | 变更前状态（DRAFT/IN_PROGRESS/COMPLETED/ARCHIVED） |
| toStatus | String | 是 | 变更后状态 |
| event | String | 是 | 必须为 `MANUAL_EDIT` |
| reason | String | 否 | 业务备注 |
| changeTime | String | 是 | 补录的时间（任意过去时间） |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": 18
}
```

`data` 为新创建的历史记录 ID。

**业务规则**：
- 仅 ADMIN 角色可调用
- 补录的 `event` **必须为 `MANUAL_EDIT`**，其他事件系统自动写
- `manual_edit` 字段自动写 1
- `from_status` 和 `to_status` 校验不同时（不能同状态补录）
- `change_time` 不能是未来时间
- 同一 `(project_id, from_status, to_status, event, change_time)` 不允许重复（防误点）

**错误码**：
| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数非法（同状态/未来时间/event 不为 MANUAL_EDIT/重复）|
| 401 | 未登录 |
| 403 | 非 ADMIN |

---

## 十三、错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（Token无效或过期） |
| 403 | 禁止访问（权限不足） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 十四、状态码说明

### 项目状态

| 状态值 | 说明 |
|--------|------|
| DRAFT | 草稿 |
| IN_PROGRESS | 进行中 |
| COMPLETED | 已完成 |
| ARCHIVED | 已归档 |

### 评估状态

| 状态值 | 说明 |
|--------|------|
| DRAFT | 未评估 |
| IN_PROGRESS | 评估中 |
| COMPLETED | 已完成 |

---

**文档结束**
