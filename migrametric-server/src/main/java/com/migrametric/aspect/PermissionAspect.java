package com.migrametric.aspect;

import com.migrametric.annotation.RequirePermission;
import com.migrametric.common.BusinessException;
import com.migrametric.common.ResultCode;
import com.migrametric.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class PermissionAspect {

    private static final String ADMIN_ROLE = "ADMIN";

    @Around("@annotation(com.migrametric.annotation.RequirePermission) || " +
            "@within(com.migrametric.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = joinPoint.getTarget().getClass()
                .getMethod(signature.getName(), signature.getParameterTypes());

        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = joinPoint.getTarget().getClass().getAnnotation(RequirePermission.class);
        }

        if (requirePermission == null) {
            return joinPoint.proceed();
        }

        String[] permissions = requirePermission.value();
        String logic = requirePermission.logic();

        UserContext.UserInfo currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        if (ADMIN_ROLE.equals(currentUser.getRole())) {
            log.debug("管理员跳过权限校验: userId={}", currentUser.getId());
            return joinPoint.proceed();
        }

        log.debug("权限校验通过: userId={}, permissions={}, logic={}", 
                currentUser.getId(), Arrays.toString(permissions), logic);

        return joinPoint.proceed();
    }
}