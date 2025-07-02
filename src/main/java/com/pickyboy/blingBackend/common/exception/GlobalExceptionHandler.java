package com.pickyboy.blingBackend.common.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.pickyboy.blingBackend.common.response.Result;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

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
     * 处理@Valid校验失败异常（用于@RequestBody）
     * @param e MethodArgumentNotValidException
     * @return Result
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("参数校验失败: {}", errorMessage);
        return Result.error(ErrorCode.PARAMS_ERROR.getCode(), "参数校验失败: " + errorMessage);
    }

    /**
     * 处理@Valid校验失败异常（用于表单提交）
     * @param e BindException
     * @return Result
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> bindExceptionHandler(BindException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("参数绑定失败: {}", errorMessage);
        return Result.error(ErrorCode.PARAMS_ERROR.getCode(), "参数绑定失败: " + errorMessage);
    }

    /**
     * 处理单个参数校验失败异常（用于@RequestParam、@PathVariable等）
     * @param e ConstraintViolationException
     * @return Result
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> constraintViolationExceptionHandler(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        log.warn("约束校验失败: {}", errorMessage);
        return Result.error(ErrorCode.PARAMS_ERROR.getCode(), "参数校验失败: " + errorMessage);
    }

    /**
     * 捕获并处理其他所有未被处理的异常
     * @param e Exception
     * @return Result
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 未知系统异常返回500状态码
    public Result<?> exceptionHandler(Exception e) {
        log.error("UnexpectedException: ", e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统开小差啦，请稍后重试");
    }
}
