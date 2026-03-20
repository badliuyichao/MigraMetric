package com.migrametric.controller;

import com.migrametric.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统健康检查接口
 *
 * @author MigraMetric Team
 */
@Tag(name = "系统接口", description = "系统健康检查和基本信息接口")
@RestController
@RequestMapping("/api/system")
public class HealthController {

    /**
     * 健康检查接口
     *
     * @return 健康状态
     */
    @Operation(summary = "健康检查", description = "检查服务是否正常运行")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("UP");
    }

    /**
     * 获取系统信息
     *
     * @return 系统信息
     */
    @Operation(summary = "系统信息", description = "获取系统基本信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "MigraMetric");
        info.put("description", "异构系统升迁工作量评估系统");
        info.put("version", "1.0.0");
        info.put("timestamp", LocalDateTime.now());
        return Result.success(info);
    }
}
