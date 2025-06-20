package me.renzheng.beaker.biz.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JWT 安全相关配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    /**
     * JWT 相关配置
     */
    private Jwt jwt = new Jwt();

    /**
     * CORS 相关配置
     */
    private Cors cors = new Cors();

    /**
     * JWT 配置
     */
    @Data
    public static class Jwt {
        /**
         * JWT 密钥
         */
        private String secret = "ZXd6cXF3ZXJxZ2Jhc2RleXV0Ym5leDEyMzQ1NjduaWNlc2VjcmV0a2V5";

        /**
         * 访问令牌过期时间（秒）
         */
        private long accessTokenExpiration = 3600;

        /**
         * 刷新令牌过期时间（秒）
         */
        private long refreshTokenExpiration = 2592000;
    }

    /**
     * CORS 配置
     */
    @Data
    public static class Cors {

        /**
         * 允许的源
         */
        private List<String> allowedOrigins = List.of("http://localhost:8080", "http://localhost:3000");

        /**
         * 允许的方法
         */
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

        /**
         * 允许的头
         */
        private List<String> allowedHeaders = List.of("Authorization", "Content-Type", "X-Requested-With");

        /**
         * 暴露的头
         */
        private List<String> exposedHeaders = List.of("Authorization");

        /**
         * 是否允许凭证
         */
        private boolean allowCredentials = true;

        /**
         * 预检请求的有效期（秒）
         */
        private long maxAge = 3600;
    }
}
