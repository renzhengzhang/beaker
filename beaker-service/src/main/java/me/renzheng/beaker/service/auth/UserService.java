package me.renzheng.beaker.service.auth;

import me.renzheng.beaker.service.auth.bo.UserBO;
import me.renzheng.beaker.service.common.EntityService;

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
