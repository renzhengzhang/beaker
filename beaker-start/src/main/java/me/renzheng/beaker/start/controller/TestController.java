package me.renzheng.beaker.start.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 测试 Controller
 *
 * @author Renzheng Zhang
 * @since 2025/6/21
 */
@Slf4j
@Controller
@RequestMapping("/test/")
public class TestController {

    /**
     * JWT Token 测试页面
     */
    @GetMapping("/jwt")
    public String jwt(Model model) {
        log.info("访问 Jwt Token 测试页面");
        // 添加页面标题和描述
        model.addAttribute("pageTitle", "JWT Token 测试工具");
        model.addAttribute("pageDescription", "Jwt Authentication 测试页，支持登录、刷新令牌、API调用等功能");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        model.addAttribute("currentTime", LocalDateTime.now().format(formatter));

        return "test/jwt";
    }

    /**
     * 图片上传测试页面
     */
    @GetMapping("/upload")
    public String uploadPage() {
        return "test/upload";
    }
}
