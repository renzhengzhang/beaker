package me.renzheng.beaker.biz.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JWT 认证响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌 (仅在登录时返回)
     */
    private String refreshToken;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";

    /**
     * 访问令牌有效期（秒）
     */
    private long expiresIn;

    /**
     * 用户名
     */
    private String username;
}
