package me.renzheng.beaker.start.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.renzheng.beaker.biz.auth.JwtTokenService;
import me.renzheng.beaker.biz.auth.dto.AuthResponseDTO;
import me.renzheng.beaker.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器，提供 JWT 登录和刷新令牌接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    /**
     * 用户名和密码登录接口
     * <p>
     * 使用用户名和密码进行登录，成功后返回 Access Token 和 Refresh Token
     *
     * @param username 用户名
     * @param password 密码
     * @param response HttpServletResponse，用于设置 Refresh Token Cookie
     * @return 认证成功返回 Access Token 和 Refresh Token，失败返回错误信息
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam(name = "username") String username,
                                   @RequestParam(name = "password") String password,
                                   HttpServletResponse response) {
        try {
            // 验证用户名密码
            Authentication token = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(token);

            // 确保认证成功
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new BusinessException("认证失败");
            }

            // 获取用户详情
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 生成 JWT 和刷新令牌
            final String accessToken = jwtTokenService.generateAccessToken(userDetails);
            final String refreshToken = jwtTokenService.generateRefreshToken(userDetails);

            // 设置 Refresh Token 为 HttpOnly Cookie
            setRefreshTokenCookie(response, refreshToken);

            // 返回认证响应 DTO (只包含 Access Token)
            AuthResponseDTO authResponseDTO = AuthResponseDTO.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenService.getAccessTokenExpirationInSeconds())
                    .username(userDetails.getUsername())
                    .build();

            return ResponseEntity.ok(authResponseDTO);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * 刷新 Jwt Token 接口
     * <p>
     * 使用 Refresh Token 获取新的 Access Token 和 Refresh Token
     *
     * @param refreshToken Refresh Token，从 Cookie 中获取
     * @param response     HttpServletResponse，用于设置新的 Refresh Token Cookie
     * @return 刷新成功返回新的 Access Token 和 Refresh Token，失败返回错误信息
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refresh_token", required = false) String refreshToken,
                                          HttpServletResponse response) {
        try {
            if (refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("缺少刷新令牌");
            }

            // 验证令牌并获取用户名
            String username = jwtTokenService.validateTokenAndGetUsername(refreshToken);

            // 检查令牌是否是刷新令牌类型
            if (!jwtTokenService.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的刷新令牌：不是刷新令牌类型");
            }

            // 验证令牌是否过期
            if (jwtTokenService.isTokenExpired(refreshToken)) {
                // 清除过期的 Cookie
                clearRefreshTokenCookie(response);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("刷新令牌已过期");
            }

            // 加载用户详情
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 生成新的 Access Token 和 Refresh Token（Token轮换策略）
            String newAccessToken = jwtTokenService.generateAccessToken(userDetails);
            String newRefreshToken = jwtTokenService.generateRefreshToken(userDetails);

            // 更新 Refresh Token Cookie
            setRefreshTokenCookie(response, newRefreshToken);

            // TODO 还需要通过持久化机制吊销旧的 Refresh Token

            // 返回认证响应 DTO
            AuthResponseDTO authResponseDTO = AuthResponseDTO.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenService.getAccessTokenExpirationInSeconds())
                    .username(username)
                    .build();

            return ResponseEntity.ok(authResponseDTO);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的刷新令牌: " + e.getMessage());
        }
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); // 生产环境应设置为 true (HTTPS)
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(jwtTokenService.getRefreshTokenExpirationInSeconds());
        response.addCookie(refreshTokenCookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie expiredCookie = new Cookie("refresh_token", "");
        expiredCookie.setHttpOnly(true);
        expiredCookie.setPath("/");
        expiredCookie.setMaxAge(0);
        response.addCookie(expiredCookie);
    }
}
