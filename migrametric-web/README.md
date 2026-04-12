# MigraMetric Web

异构系统升迁工作量评估系统 - 前端项目

## 技术栈

- Vue 3.4
- TypeScript 5.3
- Vite 5.0
- Element Plus 2.5
- ECharts 5.5
- Pinia 2.1
- Vue Router 4.2
- Axios 1.6
- Vitest

## 项目结构

```
src/
├── api/                    # API接口定义
├── assets/                 # 静态资源
├── components/             # 公共组件
├── composables/            # 组合式函数
├── layouts/                # 布局组件
├── router/                 # 路由配置
│   ├── index.ts           # 路由定义
│   └── permission.ts     # 路由权限控制
├── stores/                 # Pinia状态管理
│   └── user.ts           # 用户状态
├── styles/                 # 全局样式
├── utils/                  # 工具函数
│   ├── request.ts        # Axios封装
│   ├── validate.ts       # 表单验证
│   └── format.ts         # 日期格式化
├── views/                  # 页面组件
│   ├── login/            # 登录页
│   ├── dashboard/        # 首页
│   ├── system/           # 系统管理
│   │   └── types/        # 系统类型管理
│   ├── project/          # 项目管理
│   │   ├── list/         # 项目列表
│   │   ├── create/       # 创建项目
│   │   └── detail/       # 项目详情
│   └── error/            # 错误页面
├── App.vue                 # 根组件
├── main.ts                 # 入口文件
└── env.d.ts               # 类型声明
```

## 环境要求

- Node.js 18+
- pnpm 8+

## 快速开始

### 1. 安装依赖

```bash
pnpm install
```

### 2. 开发模式启动

```bash
pnpm dev
```

访问 http://localhost:3000

### 3. 生产构建

```bash
pnpm build
```

### 4. 预览构建结果

```bash
pnpm preview
```

## 代码检查与格式化

```bash
# ESLint检查
pnpm lint

# Prettier格式化
pnpm lint:style

# TypeScript类型检查
pnpm type-check
```

## 测试

```bash
# 运行单元测试
pnpm test

# 运行单元测试（单次）
pnpm test:unit

# 运行测试并生成覆盖率报告
pnpm test:coverage
```

## 目录规范

### 命名规范

- 组件文件：大驼峰命名（PascalCase）
- 工具文件：小驼峰命名（camelCase）
- 样式文件：小写连字符命名（kebab-case）

### 路径别名

- `@/` 指向 `src/` 目录
- 配置于 `vite.config.ts` 和 `tsconfig.json`

## API文档

API请求基于Axios封装，详情请参考 `src/utils/request.ts`

### 请求示例

```typescript
import { request } from '@/utils/request'

// GET请求
const data = await request.get<UserList>('/users', { pageNum: 1, pageSize: 10 })

// POST请求
await request.post('/login', { username: 'admin', password: '123456' })
```

## 状态管理

使用Pinia进行状态管理，用户状态存储于 `src/stores/user.ts`

### 主要状态

- `token`: JWT令牌
- `userInfo`: 用户信息
- `isLoggedIn`: 登录状态
- `isAdmin`: 是否管理员

## 路由权限

路由权限控制配置于 `src/router/permission.ts`

- 登录页无需权限
- 其他页面需要登录
- 部分页面需要管理员角色

## 许可证

Apache License 2.0
