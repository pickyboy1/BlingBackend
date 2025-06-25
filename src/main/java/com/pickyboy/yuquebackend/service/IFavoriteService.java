package com.pickyboy.yuquebackend.service;

import java.util.List;

/**
 * 收藏服务接口
 *
 * @author pickyboy
 */
public interface IFavoriteService {

    /**
     * 获取我的收藏夹分组
     *
     * @return 收藏夹分组列表
     */
    List<?> getFavoriteGroups();

    /**
     * 创建收藏夹分组
     *
     * @param createRequest 创建请求
     * @return 收藏夹分组信息
     */
    Object createFavoriteGroup(Object createRequest);

    /**
     * 删除收藏夹分组
     *
     * @param groupId 分组ID
     */
    void deleteFavoriteGroup(Long groupId);

    /**
     * 获取指定分组下的收藏列表
     *
     * @param groupId 分组ID
     * @return 收藏项列表
     */
    List<?> getFavoriteItems(Long groupId);

    /**
     * 添加收藏
     *
     * @param favoriteRequest 收藏请求
     * @return 收藏信息
     */
    Object addFavorite(Object favoriteRequest);

    /**
     * 取消收藏
     *
     * @param favoriteId 收藏ID
     */
    void removeFavorite(Long favoriteId);
}