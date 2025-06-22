package me.renzheng.beaker.start.config.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import me.renzheng.beaker.biz.auth.config.AppSecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * JwtConfig
 *
 * @author Renzheng Zhang <a href="mailto:git@renzheng.me">git@renzheng.me</a>
 */
@Configuration
public class JwtEnDecoderConfig {

    /**
     * JWT 编码器
     */
    @Bean
    @Autowired
    public JwtEncoder jwtEncoder(AppSecurityProperties securityProperties) {
        byte[] keyBytes = securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        ImmutableSecret<SecurityContext> secret = new ImmutableSecret<>(secretKey);
        return new NimbusJwtEncoder(secret);
    }

    /**
     * JWT 解码器
     */
    @Bean
    @Autowired
    public JwtDecoder jwtDecoder(AppSecurityProperties securityProperties) {
        byte[] keyBytes = securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }



}
