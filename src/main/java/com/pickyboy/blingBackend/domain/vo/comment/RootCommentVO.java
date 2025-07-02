package com.pickyboy.blingBackend.domain.vo.comment;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 根评论VO
 * 用于文章的根级评论显示
 *
 * @author pickyboy
 */
@Data
public class RootCommentVO {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 回复数量
     */
    private Integer replyCount;

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
     * 评论状态
     */
    private Integer status;
}