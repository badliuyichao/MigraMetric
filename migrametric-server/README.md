# MigraMetric Server

异构系统升迁工作量评估系统 - 后端服务

## 技术栈

- Java 17
- Spring Boot 3.2
- MyBatis-Plus 3.5
- MySQL 8.0
- JWT
- Swagger/OpenAPI 3

## 项目结构

```
src/
├── main/
│   ├── java/com/migrametric/
│   │   ├── MigrametricApplication.java    # 启动类
│   │   ├── config/                         # 配置类
│   │   │   ├── CorsConfig.java           # 跨域配置
│   │   │   ├── SwaggerConfig.java        # Swagger配置
│   │   │   └── JwtProperties.java        # JWT配置属性
│   │   ├── controller/                     # 控制器层
│   │   ├── service/                        # 服务层
│   │   ├── mapper/                         # 持久层
│   │   ├── entity/                         # 实体类
│   │   ├── dto/                            # 数据传输对象
│   │   ├── vo/                             # 视图对象
│   │   ├── common/                         # 公共类
│   │   │   ├── Result.java               # 统一返回
│   │   │   ├── ResultCode.java           # 结果码枚举
│   │   │   ├── PageResult.java           # 分页结果
│   │   │   ├── BusinessException.java    # 业务异常
│   │   │   └── GlobalExceptionHandler.java # 全局异常处理
│   │   └── util/                           # 工具类
│   └── resources/
│       ├── application.yml                # 应用配置
│       └── logback-spring.xml             # 日志配置
└── test/
    ├── java/                               # 测试代码
    └── resources/
        └── application-test.yml           # 测试配置
```

## 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+

## 快速开始

### 1. 安装依赖

```bash
mvn clean install
```

### 2. 配置数据库

创建数据库并导入初始化脚本：

```sql
CREATE DATABASE migrametric CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 修改配置文件

编辑 `src/main/resources/application.yml`，配置数据库连接信息。

### 4. 启动应用

```bash
mvn spring-boot:run
```

### 5. 访问API文档

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 运行测试

```bash
# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=ResultTest

# 生成测试覆盖率报告
mvn test jacoco:report
```

## 构建部署

```bash
# 打包
mvn clean package -DskipTests

# 运行打包后的jar
java -jar target/migrametric-server-1.0.0-SNAPSHOT.jar
```

## API规范

### 统一返回格式

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {},
    "timestamp": "2026-03-19T10:30:00"
}
```

### 分页返回格式

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "records": [],
        "total": 100,
        "pageNum": 1,
        "pageSize": 10,
        "totalPages": 10,
        "hasPrevious": false,
        "hasNext": true
    },
    "timestamp": "2026-03-19T10:30:00"
}
```

## 许可证

Apache License 2.0
