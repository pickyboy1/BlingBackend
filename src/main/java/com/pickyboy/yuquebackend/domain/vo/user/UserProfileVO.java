package com.pickyboy.yuquebackend.domain.vo.user;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 用户个人信息VO
 * 用于返回当前登录用户的个人信息，不包含敏感数据
 *
 * @author pickyboy
 */
@Data
public class UserProfileVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

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

    /**
     * 粉丝数
     */
    private Integer followerCount;

    /**
     * 关注数
     */
    private Integer followedCount;

    /**
     * 上次登陆时间
     */
    private LocalDateTime lastLogin;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}