package me.renzheng.beaker.service.auth;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.renzheng.beaker.service.auth.bo.UserBO;
import me.renzheng.beaker.start.AbstractTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

/**
 * UserServiceTests
 *
 * @author Renzheng Zhang
 * @since 2024/4/28
 */
@Slf4j
public class UserServiceTests extends AbstractTests {

    @Resource
    private UserService userService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @DisplayName("测试插入用户")
    @Order(1)
    @Disabled
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
        userInDb = userService.selectByUsername(mockUser.getUsername());
        userInDb = userService.selectByUsername(mockUser.getUsername());
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
