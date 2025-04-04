package me.renzheng.beaker.start.aspect;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;
import java.util.Set;

/**
 * 全局异常处理器
 *
 * @author Renzheng Zhang
 */
@Slf4j
@RestControllerAdvice
public class ExceptionHandleRestControllerAdvice {

    private static final String INVALID_REQUEST_PARAMETER = "非法的请求参数";

    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String IllegalArgumentException(IllegalArgumentException e) {
        log.error("全局异常处理器捕获非法参数异常", e);
        return e.getMessage();
    }

    @ExceptionHandler(value = {
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidRequestArgumentException(Exception e) {
        log.error("全局异常处理器捕获非法请求参数异常", e);
        if (e instanceof MethodArgumentNotValidException nve) {
            return extractErrors(nve.getBindingResult());
        } else if (e instanceof BindException be) {
            return extractErrors(be.getBindingResult());
        } else if (e instanceof ConstraintViolationException cve) {
            return extractErrors(cve.getConstraintViolations());
        }
        return INVALID_REQUEST_PARAMETER;
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e) {
        log.error("全局异常处理器捕获全局异常", e);
        return HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
    }

    private String extractErrors(BindingResult bindingResult) {
        StringBuilder sb = new StringBuilder();
        Optional.ofNullable(bindingResult).map(BindingResult::getAllErrors).ifPresent(fieldErrors ->
                fieldErrors.forEach((error) -> sb.append(error.getObjectName())
                        .append(": ")
                        .append(error.getDefaultMessage())
                        .append("; "))
        );
        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private String extractErrors(Set<ConstraintViolation<?>> constraintViolations) {
        StringBuilder sb = new StringBuilder();
        Optional.ofNullable(constraintViolations).ifPresent(violations ->
                violations.forEach(violation -> sb.append(violation.getPropertyPath().toString())
                        .append(": ")
                        .append(violation.getMessage())
                        .append("; ")));

        return sb.toString();
    }
}
