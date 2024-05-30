package me.renzheng.beaker.start.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * AdminController
 *
 * @author Renzheng Zhang
 * @since 2024/5/30
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String index() {
        return "admin";
    }

}
