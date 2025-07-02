package com.pickyboy.yuquebackend.domain.vo.comment;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 子评论VO
 * 用于文章的子级评论显示（回复评论）
 *
 * @author pickyboy
 */
@Data
public class SubCommentVO {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 评论者用户ID
     */
    private Long userId;

    /**
     * 评论者昵称
     */
    private String nickname;

    /**
     * 评论者头像URL
     */
    private String avatarUrl;

    /**
     * 被回复的评论ID
     */
    private Long replyToCommentId;

    /**
     * 被回复的用户ID
     */
    private Long replyToUserId;

    /**
     * 被回复的用户昵称
     */
    private String replyToUserNickname;

    /**
     * 评论状态
     */
    private Integer status;
}