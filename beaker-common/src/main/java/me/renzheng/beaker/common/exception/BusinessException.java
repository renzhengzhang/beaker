package me.renzheng.beaker.common.exception;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author Renzheng Zhang
 * @since 2024/5/28
 */
@Getter
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

}
