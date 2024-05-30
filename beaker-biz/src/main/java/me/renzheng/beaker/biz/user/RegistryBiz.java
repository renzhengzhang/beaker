package me.renzheng.beaker.biz.user;

/**
 * 注册相关业务接口
 *
 * @author Renzheng Zhang
 * @since 2024/5/28
 */
public interface RegistryBiz {

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
     * @param phoneNumber      手机号
     * @param passwd           密码
     * @param verificationCode 验证码
     */
    void registerByPhoneNumber(String phoneNumber, String passwd, String verificationCode);

    /**
     * 使用邮箱密码注册
     *
     * @param email            邮箱
     * @param passwd           密码
     * @param verificationCode 验证码
     */
    void registerByEmail(String email, String passwd, String verificationCode);
}
