package me.renzheng.beaker.start.config;

import me.renzheng.beaker.start.filter.IdentityContextInitializationFilter;
import me.renzheng.beaker.start.filter.TraceIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置
 *
 * @author <a href="mailto:renzheng.zh@gmail.com">Renzheng Zhang</a>
 */
@Configuration
public class FilterConfig {

    /**
     * TraceId 过滤器
     */
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter());
        registration.setOrder(OrderedFilter.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }

    /**
     * 配置身份信息上下文初始化过滤器
     */
    @Bean
    public FilterRegistrationBean<IdentityContextInitializationFilter> identityContextInitializationFilter() {
        FilterRegistrationBean<IdentityContextInitializationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new IdentityContextInitializationFilter());
        // 确保在 Spring Security 的 FilterChainProxy 之后执行
        registration.setOrder(OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER - 50);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
