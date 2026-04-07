package com.migrametric.filter;

import com.migrametric.context.UserContext;
import com.migrametric.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtService.validateToken(token)) {
            UserContext.UserInfo userInfo = jwtService.parseToken(token);
            if (userInfo != null) {
                // 设置到自定义上下文
                UserContext.setCurrentUser(userInfo);

                // 设置到 Spring Security 上下文（解决 403 问题）
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userInfo.getUsername(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userInfo.getRole()))
                    );
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("用户认证成功: userId={}, username={}", userInfo.getId(), userInfo.getUsername());
            }
        }

        // 对于permitAll路径，如果没有Authentication，设置匿名身份避免403
        if (SecurityContextHolder.getContext().getAuthentication() == null && isPermitAllPath(request)) {
            UsernamePasswordAuthenticationToken anonymousAuth =
                new UsernamePasswordAuthenticationToken(
                    "anonymous",
                    null,
                    Collections.emptyList()
                );
            SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
            log.debug("permitAll路径设置匿名身份: {}", request.getRequestURI());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 只清空非permitAll路径的上下文，避免影响登录接口
            if (!isPermitAllPath(request)) {
                UserContext.clear();
                SecurityContextHolder.clearContext();
            }
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/auth/login") ||
               path.equals("/auth/login") ||
               path.startsWith("/api/health") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs");
    }

    /**
     * 判断是否为permitAll路径
     */
    private boolean isPermitAllPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/auth/login") ||
               path.equals("/api/auth/logout") ||
               path.equals("/auth/login") ||
               path.startsWith("/api/health") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs");
    }
}