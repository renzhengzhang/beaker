package me.renzheng.beaker.start.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * HomePageController
 *
 * @author Renzheng Zhang
 * @since 2024/4/28
 */
@Controller
@RequestMapping("/")
public class HomePageController {

    @GetMapping
    public String index() {
        return "index";
    }

}
