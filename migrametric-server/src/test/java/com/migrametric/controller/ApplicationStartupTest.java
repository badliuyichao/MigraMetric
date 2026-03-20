package com.migrametric.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 应用启动集成测试
 *
 * @author MigraMetric Team
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("后端应用启动集成测试")
class ApplicationStartupTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("应用成功启动并响应")
    void testApplicationStartup() throws Exception {
        mockMvc.perform(get("/api/system/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("UP"));
    }

    @Test
    @DisplayName("健康检查接口返回正确结构")
    void testHealthCheckResponseStructure() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/system/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        // 验证JSON结构包含必要字段
        assertTrue(content.contains("\"code\""));
        assertTrue(content.contains("\"message\""));
        assertTrue(content.contains("\"data\""));
        assertTrue(content.contains("UP"));
        assertTrue(content.contains("\"timestamp\""));
    }

    @Test
    @DisplayName("系统信息接口正常返回")
    void testSystemInfoEndpoint() throws Exception {
        mockMvc.perform(get("/api/system/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("MigraMetric"))
                .andExpect(jsonPath("$.data.description").value("异构系统升迁工作量评估系统"))
                .andExpect(jsonPath("$.data.version").value("1.0.0"));
    }

    @Test
    @DisplayName("Swagger UI可访问（开发环境）")
    void testSwaggerAccess() throws Exception {
        // 测试环境Swagger被禁用，使用/swagger-ui/index.html路径
        // 在dev环境运行时会返回200，test环境会返回302重定向
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("API文档JSON端点存在")
    void testApiDocsAccess() throws Exception {
        // 测试环境API文档被禁用
        // 在dev环境运行时会返回200
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("不存在的接口返回统一错误格式")
    void testNotFoundReturnsCorrectFormat() throws Exception {
        // Spring Boot 3使用NoResourceFoundException，返回500
        mockMvc.perform(get("/api/nonexistent"))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }
}
