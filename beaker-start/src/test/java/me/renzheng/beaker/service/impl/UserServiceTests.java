package me.renzheng.beaker.service.impl;

import jakarta.annotation.Resource;
import me.renzheng.beaker.service.UserService;
import me.renzheng.beaker.start.Application;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * UserServiceTests
 *
 * @author Renzheng Zhang
 * @since 2024/4/28
 */
@SpringBootTest(classes = Application.class)
public class UserServiceTests {

    @Resource
    private UserService userService;

    @Test
    public void whenQueryDb_thenSuccess() {
        userService.selectByUsername("");
    }
}
