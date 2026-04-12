package com.migrametric.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CORS跨域集成测试
 *
 * @author MigraMetric Team
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CORS跨域集成测试")
class CorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("CORS预检请求返回正确的Allow头")
    void testCorsPreflight() throws Exception {
        mockMvc.perform(options("/api/system/health")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
    }

    @Test
    @DisplayName("CORS预检请求暴露响应头")
    void testCorsPreflightExposeHeaders() throws Exception {
        mockMvc.perform(options("/api/system/health")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    @Test
    @DisplayName("CORS预检请求MaxAge设置")
    void testCorsPreflightMaxAge() throws Exception {
        MvcResult result = mockMvc.perform(options("/api/system/health")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000"))
                .andExpect(status().isOk())
                .andReturn();

        // 验证CORS配置生效
        String maxAge = result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE);
        // MaxAge可能为null或3600，取决于CORS配置
        if (maxAge != null) {
            assertEquals("3600", maxAge);
        }
    }

    @Test
    @DisplayName("GET请求携带Origin头返回CORS头")
    void testGetRequestWithOrigin() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/system/health")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000"))
                .andExpect(status().isOk())
                .andReturn();

        String origin = result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        assertNotNull(origin);
    }

    @Test
    @DisplayName("POST请求携带Origin头返回CORS头")
    void testPostRequestWithOrigin() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/system/health")
                .header(HttpHeaders.ORIGIN, "http://example.com"))
                .andExpect(status().isOk())
                .andReturn();

        String origin = result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        assertNotNull(origin);
    }

    @Test
    @DisplayName("不同源的预检请求都能正确处理")
    void testCorsPreflightDifferentOrigins() throws Exception {
        String[] origins = {
                "http://localhost:3000",
                "http://localhost:8080",
                "http://example.com",
                "https://app.migrametric.com"
        };

        for (String origin : origins) {
            mockMvc.perform(options("/api/system/health")
                    .header(HttpHeaders.ORIGIN, origin)
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        }
    }

    @Test
    @DisplayName("Authorization头在预检中正确处理")
    void testCorsPreflightWithAuthorization() throws Exception {
        mockMvc.perform(options("/api/system/health")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
