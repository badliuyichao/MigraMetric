package com.migrametric.service.auth;

import com.migrametric.config.JwtProperties;
import com.migrametric.context.UserContext;
import com.migrametric.entity.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpireTime());

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("name", user.getName())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey())
                .compact();
    }

    public UserContext.UserInfo parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UserContext.UserInfo userInfo = new UserContext.UserInfo();
            userInfo.setId(Long.parseLong(claims.getSubject()));
            userInfo.setUsername(claims.get("username", String.class));
            userInfo.setName(claims.get("name", String.class));
            userInfo.setRole(claims.get("role", String.class));

            return userInfo;
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            return null;
        } catch (JwtException e) {
            log.warn("Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        UserContext.UserInfo userInfo = parseToken(token);
        return userInfo != null ? userInfo.getId() : null;
    }
}