package com.pickyboy.blingBackend.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 通用错误码
    PARAMS_ERROR(400, "请求参数错误"),
    NOT_LOGIN_ERROR(401, "未登录"),
    NO_AUTH_ERROR(401, "无权限"),
    NOT_FOUND_ERROR(404, "请求数据不存在"),
    FORBIDDEN_ERROR(403, "禁止访问"),
    SYSTEM_ERROR(500, "系统内部异常"),

    // 用户相关错误码 (1000-1999)
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_ALREADY_EXISTS(1002, "用户名已存在"),
    PHONE_ALREADY_EXISTS(1003, "手机号已注册"),
    INVALID_PASSWORD(1004, "密码错误"),
    INVALID_LOGIN_TYPE(1005, "不支持的登录类型"),
    INVALID_REGISTER_TYPE(1006, "不支持的注册类型"),
    USER_DISABLED(1007, "用户已被禁用"),
    PASSWORD_TOO_WEAK(1008, "密码强度不够"),
    USER_ALREADY_FOLLOWED(1009, "用户已关注"),
    USER_ALREADY_UNFOLLOWED(1010, "用户未关注"),

    // 知识库相关错误码 (2000-2999)
    KNOWLEDGE_BASE_NOT_FOUND(2001, "知识库不存在"),
    KNOWLEDGE_BASE_ACCESS_DENIED(2002, "无权访问该知识库"),
    KNOWLEDGE_BASE_NAME_DUPLICATE(2003, "知识库名称已存在"),
    KNOWLEDGE_BASE_DELETE_FAILED(2004, "知识库删除失败"),

    // 资源/文档相关错误码 (3000-3999)
    RESOURCE_NOT_FOUND(3001, "资源不存在"),
    RESOURCE_ACCESS_DENIED(3002, "无权访问该资源"),
    RESOURCE_DELETE_FAILED(3003, "资源删除失败"),
    RESOURCE_MOVE_FAILED(3004, "资源移动失败"),
    RESOURCE_COPY_FAILED(3005, "资源复制失败"),
    INVALID_RESOURCE_TYPE(3006, "无效的资源类型"),
    INVALID_REQUEST(3007, "无效的请求参数"),
    RESOURCE_ALREADY_LIKED(3008, "资源已点赞"),
    RESOURCE_ALREADY_UNLIKED(3009, "资源未点赞"),

    // 文件上传相关错误码 (4000-4999)
    FILE_UPLOAD_FAILED(4001, "文件上传失败"),
    FILE_SIZE_EXCEEDED(4002, "文件大小超出限制"),
    INVALID_FILE_TYPE(4003, "不支持的文件类型"),
    INVALID_UPLOAD_TYPE(4004, "无效的上传类型"),
    UPLOAD_URL_GENERATE_FAILED(4005, "生成上传链接失败"),

    // 业务逻辑相关错误码 (5000-5999)
    OPERATION_NOT_ALLOWED(5001, "操作不被允许"),
    DATA_ALREADY_EXISTS(5002, "数据已存在"),
    DATA_CONFLICT(5003, "数据冲突"),
    INSUFFICIENT_PERMISSIONS(5004, "权限不足"),
    VERSION_RESTORE_FAILED(5005, "版本恢复失败"),

    // 标签及小记相关错误码 (6000-6999)
    TAG_NAME_DUPLICATE(6001,"标签名已存在"),

    // 评论相关错误码 (7000-7999)
    COMMENT_NOT_FOUND(7001, "评论不存在"),
    COMMENT_DELETE_FAILED(7002, "评论删除失败"),
    COMMENT_ALREADY_LIKED(7006, "评论已点赞"),
    COMMENT_ALREADY_UNLIKED(7007, "评论未点赞");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
