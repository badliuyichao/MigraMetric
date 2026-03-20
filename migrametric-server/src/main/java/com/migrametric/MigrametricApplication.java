package com.migrametric;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 异构系统升迁工作量评估系统 - 启动类
 *
 * @author MigraMetric Team
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.migrametric.mapper")
public class MigrametricApplication {

    public static void main(String[] args) {
        SpringApplication.run(MigrametricApplication.class, args);
        System.out.println("""

            ╔══════════════════════════════════════════════════════════════╗
            ║                                                              ║
            ║     异构系统升迁工作量评估系统 (MigraMetric)                   ║
            ║                                                              ║
            ║     服务已启动成功！                                          ║
            ║                                                              ║
            ║     API文档地址: http://localhost:8080/swagger-ui.html        ║
            ║                                                              ║
            ╚══════════════════════════════════════════════════════════════╝
            """);
    }
}
