package me.renzheng.beaker.start.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import me.renzheng.beaker.biz.auth.config.AppSecurityProperties;
import me.renzheng.beaker.biz.user.impl.UserBizImpl;
import me.renzheng.beaker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Spring Security 相关配置
 *
 * @author Renzheng Zhang
 * @since 2024/4/19
 */
@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class WebSecurityConfig {

    private static final String[] PERMIT_ALL_URLS = {
            "/",
            "/login",
            "/logout",          // 登出接口
            "/favicon.ico",     // Favicon 图标
            "/static/**",       // 静态资源
            "/test/**",         // 测试页面
            "/actuator/health", // 健康检查端点
            "/actuator/info",   // 信息端点
            "/error"            // 错误页面
    };

    private static final String[] API_PERMIT_ALL_URLS = {
            "/api/auth/login",      // 登录接口
            "/api/auth/refresh",    // Refresh Token 接口
            "/api/common/**",       // 通用接口
            "/api/public/**"        // 公开接口
    };

    private final AppSecurityProperties securityProperties;

    @Autowired
    public WebSecurityConfig(AppSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * Web 应用安全过滤链 - 用于表单登录（Thymeleaf 页面）
     */
    @Bean
    @Order(1)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(request -> !request.getRequestURI().startsWith("/api/")) // 明确排除 /api/** 路径
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(PERMIT_ALL_URLS).permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .formLogin((formLogin) -> formLogin
                        .loginPage("/login")
                        .permitAll())
                .logout((logout) -> logout
                        .logoutUrl("/logout") // 登出处理URL
                        .logoutSuccessUrl("/login?logout") // 登出成功后重定向 URL
                        .invalidateHttpSession(true) // 使 Session 失效
                        .deleteCookies("JSESSIONID") // 删除指定的 Cookies
                        .clearAuthentication(true) // 清除认证信息
                        .permitAll() // 允许所有用户访问登出URL
                );

        return http.build();
    }

    /**
     * API 安全过滤链 - 用于 JWT 认证
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(API_PERMIT_ALL_URLS).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder())))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    /**
     * JWT 解码器
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    /**
     * JWT 编码器
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        byte[] keyBytes = securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");
        ImmutableSecret<SecurityContext> secret = new ImmutableSecret<>(secretKey);
        return new NimbusJwtEncoder(secret);
    }

    /**
     * CORS 配置 - 更安全的配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(securityProperties.getCors().getAllowedOrigins());
        configuration.setAllowedMethods(securityProperties.getCors().getAllowedMethods());
        configuration.setAllowedHeaders(securityProperties.getCors().getAllowedHeaders());
        configuration.setExposedHeaders(securityProperties.getCors().getExposedHeaders());
        configuration.setAllowCredentials(securityProperties.getCors().isAllowCredentials());
        configuration.setMaxAge(securityProperties.getCors().getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setAuthoritiesMapper(new SimpleAuthorityMapper());
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    @Autowired
    public UserDetailsService userDetailsService(UserService userService) {
        return new UserBizImpl(userService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
