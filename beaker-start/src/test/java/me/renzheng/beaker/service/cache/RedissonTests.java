package me.renzheng.beaker.service.cache;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.renzheng.beaker.common.enums.Gender;
import me.renzheng.beaker.service.auth.bo.UserBO;
import me.renzheng.beaker.start.AbstractTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
public class RedissonTests extends AbstractTests {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void whenSetAndGetUser() {
        RBucket<UserBO> bucket = redissonClient.getBucket("user:100000");
        UserBO user = mockUser();
        bucket.set(user);
        UserBO cachedUser = bucket.get();

        Assertions.assertEquals(user.getUsername(), cachedUser.getUsername());
        Assertions.assertEquals(user.getGender(), cachedUser.getGender());
    }

    private UserBO mockUser() {
        UserBO user = new UserBO();
        user.setUsername(UUID.randomUUID().toString());
        user.setGender(Gender.MALE);
        user.setBirthday(LocalDate.of(1999, 1, 1));
        user.setPhoneNumber("12345678901");
        user.setEmail("renzheng@gmail.com");
        user.setBanned(false);
        return user;
    }
}
