package com.pickyboy.blingBackend.common.constants;

public class RedisKeyConstants {

    /** 资源浏览防刷键: view:resource:{资源ID}:{用户ID} */
    public static final String RESOURCE_VIEW_KEY_TEMPLATE = "view:resource:%s:%s";

    /** 知识库浏览防刷键: view:kb:{知识库ID}:{用户ID} */
    public static final String KB_VIEW_KEY_TEMPLATE = "view:kb:%s:%s";

    public static String getResourceViewKey(Long resourceId, Long userId) {
        return String.format(RESOURCE_VIEW_KEY_TEMPLATE, resourceId, userId);
    }

    public static String getKbViewKey(Long kbId, Long userId) {
        return String.format(KB_VIEW_KEY_TEMPLATE, kbId, userId);
    }
}