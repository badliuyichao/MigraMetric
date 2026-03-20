# MigraMetric

三方异构系统升迁评估工具

## 项目简介

MigraMetric 是一款用于评估和量化企业异构系统升迁工作量的工具。通过自动化分析系统架构、功能模块和技术栈，帮助企业准确预估迁移成本和工期。

## 技术架构

```
┌─────────────────────────────────────────────────────────────┐
│                        MigraMetric                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────┐      ┌─────────────────────────┐  │
│  │   migrametric-web    │      │   migrametric-server    │  │
│  │    (前端 Vue 3)      │ ──── │    (后端 Spring Boot)    │  │
│  └─────────────────────┘      └─────────────────────────┘  │
│           │                            │                   │
│     Element Plus                    MyBatis-Plus            │
│       ECharts                        MySQL 8                │
│       Pinia                          JWT                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 项目结构

```
MigraMetric/
├── docs/                      # 项目文档
│   └── 设计文档.md
├── migrametric-server/        # 后端服务
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/migrametric/
│   │   │   │   ├── config/      # 配置类
│   │   │   │   ├── controller/   # 控制器
│   │   │   │   ├── service/      # 服务层
│   │   │   │   ├── mapper/       # 持久层
│   │   │   │   ├── entity/       # 实体类
│   │   │   │   ├── dto/          # 数据传输对象
│   │   │   │   ├── vo/           # 视图对象
│   │   │   │   ├── common/       # 公共类
│   │   │   │   └── util/         # 工具类
│   │   │   └── resources/        # 配置文件
│   │   └── test/                 # 测试代码
│   └── README.md
├── migrametric-web/           # 前端项目
│   ├── src/
│   │   ├── api/                # API接口
│   │   ├── assets/             # 静态资源
│   │   ├── components/         # 组件
│   │   ├── composables/        # 组合式函数
│   │   ├── layouts/            # 布局
│   │   ├── router/             # 路由
│   │   ├── stores/             # 状态管理
│   │   ├── styles/             # 样式
│   │   ├── utils/              # 工具函数
│   │   └── views/              # 页面
│   └── README.md
├── LICENSE
└── README.md
```

## 技术栈

### 后端 (migrametric-server)

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17+ | 编程语言 |
| Spring Boot | 3.2 | 应用框架 |
| MyBatis-Plus | 3.5 | ORM框架 |
| MySQL | 8.0 | 数据库 |
| JWT | - | 身份认证 |
| Swagger/OpenAPI | 3 | API文档 |

### 前端 (migrametric-web)

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4 | 渐进式框架 |
| TypeScript | 5.3 | 类型系统 |
| Vite | 5.0 | 构建工具 |
| Element Plus | 2.5 | UI组件库 |
| ECharts | 5.5 | 数据可视化 |
| Pinia | 2.1 | 状态管理 |
| Vue Router | 4.2 | 路由管理 |

## 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Node.js 18+
- pnpm 8+

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd MigraMetric
```

### 2. 启动后端服务

```bash
cd migrametric-server

# 配置数据库，编辑 src/main/resources/application.yml

# 启动服务
mvn spring-boot:run
```

### 3. 启动前端项目

```bash
cd migrametric-web

# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev
```

### 4. 访问应用

- 前端地址: http://localhost:3000
- 后端API文档: http://localhost:8080/swagger-ui.html

## 子项目文档

- [后端服务文档](./migrametric-server/README.md)
- [前端项目文档](./migrametric-web/README.md)

## 许可证

Apache License 2.0
