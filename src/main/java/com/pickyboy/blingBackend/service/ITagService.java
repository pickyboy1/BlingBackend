package com.pickyboy.blingBackend.service;

import java.util.List;

/**
 * 标签服务接口
 *
 * @author pickyboy
 */
public interface ITagService {

    /**
     * 获取当前用户的所有标签
     *
     * @return 标签列表
     */
    List<?> getUserTags();

    /**
     * 创建新标签
     *
     * @param createRequest 创建请求
     * @return 标签信息
     */
    Object createTag(Object createRequest);

    /**
     * 修改标签名
     *
     * @param tagId 标签ID
     * @param updateRequest 更新请求
     * @return 更新后的标签信息
     */
    Object updateTag(Long tagId, Object updateRequest);

    /**
     * 删除标签
     *
     * @param tagId 标签ID
     */
    void deleteTag(Long tagId);
}