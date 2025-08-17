package me.renzheng.beaker.common.exception;

import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常
 *
 * @author Renzheng Zhang
 * @since 2024/5/28
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -3467532361883711996L;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

}
