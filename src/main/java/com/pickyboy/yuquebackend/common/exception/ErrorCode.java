package com.pickyboy.yuquebackend.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 可以在这里定义各种业务错误码
    PARAMS_ERROR(400, "请求参数错误"),
    NOT_LOGIN_ERROR(401, "未登录"),
    NO_AUTH_ERROR(401, "无权限"),
    NOT_FOUND_ERROR(404, "请求数据不存在"),
    FORBIDDEN_ERROR(403, "禁止访问"),
    SYSTEM_ERROR(500, "系统内部异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
