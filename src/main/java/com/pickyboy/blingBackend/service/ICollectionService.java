package com.pickyboy.blingBackend.service;

import java.util.List;

import com.pickyboy.blingBackend.domain.dto.collection.CollectionGroupCreateRequest;
import com.pickyboy.blingBackend.domain.dto.collection.CollectionItemCreateRequest;
import com.pickyboy.blingBackend.domain.vo.collection.CollectionGroup;
import com.pickyboy.blingBackend.domain.vo.user.ActivityRecord;

/**
 * 收藏夹服务接口
 *
 * @author pickyboy
 */
public interface ICollectionService {

    /**
     * 获取当前用户的收藏夹分组列表
     *
     * @return 收藏夹分组列表
     */
    List<CollectionGroup> getUserCollectionGroups();

    /**
     * 创建新的收藏夹分组
     *
     * @param request 创建请求
     * @return 创建的收藏夹分组
     */
    CollectionGroup createCollectionGroup(CollectionGroupCreateRequest request);

    /**
     * 删除收藏夹分组
     *
     * @param groupId 分组ID
     */
    void deleteCollectionGroup(Long groupId);

    /**
     * 获取指定分组下的收藏文章列表
     *
     * @param groupId 分组ID
     * @param page 页码
     * @param limit 每页数量
     * @return 收藏文章列表
     */
    List<ActivityRecord> getArticlesInCollectionGroup(Long groupId, Integer page, Integer limit);

    /**
     * 将文章添加到收藏夹
     *
     * @param groupId 分组ID
     * @param request 添加请求
     */
    void addArticleToCollectionGroup(Long groupId, CollectionItemCreateRequest request);

    /**
     * 从收藏夹移除文章
     *
     * @param groupId 分组ID
     * @param articleId 文章ID
     */
    void removeArticleFromCollectionGroup(Long groupId, Long articleId);
}