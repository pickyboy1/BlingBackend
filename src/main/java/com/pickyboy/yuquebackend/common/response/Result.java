package com.pickyboy.yuquebackend.common.response;

import java.io.Serializable;

import lombok.Data;

@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    // 状态码
    private Integer code;
    // 消息
    private String message;
    // 数据
    private T data;

    // 私有构造方法
    private Result() {}

    // 成功静态方法
    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // 失败静态方法
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}