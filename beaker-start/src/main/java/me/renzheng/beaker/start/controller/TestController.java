package me.renzheng.beaker.start.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试 Controller
 *
 * @author Renzheng Zhang
 * @since 2024/4/18
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping()
    public String get() {
        return "Hello, world!";
    }
}
