package me.renzheng.beaker.biz.user;

/**
 * 用户相关业务接口
 *
 * @author Renzheng Zhang
 * @since 2024/5/28
 */
public interface UserBiz {


    /**
     * 使用用户名密码注册
     *
     * @param username 用户名
     * @param passwd   密码
     */
    void registerByUsername(String username, String passwd);

    /**
     * 使用手机号密码注册
     *
     * @param phoneNumber 手机号
     * @param passwd      密码
     */
    void registerByPhoneNumber(String phoneNumber, String passwd);

    /**
     * 使用邮箱密码注册
     *
     * @param email  邮箱
     * @param passwd 密码
     */
    void registerByEmail(String email, String passwd);

    /**
     * 封禁用户
     *
     * @param username 用户名
     */
    void banByUsername(String username);

    /**
     * 封禁用户
     *
     * @param userId 用户id
     */
    void banById(Long userId);
}
