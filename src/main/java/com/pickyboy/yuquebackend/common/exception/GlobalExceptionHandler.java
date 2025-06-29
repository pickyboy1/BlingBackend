package com.pickyboy.yuquebackend.common.exception;


import com.pickyboy.yuquebackend.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获并处理自定义的业务异常
     * @param e BusinessException
     * @return Result
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 通常业务异常返回400状态码
    public Result<?> businessExceptionHandler(BusinessException e) {
        log.warn("BusinessException: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 捕获并处理其他所有未被处理的异常
     * @param e Exception
     * @return Result
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 未知系统异常返回500状态码
    public Result<?> exceptionHandler(Exception e) {
        log.error("RuntimeException: ", e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统开小差啦，请稍后重试");
    }
}
