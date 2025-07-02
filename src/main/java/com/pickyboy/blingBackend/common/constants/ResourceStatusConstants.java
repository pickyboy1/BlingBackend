package com.pickyboy.blingBackend.common.constants;

/**
 * 资源状态相关常量
 * 用于用户历史记录、收藏夹等功能
 *
 * @author pickyboy
 */
public class ResourceStatusConstants {

    /**
     * 文章状态常量
     */
    public static class ArticleStatus {
        /** 正常可访问 */
        public static final String NORMAL = "NORMAL";

        /** 文章已删除 */
        public static final String ARTICLE_DELETED = "ARTICLE_DELETED";

        /** 知识库已删除 */
        public static final String KB_DELETED = "KB_DELETED";

        /** 作者已注销 */
        public static final String AUTHOR_DELETED = "AUTHOR_DELETED";

        /** 文章已设为私密 */
        public static final String PRIVATE = "PRIVATE";
    }
}