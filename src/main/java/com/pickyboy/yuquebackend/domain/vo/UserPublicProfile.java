package com.pickyboy.yuquebackend.domain.vo;

import lombok.Data;

/**
 * 用户公开信息VO
 *
 * @author pickyboy
 */
@Data
public class UserPublicProfile {

    /**
     * 用户ID
     */
    private Long id;

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
     * 粉丝数
     */
    private Integer followerCount;

    /**
     * 关注数
     */
    private Integer followedCount;
}