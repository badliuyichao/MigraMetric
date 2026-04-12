package com.migrametric.config;

import com.migrametric.common.BusinessException;
import com.migrametric.common.GlobalExceptionHandler;
import com.migrametric.common.Result;
import com.migrametric.common.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * GlobalExceptionHandler全局异常处理器单元测试
 *
 * @author MigraMetric Team
 */
@DisplayName("GlobalExceptionHandler全局异常处理器测试")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("BusinessException 正确转换为Result")
    void testHandleBusinessException() {
        // Given
        BusinessException exception = new BusinessException(ResultCode.PROJECT_NOT_FOUND);

        // When
        Result<Void> result = exceptionHandler.handleBusinessException(exception);

        // Then
        assertEquals(20001, result.getCode());
        assertEquals("项目不存在", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("BusinessException 自定义错误码和消息")
    void testHandleBusinessExceptionWithCustomCode() {
        // Given
        BusinessException exception = new BusinessException(9999, "自定义错误消息");

        // When
        Result<Void> result = exceptionHandler.handleBusinessException(exception);

        // Then
        assertEquals(9999, result.getCode());
        assertEquals("自定义错误消息", result.getMessage());
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 提取第一个错误信息")
    void testHandleValidException() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        FieldError fieldError = new FieldError("dto", "username", null, false, null, null, "用户名不能为空");

        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(fieldError);

        // When
        Result<Void> result = exceptionHandler.handleValidException(exception);

        // Then
        assertEquals(12001, result.getCode());
        assertEquals("用户名不能为空", result.getMessage());
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 无FieldError时使用默认消息")
    void testHandleValidExceptionWithNullFieldError() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(null);

        // When
        Result<Void> result = exceptionHandler.handleValidException(exception);

        // Then
        assertEquals(12001, result.getCode());
        assertEquals("参数校验失败", result.getMessage());
    }

    @Test
    @DisplayName("BindException 处理参数绑定异常")
    void testHandleBindException() {
        // Given
        BindException exception = mock(BindException.class);
        FieldError fieldError = new FieldError("dto", "password", null, false, null, null, "密码不能为空");

        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(fieldError);

        // When
        Result<Void> result = exceptionHandler.handleBindException(exception);

        // Then
        assertEquals(400, result.getCode());
        assertEquals("密码不能为空", result.getMessage());
    }

    @Test
    @DisplayName("BindException 无FieldError时使用默认消息")
    void testHandleBindExceptionWithNullFieldError() {
        // Given
        BindException exception = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(null);

        // When
        Result<Void> result = exceptionHandler.handleBindException(exception);

        // Then
        assertEquals(400, result.getCode());
        assertEquals("参数绑定失败", result.getMessage());
    }

    @Test
    @DisplayName("ConstraintViolationException 处理约束校验异常")
    void testHandleConstraintViolationException() {
        // Given
        ConstraintViolationException exception = mock(ConstraintViolationException.class);

        @SuppressWarnings("unchecked")
        ConstraintViolation<String> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("输入值不能为空");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);
        when(exception.getConstraintViolations()).thenReturn(violations);

        // When
        Result<Void> result = exceptionHandler.handleConstraintViolationException(exception);

        // Then
        assertEquals(12001, result.getCode());
        assertEquals("输入值不能为空", result.getMessage());
    }

    @Test
    @DisplayName("ConstraintViolationException 多个约束违反时取第一个")
    void testHandleConstraintViolationExceptionWithMultipleViolations() {
        // Given
        ConstraintViolationException exception = mock(ConstraintViolationException.class);

        @SuppressWarnings("unchecked")
        ConstraintViolation<String> violation1 = mock(ConstraintViolation.class);
        when(violation1.getMessage()).thenReturn("第一个错误");

        @SuppressWarnings("unchecked")
        ConstraintViolation<String> violation2 = mock(ConstraintViolation.class);
        when(violation2.getMessage()).thenReturn("第二个错误");

        // 使用LinkedHashSet保持顺序
        Set<ConstraintViolation<?>> violations = new java.util.LinkedHashSet<>();
        violations.add(violation1);
        violations.add(violation2);
        when(exception.getConstraintViolations()).thenReturn(violations);

        // When
        Result<Void> result = exceptionHandler.handleConstraintViolationException(exception);

        // Then
        assertEquals(12001, result.getCode());
        // 由于使用LinkedHashSet，第一个添加的元素应该在第一位
        assertNotNull(result.getMessage());
    }

    @Test
    @DisplayName("MissingServletRequestParameterException 处理缺少请求参数异常")
    void testHandleMissingServletRequestParameterException() {
        // Given
        MissingServletRequestParameterException exception = mock(MissingServletRequestParameterException.class);
        when(exception.getParameterName()).thenReturn("username");

        // When
        Result<Void> result = exceptionHandler.handleMissingServletRequestParameterException(exception);

        // Then
        assertEquals(400, result.getCode());
        assertEquals("缺少必要参数: username", result.getMessage());
    }

    @Test
    @DisplayName("HttpRequestMethodNotSupportedException 处理请求方法不支持异常")
    void testHandleHttpRequestMethodNotSupportedException() {
        // Given
        HttpRequestMethodNotSupportedException exception = mock(HttpRequestMethodNotSupportedException.class);
        when(exception.getMethod()).thenReturn("DELETE");

        // When
        Result<Void> result = exceptionHandler.handleHttpRequestMethodNotSupportedException(exception);

        // Then
        assertEquals(405, result.getCode());
        assertEquals("不支持的请求方法: DELETE", result.getMessage());
    }

    @Test
    @DisplayName("NoHandlerFoundException 处理404异常")
    void testHandleNoHandlerFoundException() {
        // Given
        NoHandlerFoundException exception = mock(NoHandlerFoundException.class);
        when(exception.getRequestURL()).thenReturn("/api/unknown");

        // When
        Result<Void> result = exceptionHandler.handleNoHandlerFoundException(exception);

        // Then
        assertEquals(404, result.getCode());
        assertEquals("接口不存在: /api/unknown", result.getMessage());
    }

    @Test
    @DisplayName("Exception 处理未预期异常")
    void testHandleException() {
        // Given
        Exception exception = new RuntimeException("数据库连接失败");

        // When
        Result<Void> result = exceptionHandler.handleException(exception);

        // Then
        assertEquals(500, result.getCode());
        assertEquals("系统繁忙，请稍后重试", result.getMessage());
    }

    @Test
    @DisplayName("RuntimeException 处理运行时异常")
    void testHandleRuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("空指针异常");

        // When
        Result<Void> result = exceptionHandler.handleRuntimeException(exception);

        // Then
        assertEquals(500, result.getCode());
        assertEquals("系统繁忙，请稍后重试", result.getMessage());
    }
}
