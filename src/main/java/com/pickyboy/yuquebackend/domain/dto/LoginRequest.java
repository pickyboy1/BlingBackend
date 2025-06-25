package com.pickyboy.yuquebackend.domain.dto;

import lombok.Data;

/**
 * 用户登录请求DTO
 *
 * @author pickyboy
 */
@Data
public class LoginRequest {

    /**
     * 登录类型: 1: 用户名密码登录 2: 手机号登录
     */
    private Integer loginType;

    /*
    * 登录标识
    * */
    private String identifier;

    /**
     * 登录凭证
     */
    private String credential;

}