package me.renzheng.beaker.start.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import me.renzheng.beaker.common.context.IdentityContext;
import me.renzheng.beaker.common.context.IdentityContextHolder;
import me.renzheng.beaker.service.auth.bo.UserBO;
import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static me.renzheng.beaker.common.constants.LoggingConstants.MDC_USER_ID;
import static me.renzheng.beaker.common.constants.LoggingConstants.MDC_USERNAME;


/**
 * 身份信息上下文初始化过滤器
 *
 * @author Renzheng Zhang
 */
public class IdentityContextInitializationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            // 从 SecurityContextHolder 中获取身份信息
            IdentityContext identityContext = resolveIdentityContext();
            IdentityContextHolder.setIdentityContext(identityContext);
            // 将身份信息写入 MDC
            MDC.put(MDC_USER_ID, String.valueOf(identityContext.getId()));
            MDC.put(MDC_USERNAME, identityContext.getUsername());

            chain.doFilter(request, response);
        } finally {
            IdentityContextHolder.removeIdentityContext();
            MDC.remove(MDC_USER_ID);
            MDC.remove(MDC_USERNAME);
        }
    }

    private IdentityContext resolveIdentityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return IdentityContext.anonymous();
        }
        if (auth.getPrincipal() instanceof UserBO user) {
            return buildIdentityContextFrom(user);
        }
        return IdentityContext.anonymous();
    }

    private IdentityContext buildIdentityContextFrom(UserBO user) {
        return IdentityContext.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }
}
