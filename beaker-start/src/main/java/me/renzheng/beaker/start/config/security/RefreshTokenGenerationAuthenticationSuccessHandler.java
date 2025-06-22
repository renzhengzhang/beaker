package me.renzheng.beaker.start.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.renzheng.beaker.biz.auth.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 处理用户登录成功后生成 Refresh Token 的逻辑
 * <p>
 * 在用户通过表单成功登录后，为其生成一个 Refresh Token，并将其安全地存储在 HttpOnly Cookie 中。
 * 后续可通过该 Refresh Token 获取 Access Token，为 Session 认证和 JWT 身份认证提供桥接。
 *
 * @author Renzheng Zhang
 * @since 2024-07-30
 */
@Component
public class RefreshTokenGenerationAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private JwtTokenService jwtTokenService;

    @Autowired
    public void setJwtTokenService(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 1. 获取认证成功的用户详情
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // 2. 生成 Refresh Token
        String refreshToken = jwtTokenService.generateRefreshToken(userDetails);
        // 3. 将 Refresh Token 设置到 HttpOnly Cookie 中
        setRefreshTokenCookie(response, refreshToken);
        // 4. 调用父类方法，处理重定向逻辑
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        // TODO 在生产环境中，应该设置为 true，以确保 Cookie 只在 HTTPS 中传输
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(jwtTokenService.getRefreshTokenExpirationInSeconds());
        response.addCookie(refreshTokenCookie);
    }
}