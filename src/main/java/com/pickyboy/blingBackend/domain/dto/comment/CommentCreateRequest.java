package com.pickyboy.blingBackend.domain.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 评论创建请求DTO
 *
 * @author pickyboy
 */
@Data
public class CommentCreateRequest {

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;

    /**
     * 要回复的父评论ID
     * 若为直接评论文章，则不传此字段
     */
    private Long parentId;
}