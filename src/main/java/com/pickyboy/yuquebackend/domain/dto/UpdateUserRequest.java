package com.pickyboy.yuquebackend.domain.dto;

import lombok.Data;

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
    private String nickname;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 简介
     */
    private String description;

    /**
     * 地址
     */
    private String location;

    /**
     * 行业领域
     */
    private String field;
}