package me.renzheng.beaker.start.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * LoginController
 *
 * @author Renzheng Zhang
 * @since 2024/5/30
 */
@Controller
@RequestMapping("/")
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

}
