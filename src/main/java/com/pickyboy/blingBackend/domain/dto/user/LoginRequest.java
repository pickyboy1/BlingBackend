package com.pickyboy.blingBackend.domain.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户登录请求DTO
 *
 * @author pickyboy
 */
@Data
public class LoginRequest {

    /**
     * 登录类型: 1-用户名密码登录，2-手机号登录
     */
    @NotNull(message = "登录类型不能为空")
    private Integer loginType;

    /**
     * 登录标识符（用户名或手机号）
     */
    @NotBlank(message = "登录标识符不能为空")
    private String identifier;

    /**
     * 登录凭证（密码或验证码）
     */
    @NotBlank(message = "登录凭证不能为空")
    private String credential;
}