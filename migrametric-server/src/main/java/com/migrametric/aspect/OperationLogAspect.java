package com.migrametric.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.migrametric.annotation.OperLog;
import com.migrametric.entity.log.OperationLog;
import com.migrametric.service.log.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面
 * 自动记录带有 @OperLog 注解的方法
 *
 * @author MigraMetric Team
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    /**
     * 环绕通知，记录操作日志
     */
    @Around("@annotation(com.migrametric.annotation.OperLog)")
    public Object recordOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取方法注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = joinPoint.getTarget().getClass().getMethod(signature.getName(), signature.getParameterTypes());
        OperLog operLog = method.getAnnotation(OperLog.class);

        // 创建日志实体
        OperationLog operationLog = new OperationLog();
        operationLog.setModule(operLog.module());
        operationLog.setOperationType(operLog.operationType());
        operationLog.setDescription(operLog.description());
        operationLog.setRequestMethod(method.getName());
        operationLog.setHttpMethod(getHttpMethod());

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            operationLog.setRequestUrl(request.getRequestURI());
            operationLog.setIpAddress(getIpAddress(request));
            operationLog.setLocation(getLocation(request));
        }

        // 获取请求参数
        operationLog.setRequestParams(getRequestParams(joinPoint));

        // 设置默认用户信息（后续从SecurityContext获取）
        operationLog.setUserId(getCurrentUserId());
        operationLog.setUsername(getCurrentUsername());

        Object result = null;
        int status = 1; // 默认成功
        String errorMsg = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            status = 0;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            // 计算执行时长
            long executionTime = System.currentTimeMillis() - startTime;
            operationLog.setExecutionTime(executionTime);
            operationLog.setStatus(status);
            operationLog.setErrorMsg(errorMsg);

            // 保存响应数据（截断过长的部分）
            if (result != null) {
                try {
                    String responseJson = objectMapper.writeValueAsString(result);
                    if (responseJson.length() > 4000) {
                        responseJson = responseJson.substring(0, 4000) + "...[truncated]";
                    }
                    operationLog.setResponseData(responseJson);
                } catch (Exception e) {
                    operationLog.setResponseData("[ serialization failed ]");
                }
            }

            operationLog.setCreateTime(LocalDateTime.now());

            // 异步保存日志（使用线程池）
            try {
                operationLogService.saveLog(operationLog);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }
    }

    /**
     * 获取HTTP请求方法
     */
    private String getHttpMethod() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest().getMethod();
        }
        return "UNKNOWN";
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取登录地点（简化实现）
     */
    private String getLocation(HttpServletRequest request) {
        // 简化实现，实际可使用IP库
        String ip = getIpAddress(request);
        if (ip != null && ip.startsWith("127.")) {
            return "本地";
        }
        if (ip != null && ip.startsWith("192.168.")) {
            return "内网";
        }
        return "未知";
    }

    /**
     * 获取当前用户ID（后续从SecurityContext获取）
     */
    private Long getCurrentUserId() {
        // TODO: 从SecurityContext获取当前用户ID
        return 0L;
    }

    /**
     * 获取当前用户名（后续从SecurityContext获取）
     */
    private String getCurrentUsername() {
        // TODO: 从SecurityContext获取当前用户名
        return "system";
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            if (paramNames == null || args == null || paramNames.length == 0) {
                return "";
            }

            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                // 过滤掉HttpServletRequest、MultipartFile等不可序列化的参数
                if (args[i] instanceof HttpServletRequest
                        || args[i] instanceof MultipartFile
                        || args[i] instanceof MultipartFile[]) {
                    params.put(paramNames[i], "[file or request]");
                } else if (args[i] instanceof String || args[i] instanceof Number
                        || args[i] instanceof Boolean) {
                    params.put(paramNames[i], args[i]);
                } else {
                    // 对于复杂对象，序列化为JSON
                    try {
                        params.put(paramNames[i], args[i]);
                    } catch (Exception e) {
                        params.put(paramNames[i], "[object]");
                    }
                }
            }

            String json = objectMapper.writeValueAsString(params);
            // 截断过长的参数
            if (json.length() > 2000) {
                json = json.substring(0, 2000) + "...[truncated]";
            }
            return json;
        } catch (Exception e) {
            log.debug("获取请求参数失败", e);
            return "";
        }
    }
}
