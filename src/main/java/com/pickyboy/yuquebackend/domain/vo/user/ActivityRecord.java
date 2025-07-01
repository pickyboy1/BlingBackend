package com.pickyboy.yuquebackend.domain.vo.user;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 用户活动记录VO
 * 用于浏览历史、点赞历史、评论历史等
 *
 * @author pickyboy
 */
@Data
public class ActivityRecord {

    /**
     * 文档的ID
     */
    private Long resourceId;

    /**
     * 文档的标题
     */
    private String resourceTitle;

    /**
     * 文档的类型 (e.g., markdown)
     */
    private String resourceType;

    /**
     * 所属知识库的ID
     */
    private Long kbId;

    /**
     * 所属知识库的名称
     */
    private String kbName;

    /**
     * 文档作者的用户ID
     */
    private Long authorId;

    /**
     * 文档作者的昵称
     */
    private String authorName;

    /**
     * 操作发生的时间
     */
    private LocalDateTime actionAt;
}