package me.renzheng.beaker.biz.user.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.renzheng.beaker.biz.user.UserBiz;
import me.renzheng.beaker.dao.bo.UserBO;
import me.renzheng.beaker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Objects;

/**
 * UserBizImpl
 *
 * @author Renzheng Zhang
 * @since 2024/6/1
 */
@Slf4j
@AllArgsConstructor(onConstructor_ = @Autowired)
public class UserBizImpl implements UserBiz, UserDetailsService {

    private final UserService userService;

    @Override
    public void registerByUsername(String username, String passwd) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerByPhoneNumber(String phoneNumber, String passwd) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerByEmail(String email, String passwd) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void banByUsername(String username) {
        UserBO user = userService.selectByUsername(username);
        if (Objects.isNull(user)) {
            if (log.isInfoEnabled()) {
                log.info("user not found. username: {}", username);
            }
            throw new UsernameNotFoundException("用户不存在");
        }

        if (user.getBanned()) {
            if (log.isInfoEnabled()) {
                log.info("user has been baned. username: {}", username);
            }
            return;
        }

        UserBO userBO = new UserBO();
        userBO.setId(user.getId());
        userBO.setBanned(true);
        userService.update(userBO);
    }

    @Override
    public void banById(Long userId) {
        UserBO user = userService.select(userId);
        if (Objects.isNull(user)) {
            if (log.isInfoEnabled()) {
                log.info("user not found. userId: {}", userId);
            }
            throw new UsernameNotFoundException("用户不存在");
        }

        if (user.getBanned()) {
            if (log.isInfoEnabled()) {
                log.info("user has been baned. userId: {}", userId);
            }
            return;
        }

        UserBO userBO = new UserBO();
        userBO.setId(userId);
        userBO.setBanned(true);
        userService.update(userBO);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserBO user = userService.selectByUsername(username);

        return new User(
                user.getUsername(),         // username
                user.getPasswd(),           // password
                !user.getBanned(),          // enabled
                true,                       // accountNonExpired
                true,                       // credentialsNonExpired
                !user.getBanned(),          // accountNonLocked
                Collections.emptyList());   // TODO authorities
    }
}
