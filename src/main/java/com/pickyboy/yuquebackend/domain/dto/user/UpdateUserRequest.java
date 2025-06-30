package com.pickyboy.yuquebackend.domain.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * 更新用户信息请求DTO
 *
 * @author pickyboy
 */
@Data
public class UpdateUserRequest {

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    /**
     * 头像地址
     */
    @URL(message = "头像地址格式不正确")
    private String avatarUrl;

    /**
     * 简介
     */
    @Size(max = 200, message = "简介长度不能超过200个字符")
    private String description;

    /**
     * 地址
     */
    @Size(max = 100, message = "地址长度不能超过100个字符")
    private String location;

    /**
     * 行业领域
     */
    @Size(max = 50, message = "行业领域长度不能超过50个字符")
    private String field;
}