package com.migrametric.aspect;

import com.migrametric.annotation.RequirePermission;
import com.migrametric.common.BusinessException;
import com.migrametric.common.ResultCode;
import com.migrametric.entity.user.User;
import com.migrametric.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 权限校验切面
 *
 * @author MigraMetric Team
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final UserMapper userMapper;

    /**
     * 管理员角色
     */
    private static final String ADMIN_ROLE = "ADMIN";

    /**
     * 环绕通知，校验权限
     */
    @Around("@annotation(com.migrametric.annotation.RequirePermission) || @within(com.migrametric.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = joinPoint.getTarget().getClass().getMethod(signature.getName(), signature.getParameterTypes());

        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = joinPoint.getTarget().getClass().getAnnotation(RequirePermission.class);
        }

        if (requirePermission == null) {
            return joinPoint.proceed();
        }

        String[] permissions = requirePermission.value();
        String logic = requirePermission.logic();

        // TODO: 从上下文获取当前用户信息和权限
        // 目前暂时跳过权限校验，直接执行方法
        // 实际项目中需要从SecurityContext或Token中获取用户信息

        log.debug("权限校验: permissions={}, logic={}", permissions, logic);

        return joinPoint.proceed();
    }
}
