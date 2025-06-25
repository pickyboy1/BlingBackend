package com.pickyboy.yuquebackend.domain.dto;

import lombok.Data;

/**
 * 用户注册请求DTO
 *
 * @author pickyboy
 */
@Data
public class RegisterRequest {

    /**
     * 注册类型: '
     */
    private Integer registerType;

    /**

     */
    private String identifier;

    /**
     * 密码 (当type为'username'时必需)
     */
    private String password;


    /**
     * 短信验证码
     */
    private String verificationCode;
}