package com.migrametric.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Result统一返回类单元测试
 *
 * @author MigraMetric Team
 */
@DisplayName("Result统一返回类测试")
class ResultTest {

    @Test
    @DisplayName("Result.success() 返回正确状态码")
    void testSuccessResult() {
        Result<String> result = Result.success("test data");

        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals("test data", result.getData());
        assertNotNull(result.getTimestamp());
    }

    @Test
    @DisplayName("Result.success() 无数据返回")
    void testSuccessResultWithoutData() {
        Result<Void> result = Result.success();

        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
    }

    @Test
    @DisplayName("Result.success(T data) 支持泛型")
    void testSuccessWithData() {
        List<String> list = Arrays.asList("a", "b", "c");
        Result<List<String>> result = Result.success(list);

        assertEquals(200, result.getCode());
        assertEquals(3, result.getData().size());
        assertEquals("a", result.getData().get(0));
    }

    @Test
    @DisplayName("Result.success(String, T) 带消息和数据")
    void testSuccessWithMessageAndData() {
        Result<Integer> result = Result.success("查询成功", 100);

        assertEquals(200, result.getCode());
        assertEquals("查询成功", result.getMessage());
        assertEquals(100, result.getData());
    }

    @Test
    @DisplayName("Result.fail() 返回错误信息")
    void testFailResult() {
        Result<Void> result = Result.fail(500, "系统异常");

        assertEquals(500, result.getCode());
        assertEquals("系统异常", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("Result.fail(String) 使用默认错误码")
    void testFailWithMessage() {
        Result<Void> result = Result.fail("业务处理失败");

        assertEquals(500, result.getCode());
        assertEquals("业务处理失败", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("Result.fail(ResultCode) 使用枚举")
    void testFailWithResultCode() {
        Result<Void> result = Result.fail(ResultCode.USERNAME_PASSWORD_ERROR);

        assertEquals(10001, result.getCode());
        assertEquals("用户名或密码错误", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("isSuccess() 返回true当code为200")
    void testIsSuccess() {
        Result<String> successResult = Result.success("data");
        assertTrue(successResult.isSuccess());

        Result<Void> failResult = Result.fail("error");
        assertFalse(failResult.isSuccess());
    }

    @Test
    @DisplayName("Result支持复杂对象")
    void testComplexObject() {
        // 模拟复杂数据结构
        TestData data = new TestData();
        data.setId(1L);
        data.setName("测试数据");
        data.setValues(Arrays.asList("v1", "v2", "v3"));

        Result<TestData> result = Result.success(data);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());
        assertEquals("测试数据", result.getData().getName());
        assertEquals(3, result.getData().getValues().size());
    }

    /**
     * 测试用数据类
     */
    static class TestData {
        private Long id;
        private String name;
        private List<String> values;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getValues() { return values; }
        public void setValues(List<String> values) { this.values = values; }
    }
}
