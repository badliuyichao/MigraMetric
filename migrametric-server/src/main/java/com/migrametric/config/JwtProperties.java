package com.migrametric.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性
 *
 * @author MigraMetric Team
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT密钥
     */
    private String secret;

    /**
     * Token有效期（毫秒）
     */
    private Long expireTime;

    /**
     * 刷新Token有效期（毫秒）
     */
    private Long refreshExpireTime;
}
