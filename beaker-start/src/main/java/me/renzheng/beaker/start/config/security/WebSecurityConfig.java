package me.renzheng.beaker.start.config.security;

import me.renzheng.beaker.biz.auth.config.AppSecurityProperties;
import me.renzheng.beaker.biz.user.impl.UserBizImpl;
import me.renzheng.beaker.service.auth.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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

    /**
     * 公开访问的 Web URL 数组。不需要认证即可访问，通常包括登录页面、静态资源等
     */
    private static final String[] PERMIT_ALL_URLS = {
            // 主页
            "/",
            // 登录页面
            "/login",
            // 登出接口
            "/logout",
            // 错误页面
            "/error",
            // Favicon 图标
            "/favicon.ico",
            // well-known URL，用于暴露公共元数据，例如密码管理器的自动配置
            "/.well-known/**",
            // 静态资源
            "/static/**",
            "/js/**",
            "/css/**",
            "/images/**",
            // 测试页面
            "/test/**",
            // 健康检查端点
            "/actuator/health",
            // 信息端点
            "/actuator/info",
    };

    /**
     * 公开访问的 API URL 数组。不需要认证即可访问，通常包括登录、公共接口等
     */
    private static final String[] API_PERMIT_ALL_URLS = {
            // JWT 登录接口
            "/api/auth/login",
            // Refresh Token 接口
            "/api/auth/refresh",
            // 通用接口
            "/api/common/**",
            // 公开接口
            "/api/public/**"
    };

    /**
     * 登出时需要清除的 Cookies 名称列表
     */
    private static final String[] COOKIE_NAMES_TO_CLEAR_WHEN_LOGOUT = {
            "JSESSIONID",
            "refresh_token"
    };

    private final AppSecurityProperties securityProperties;
    private final JwtDecoder jwtDecoder;
    private final RefreshTokenGenerationAuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    public WebSecurityConfig(AppSecurityProperties securityProperties,
                             JwtDecoder jwtDecoder,
                             RefreshTokenGenerationAuthenticationSuccessHandler authenticationSuccessHandler) {
        this.securityProperties = securityProperties;
        this.jwtDecoder = jwtDecoder;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    /**
     * Web SecurityFilterChain
     */
    @Bean
    @Order(1)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 排除 /api/** 路径
                .securityMatcher(request -> !isApiRequest(request.getRequestURI()))
                .authorizeHttpRequests((authorize) -> authorize
                        // 公开访问的 URL
                        .requestMatchers(PERMIT_ALL_URLS).permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated())
                .formLogin(formLogin -> formLogin
                        // 表单登录页面 URL
                        .loginPage("/login")
                        // 登录成功处理器，用于桥接 form-login 和 JWT
                        .successHandler(authenticationSuccessHandler)
                        )
                .logout(logout -> logout
                        // 登出处理URL
                        .logoutUrl("/logout")
                        // 登出成功后跳转的 URL
                        .logoutSuccessUrl("/login?logout")
                        // 清除 HTTP Session
                        .invalidateHttpSession(true)
                        // TODO 添加 LogoutHandler 吊销当前设备的 Refresh Token
                        // 清除指定的 Cookies
                        .deleteCookies(COOKIE_NAMES_TO_CLEAR_WHEN_LOGOUT)
                        // Authentication
                        .clearAuthentication(true));

        return http.build();
    }

    /**
     * API SecurityFilterChain
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
                        .jwt(jwt -> jwt.decoder(jwtDecoder)))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
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

    private boolean isApiRequest(String requestUri) {
        return requestUri.startsWith("/api/");
    }
}
