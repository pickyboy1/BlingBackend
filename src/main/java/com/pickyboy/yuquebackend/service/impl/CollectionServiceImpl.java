package com.pickyboy.yuquebackend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pickyboy.yuquebackend.common.exception.BusinessException;
import com.pickyboy.yuquebackend.common.exception.ErrorCode;
import com.pickyboy.yuquebackend.common.utils.CurrentHolder;
import com.pickyboy.yuquebackend.domain.dto.collection.CollectionGroupCreateRequest;
import com.pickyboy.yuquebackend.domain.dto.collection.CollectionItemCreateRequest;
import com.pickyboy.yuquebackend.domain.entity.FavoriteGroups;
import com.pickyboy.yuquebackend.domain.entity.Favorites;
import com.pickyboy.yuquebackend.domain.vo.collection.CollectionGroup;
import com.pickyboy.yuquebackend.domain.vo.user.ActivityRecord;
import com.pickyboy.yuquebackend.mapper.FavoriteGroupsMapper;
import com.pickyboy.yuquebackend.mapper.FavoritesMapper;
import com.pickyboy.yuquebackend.service.ICollectionService;
import com.pickyboy.yuquebackend.service.IResourceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 收藏夹服务实现类
 *
 * @author pickyboy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements ICollectionService {

    private final FavoriteGroupsMapper favoriteGroupsMapper;
    private final FavoritesMapper favoritesMapper;
    private final IResourceService resourceService;

    @Override
    public List<CollectionGroup> getUserCollectionGroups() {
        log.info("获取用户收藏夹分组列表");
        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        List<FavoriteGroups> favoriteGroups = favoriteGroupsMapper.selectList(
                new LambdaQueryWrapper<FavoriteGroups>()
                        .eq(FavoriteGroups::getUserId, currentUserId)
                        .orderByDesc(FavoriteGroups::getCreatedAt)
        );

        return favoriteGroups.stream()
                .map(this::convertToCollectionGroup)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CollectionGroup createCollectionGroup(CollectionGroupCreateRequest request) {
        log.info("创建收藏夹分组: groupName={}", request.getGroupName());
        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 检查分组名称是否已存在
        boolean exists = favoriteGroupsMapper.exists(
                new LambdaQueryWrapper<FavoriteGroups>()
                        .eq(FavoriteGroups::getUserId, currentUserId)
                        .eq(FavoriteGroups::getGroupName, request.getGroupName())
        );
        if (exists) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS, "收藏夹分组名称已存在");
        }

        FavoriteGroups favoriteGroup = new FavoriteGroups();
        favoriteGroup.setUserId(currentUserId);
        favoriteGroup.setGroupName(request.getGroupName());
        favoriteGroup.setCount(0);

        favoriteGroupsMapper.insert(favoriteGroup);
        log.info("创建收藏夹分组成功: groupId={}", favoriteGroup.getId());

        return convertToCollectionGroup(favoriteGroup);
    }

    @Override
    @Transactional
    public void deleteCollectionGroup(Long groupId) {
        log.info("删除收藏夹分组: groupId={}", groupId);
        Long currentUserId = getCurrentUserId();

        // 检查分组是否存在且属于当前用户
        FavoriteGroups favoriteGroup = favoriteGroupsMapper.selectOne(
                new LambdaQueryWrapper<FavoriteGroups>()
                        .eq(FavoriteGroups::getId, groupId)
                        .eq(FavoriteGroups::getUserId, currentUserId)
        );
        if (favoriteGroup == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "收藏夹分组不存在");
        }

        // 删除分组下的所有收藏条目
        favoritesMapper.delete(
                new LambdaQueryWrapper<Favorites>()
                        .eq(Favorites::getGroupId, groupId)
        );

        // 删除分组
        favoriteGroupsMapper.deleteById(groupId);
        log.info("删除收藏夹分组成功: groupId={}", groupId);
    }

    @Override
    public List<ActivityRecord> getArticlesInCollectionGroup(Long groupId, Integer page, Integer limit) {
        log.info("获取收藏夹分组下的文章列表: groupId={}, page={}, limit={}", groupId, page, limit);
        Long currentUserId = getCurrentUserId();

        // 检查分组是否存在且属于当前用户
        FavoriteGroups favoriteGroup = favoriteGroupsMapper.selectOne(
                new LambdaQueryWrapper<FavoriteGroups>()
                        .eq(FavoriteGroups::getId, groupId)
                        .eq(FavoriteGroups::getUserId, currentUserId)
        );
        if (favoriteGroup == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "收藏夹分组不存在");
        }

        Integer offset = (page - 1) * limit;
        return favoritesMapper.getArticlesInGroup(groupId, offset, limit);
    }

    @Override
    @Transactional
    public void addArticleToCollectionGroup(Long groupId, CollectionItemCreateRequest request) {
        log.info("添加文章到收藏夹: groupId={}, resourceId={}", groupId, request.getResourceId());
        Long currentUserId = getCurrentUserId();

        // 检查分组是否存在且属于当前用户
        FavoriteGroups favoriteGroup = favoriteGroupsMapper.selectOne(
                new LambdaQueryWrapper<FavoriteGroups>()
                        .eq(FavoriteGroups::getId, groupId)
                        .eq(FavoriteGroups::getUserId, currentUserId)
        );
        if (favoriteGroup == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "收藏夹分组不存在");
        }

        // 【重构】检查文章是否存在且可访问（包括知识库权限验证）
        try {
            resourceService.getResourceById(request.getResourceId());
        } catch (BusinessException e) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在、无访问权限或其知识库已被删除");
        }

        // 检查是否已收藏
        boolean exists = favoritesMapper.exists(
                new LambdaQueryWrapper<Favorites>()
                        .eq(Favorites::getGroupId, groupId)
                        .eq(Favorites::getResourceId, request.getResourceId())
        );
        if (exists) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS, "文章已在该收藏夹中");
        }

        // 添加收藏记录
        Favorites favorite = new Favorites();
        favorite.setGroupId(groupId);
        favorite.setResourceId(request.getResourceId());
        favoritesMapper.insert(favorite);

        // 【修复并发问题】原子操作增加分组收藏数量
        favoriteGroupsMapper.incrementGroupCount(groupId);

        log.info("添加文章到收藏夹成功: groupId={}, resourceId={}", groupId, request.getResourceId());
    }

    @Override
    @Transactional
    public void removeArticleFromCollectionGroup(Long groupId, Long articleId) {
        log.info("从收藏夹移除文章: groupId={}, articleId={}", groupId, articleId);
        Long currentUserId = getCurrentUserId();

        // 检查分组是否存在且属于当前用户
        FavoriteGroups favoriteGroup = favoriteGroupsMapper.selectOne(
                new LambdaQueryWrapper<FavoriteGroups>()
                        .eq(FavoriteGroups::getId, groupId)
                        .eq(FavoriteGroups::getUserId, currentUserId)
        );
        if (favoriteGroup == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "收藏夹分组不存在");
        }

        // 检查收藏记录是否存在
        Favorites favorite = favoritesMapper.selectOne(
                new LambdaQueryWrapper<Favorites>()
                        .eq(Favorites::getGroupId, groupId)
                        .eq(Favorites::getResourceId, articleId)
        );
        if (favorite == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "收藏记录不存在");
        }

        // 删除收藏记录
        favoritesMapper.deleteById(favorite.getId());

        // 【修复并发问题】原子操作减少分组收藏数量
        favoriteGroupsMapper.decrementGroupCount(groupId);

        log.info("从收藏夹移除文章成功: groupId={}, articleId={}", groupId, articleId);
    }

    /**
     * 获取当前用户ID
     *
     * @return 当前用户ID
     */
    private Long getCurrentUserId() {
        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUserId;
    }

    /**
     * 转换FavoriteGroups为CollectionGroup
     *
     * @param favoriteGroup 收藏夹分组实体
     * @return 收藏夹分组VO
     */
    private CollectionGroup convertToCollectionGroup(FavoriteGroups favoriteGroup) {
        CollectionGroup collectionGroup = new CollectionGroup();
        collectionGroup.setId(favoriteGroup.getId().toString());
        collectionGroup.setGroupName(favoriteGroup.getGroupName());
        collectionGroup.setCount(favoriteGroup.getCount());
        return collectionGroup;
    }
}