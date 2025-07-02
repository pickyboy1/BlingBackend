package com.pickyboy.yuquebackend.domain.vo.user;

import java.util.List;

import com.pickyboy.yuquebackend.domain.entity.KnowledgeBases;

import lombok.Data;

/**
 * 用户主页信息VO
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

    /**
     * 知识库列表
     */
    private List<KnowledgeBases> knowledgeBases;

    /**
     * 是否已关注
     */
    private Boolean isFollowed;
}