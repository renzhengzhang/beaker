package me.renzheng.beaker.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.renzheng.beaker.dao.bo.UserBO;
import me.renzheng.beaker.service.UserService;
import me.renzheng.beaker.start.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

/**
 * UserServiceTests
 *
 * @author Renzheng Zhang
 * @since 2024/4/28
 */
@Slf4j
@SpringBootTest(classes = Application.class)
public class UserServiceTests {

    @Resource
    private UserService userService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @DisplayName("测试插入用户")
    @Order(1)
    @Test
    public void whenRegister_thenSuccess() {
        UserBO newUser = mockNewUser();
        UserBO existingUser = userService.selectByUsername(newUser.getUsername());
        if (Objects.nonNull(existingUser)) {
            if (log.isInfoEnabled()) {
                log.info("User already exists: {}", newUser.getUsername());
            }
            return;
        }

        userService.insert(newUser);
    }

    @DisplayName("测试查询用户")
    @Order(2)
    @Test
    public void whenQueryDb_thenSuccess() {
        UserBO mockUser = mockNewUser();
        UserBO userInDb = userService.selectByUsername(mockUser.getUsername());
        Assertions.assertNotNull(userInDb, "failed to query user from db.");
        Assertions.assertEquals(mockUser.getUsername(), userInDb.getUsername(), "username not match.");
    }

    private UserBO mockNewUser() {
        UserBO user = new UserBO();
        user.setUsername("user");
        user.setPasswd(passwordEncoder.encode("password"));
        user.setBanned(false);
        return user;
    }
}
