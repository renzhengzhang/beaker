package me.renzheng.beaker.biz.auth.impl;

import lombok.RequiredArgsConstructor;
import me.renzheng.beaker.biz.auth.JwtTokenService;
import me.renzheng.beaker.biz.auth.config.AppSecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Jwt Token 服务实现类，用于生成和校验 Jwt Token
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final AppSecurityProperties securityProperties;

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(
                userDetails,
                securityProperties.getJwt().getAccessTokenExpiration(),
                "access"
        );
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(
                userDetails,
                securityProperties.getJwt().getRefreshTokenExpiration(),
                "refresh"
        );
    }

    /**
     * 生成 Jwt Token
     *
     * @param userDetails       UserDetails
     * @param expirationSeconds 过期时间（秒）
     * @param tokenType         Token 类型
     * @return Jwt Token
     */
    private String generateToken(UserDetails userDetails, long expirationSeconds, String tokenType) {
        Instant now = Instant.now();

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer("beaker")
                .issuedAt(now)
                .expiresAt(now.plus(expirationSeconds, ChronoUnit.SECONDS))
                .subject(userDetails.getUsername())
                .claim("type", tokenType);

        // 如果有权限信息，添加到令牌中
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        if (authorities != null && !authorities.isEmpty()) {
            String scopes = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(" "));
            claimsBuilder.claim("scope", scopes);
        }

        JwtClaimsSet claims = claimsBuilder.build();

        // 使用 HS256 算法创建 Jwt 头部，与 WebSecurityConfig 中配置的保持一致
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    @Override
    public String validateTokenAndGetUsername(String token) {
        try {
            var jwt = jwtDecoder.decode(token);
            return jwt.getSubject();
        } catch (JwtException e) {
            throw new JwtException("无效的 JWT Token: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = validateTokenAndGetUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            var jwt = jwtDecoder.decode(token);
            return Objects.requireNonNull(jwt.getExpiresAt()).isBefore(Instant.now());
        } catch (JwtException e) {
            return true;
        }
    }

    @Override
    public boolean isRefreshToken(String token) {
        try {
            // TODO 这里除了需要校验 Token 签名合法性之外，还需要通过持久化机制校验 Token 有没有被吊销
            var jwt = jwtDecoder.decode(token);
            return "refresh".equals(jwt.getClaim("type"));
        } catch (JwtException e) {
            throw new JwtException("无效的 Jwt Token: " + e.getMessage(), e);
        }
    }

    @Override
    public int getAccessTokenExpirationInSeconds() {
        return securityProperties.getJwt().getAccessTokenExpiration();
    }


    @Override
    public int getRefreshTokenExpirationInSeconds() {
        return securityProperties.getJwt().getRefreshTokenExpiration();
    }
}
