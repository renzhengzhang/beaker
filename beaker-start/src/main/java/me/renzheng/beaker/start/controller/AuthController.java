package me.renzheng.beaker.start.controller;

import lombok.RequiredArgsConstructor;
import me.renzheng.beaker.biz.auth.JwtTokenService;
import me.renzheng.beaker.biz.auth.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
     * 登录API，验证用户名密码并返回JWT和刷新令牌
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        try {
            // 验证用户名密码
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // 确保认证成功
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("认证失败");
            }

            // 获取用户详情
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 生成 JWT 和刷新令牌
            final String accessToken = jwtTokenService.generateAccessToken(userDetails);
            final String refreshToken = jwtTokenService.generateRefreshToken(userDetails);

            // 返回认证响应 DTO
            AuthResponse response = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenService.getAccessTokenExpirationInSeconds())
                    .username(userDetails.getUsername())
                    .build();

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("登录过程中发生错误: " + e.getMessage());
        }
    }

    /**
     * 刷新令牌API，验证刷新令牌并返回新的访问令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestParam String refreshToken) {
        try {
            // 验证令牌并获取用户名
            String username = jwtTokenService.validateTokenAndGetUsername(refreshToken);

            // 检查令牌是否是刷新令牌类型
            if (!jwtTokenService.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的刷新令牌：不是刷新令牌类型");
            }

            // 验证令牌是否过期
            if (jwtTokenService.isTokenExpired(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("刷新令牌已过期");
            }

            // 加载用户详情
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 生成新的访问令牌
            String newAccessToken = jwtTokenService.generateAccessToken(userDetails);

            // 返回认证响应 DTO (不包含刷新令牌)
            AuthResponse response = AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenService.getAccessTokenExpirationInSeconds())
                    .username(username)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的刷新令牌: " + e.getMessage());
        }
    }
}
