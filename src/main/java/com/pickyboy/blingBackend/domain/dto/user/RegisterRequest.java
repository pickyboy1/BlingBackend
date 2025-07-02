package com.pickyboy.blingBackend.domain.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求DTO
 *
 * @author pickyboy
 */
@Data
public class RegisterRequest {

    /**
     * 注册类型: 1-用户名密码注册，2-手机号注册
     */
    @NotNull(message = "注册类型不能为空")
    private Integer registerType;

    /**
     * 注册标识符（用户名或手机号）
     */
    @NotBlank(message = "注册标识符不能为空")
    @Size(max = 50, message = "注册标识符长度不能超过50个字符")
    private String identifier;

    /**
     * 密码 (当registerType为1时必需)
     */
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /**
     * 短信验证码 (当registerType为2时必需)
     */
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为6位数字")
    private String verificationCode;
}