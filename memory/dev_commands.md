---
name: 开发命令速查
description: 常用开发命令和工具
type: reference
---

**后端 (migrametric-server)**:
```bash
cd migrametric-server
mvn spring-boot:run              # 启动开发服务器 (端口 8080)
mvn test                          # 运行所有测试
mvn test -Dtest=ClassName        # 运行指定测试类
mvn test jacoco:report            # 生成覆盖率报告
mvn clean package -DskipTests     # 构建 JAR 包
```

**前端 (migrametric-web)**:
```bash
cd migrametric-web
pnpm install                      # 安装依赖
pnpm dev                         # 启动开发服务器 (端口 3000，自动打开浏览器)
pnpm build                       # 生产环境构建
pnpm lint                        # ESLint 检查并自动修复
pnpm type-check                  # TypeScript 类型检查
pnpm test:unit                    # 运行单元测试 (单次执行)
pnpm test:coverage               # 生成覆盖率报告
```

**数据库**:
- 数据库: MySQL 8.0
- 建表脚本: `docs/init-db.sql` (44KB)
- 配置文件: `migrametric-server/src/main/resources/application.yml`

**API文档**:
- Swagger UI: http://localhost:8080/swagger-ui.html

**Why**: 快速执行常用操作，提高开发效率
**How to apply**: 需要启动服务、运行测试、构建项目时参考