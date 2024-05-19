package me.renzheng.beaker.service.impl;

import jakarta.annotation.Resource;
import me.renzheng.beaker.common.enums.Gender;
import me.renzheng.beaker.dao.bo.UserBO;
import me.renzheng.beaker.service.UserService;
import me.renzheng.beaker.start.Application;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

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
    public void test() {
        UserBO user = new UserBO();
        user.setUsername("admin");
        user.setPasswd("passwd");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setEmail("beaker@renzheng.me");
        user.setGender(Gender.UNKNOWN);
        user.setBaned(false);
        userService.insert(user);
    }
}
