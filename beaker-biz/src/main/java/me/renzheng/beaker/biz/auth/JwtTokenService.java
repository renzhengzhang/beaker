package me.renzheng.beaker.biz.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;

/**
 * Jwt Token 服务接口，定义 Jwt Token 生成和校验的操作
 */
public interface JwtTokenService {

    /**
     * 生成 Jwt Access Token
     *
     * @param userDetails UserDetails
     * @return Jwt Access Token
     */
    String generateAccessToken(UserDetails userDetails);

    /**
     * 生成 Refresh Token
     *
     * @param userDetails UserDetails
     * @return JWT Refresh Token
     */
    String generateRefreshToken(UserDetails userDetails);

    /**
     * 验证 Jwt Token 签名并返回用户名
     *
     * @param token Jwt Token
     * @return 令牌中的用户名
     * @throws JwtException 如果令牌无效
     */
    String validateTokenAndGetUsername(String token);

    /**
     * 验证 Token 对应的用户
     *
     * @param token       Jwt Token
     * @param userDetails UserDetails
     * @return 如果 Token 对应的用户名匹配则返回true
     */
    boolean validateToken(String token, UserDetails userDetails);

    /**
     * 检查 Token 是否过期
     *
     * @param token Jwt Token
     * @return 如果令牌已过期返回true
     */
    boolean isTokenExpired(String token);

    /**
     * 检查 Token 是否为 Refresh Token
     *
     * @param token Jwt Token
     * @return 如果是 Refresh Token 返回 true
     * @throws JwtException 如果 Token 无效
     */
    boolean isRefreshToken(String token);

    /**
     * 获取 Access Token 有效期（秒）
     *
     * @return Access Token 有效期（秒）
     */
    long getAccessTokenExpirationInSeconds();
}
