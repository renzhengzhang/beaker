package me.renzheng.beaker.service;

import me.renzheng.beaker.dao.bo.UserBO;

/**
 * UserService
 *
 * @author Renzheng Zhang
 * @since 2024/4/28
 */
public interface UserService extends EntityService<Long, UserBO> {

    UserBO selectByPhoneNumber(String phoneNumber);

    UserBO selectByUsername(String username);

    UserBO selectByEmail(String email);
}
