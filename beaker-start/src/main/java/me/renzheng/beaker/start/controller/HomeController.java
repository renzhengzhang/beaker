package me.renzheng.beaker.start.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * HomeController
 *
 * @author Renzheng Zhang
 * @since 2024/4/28
 */
@Slf4j
@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String index() {
        log.info("访问 home");
        return "home";
    }

}
