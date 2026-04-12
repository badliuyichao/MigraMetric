package com.migrametric.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BusinessException业务异常类单元测试
 *
 * @author MigraMetric Team
 */
@DisplayName("BusinessException业务异常类测试")
class BusinessExceptionTest {

    @Test
    @DisplayName("构造函数 - code和message")
    void testConstructorWithCodeAndMessage() {
        BusinessException exception = new BusinessException(1001, "业务异常");

        assertEquals(1001, exception.getCode());
        assertEquals("业务异常", exception.getMessage());
        assertNull(exception.getDetails());
    }

    @Test
    @DisplayName("构造函数 - ResultCode枚举")
    void testConstructorWithResultCode() {
        BusinessException exception = new BusinessException(ResultCode.USERNAME_PASSWORD_ERROR);

        assertEquals(10001, exception.getCode());
        assertEquals("用户名或密码错误", exception.getMessage());
        assertNull(exception.getDetails());
    }

    @Test
    @DisplayName("构造函数 - ResultCode和自定义消息")
    void testConstructorWithResultCodeAndMessage() {
        BusinessException exception = new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");

        assertEquals(12002, exception.getCode());
        assertEquals("项目不存在", exception.getMessage());
    }

    @Test
    @DisplayName("构造函数 - ResultCode和details")
    void testConstructorWithResultCodeAndDetails() {
        Object details = new Object();
        BusinessException exception = new BusinessException(ResultCode.VALIDATION_ERROR, details);

        assertEquals(12001, exception.getCode());
        assertEquals("数据校验失败", exception.getMessage());
        assertNotNull(exception.getDetails());
        assertEquals(details, exception.getDetails());
    }

    @Test
    @DisplayName("构造函数 - code, message和details")
    void testConstructorWithAllParameters() {
        Object details = new Object();
        BusinessException exception = new BusinessException(9999, "自定义错误", details);

        assertEquals(9999, exception.getCode());
        assertEquals("自定义错误", exception.getMessage());
        assertNotNull(exception.getDetails());
    }

    @Test
    @DisplayName("异常可被正常抛出和捕获")
    void testExceptionCanBeThrownAndCaught() {
        // Given
        BusinessException original = new BusinessException(ResultCode.PROJECT_NOT_FOUND);

        // When & Then
        BusinessException caught = assertThrows(BusinessException.class, () -> {
            throw original;
        });

        assertEquals(original.getCode(), caught.getCode());
        assertEquals(original.getMessage(), caught.getMessage());
    }

    @Test
    @DisplayName("getMessage返回正确消息")
    void testGetMessage() {
        BusinessException exception = new BusinessException(5001, "测试消息");

        assertEquals("测试消息", exception.getMessage());
        assertEquals("测试消息", exception.getLocalizedMessage());
    }
}
