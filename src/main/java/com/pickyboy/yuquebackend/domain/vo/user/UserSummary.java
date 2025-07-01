package com.pickyboy.yuquebackend.domain.vo.user;

import lombok.Data;

/**
 * 用户摘要信息VO
 * 用于在列表中展示的用户信息
 *
 * @author pickyboy
 */
@Data
public class UserSummary {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 简介
     */
    private String description;

    /**
     * 当前登录用户是否已关注该用户
     */
    private Boolean isFollowing;
}