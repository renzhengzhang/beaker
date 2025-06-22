package me.renzheng.beaker.start.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 登录控制器 - 专门处理表单登录页面
 *
 * @author Renzheng Zhang
 * @since 2024/5/30
 */
@Slf4j
@Controller
@RequestMapping("/")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoginController {

    /**
     * 表单登录页面
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        log.info("访问登录页面，error={}, logout={}", error, logout);

        if (error != null) {
            model.addAttribute("error", true);
        }
        if (logout != null) {
            model.addAttribute("logout", true);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        model.addAttribute("currentTime", LocalDateTime.now().format(formatter));

        return "login";
    }
}
