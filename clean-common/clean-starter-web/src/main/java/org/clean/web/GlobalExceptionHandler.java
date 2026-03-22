package org.clean.web;

import feign.FeignException;
import feign.codec.DecodeException;
import lombok.extern.slf4j.Slf4j;
import org.clean.CleanException;
import org.clean.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(CleanException.class)
    public Result<String> cleanExceptionHandler(CleanException e) {
        log.error("业务异常: {} -> {}", Arrays.stream(e.getStackTrace()).findFirst().orElse(null), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }
    @ExceptionHandler(RuntimeException.class)
    public Result<String> runtimeExceptionHandler(CleanException e) {
        log.error("业务异常: {} -> {}", Arrays.stream(e.getStackTrace()).findFirst().orElse(null), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }


    /**
     * 拦截 DecodeException 异常，decoder 中抛出的自定义全局异常会进入此处
     */
    @ExceptionHandler(DecodeException.class)
    public Result<?> handleDecodeException(DecodeException e) {
        Throwable cause = e.getCause();
        if (cause instanceof CleanException) {
            CleanException cleanException = (CleanException) cause;
            // 上游符合全局响应包装约定的再次抛出即可
            log.error("远程调用错误: ", e);
            return Result.fail(cleanException.getCode(), cleanException.getMessage());
        }
        log.error("DecodeException: ", e);
        return Result.fail(4000, e.getMessage());
    }

}