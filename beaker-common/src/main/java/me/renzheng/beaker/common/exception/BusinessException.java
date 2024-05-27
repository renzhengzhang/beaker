package me.renzheng.beaker.common.exception;

/**
 * 业务异常
 *
 * @author Renzheng Zhang
 * @since 2024/5/28
 */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
