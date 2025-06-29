package com.pickyboy.yuquebackend.domain.vo.resource;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 公开分享文档VO
 *
 * @author pickyboy
 */
@Data
public class PublicResourceVO {

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 作者信息
     */
    private AuthorInfo author;

    @Data
    public static class AuthorInfo {
        /**
         * 作者昵称
         */
        private String nickname;

        /**
         * 作者头像URL
         */
        private String avatarUrl;
    }
}