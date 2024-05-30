package me.renzheng.beaker.biz.user;

/**
 * 登录相关业务接口
 *
 * @author Renzheng Zhang
 * @since 2024/5/28
 */
public interface LoginBiz {


    /**
     * 使用用户名密码登录
     *
     * @param username 用户名
     * @param passwd   密码
     */
    void loginByUsername(String username, String passwd);

    /**
     * 使用手机号密码登录
     *
     * @param phoneNumber 手机号
     * @param passwd      密码
     */
    void loginByPhoneNumber(String phoneNumber, String passwd);

    /**
     * 使用邮箱密码登录
     *
     * @param email  邮箱
     * @param passwd 密码
     */
    void loginByEmail(String email, String passwd);

}
