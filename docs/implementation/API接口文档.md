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
    "totalWorkload": 231.38,
    "evaluationStatus": "COMPLETED",
    "evaluationStatusText": "已完成"
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

**请求**

```http
GET /modules?pageNum=1&pageSize=10&moduleName=财务
Authorization: Bearer <token>
```

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

### 10.1 获取项目统计信息

**请求**

```http
GET /api/statistics/project/1
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "projectId": 1,
    "projectName": "XX集团ERP迁移项目",
    "totalWorkload": 231.38,
    "moduleCount": 5,
    "evaluationDate": "2026-03-25"
  }
}
```

### 10.2 工作量类型分布

**请求**

```http
GET /api/statistics/project/1/workload-type
Authorization: Bearer <token>
```

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

## 十二、错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（Token无效或过期） |
| 403 | 禁止访问（权限不足） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 十三、状态码说明

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
