package me.renzheng.beaker.start.advice;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 全局的 Controller Advice，用于向视图的 Model 中添加通用属性
 */
@ControllerAdvice
public class GlobalModelAttributeAdvice {

    /**
     * 将当前登录的用户名添加到 Model 中
     * <p>
     * 如果用户已通过会话认证，则返回其用户名；否则返回 null。
     * Thymeleaf 模板可以根据此属性是否存在来判断用户登录状态。
     *
     * @return 当前用户名，如果未登录则为 null
     */
    @ModelAttribute("username")
    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 检查用户是否已认证且不是匿名用户
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            // 为其他 Principal 类型（例如 OAuth2）提供备用方案
            return authentication.getName();
        }
    }
}