package com.pickyboy.blingBackend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pickyboy.blingBackend.common.response.Result;
import com.pickyboy.blingBackend.domain.dto.collection.CollectionGroupCreateRequest;
import com.pickyboy.blingBackend.domain.dto.collection.CollectionItemCreateRequest;
import com.pickyboy.blingBackend.domain.vo.collection.CollectionGroup;
import com.pickyboy.blingBackend.domain.vo.user.ActivityRecord;
import com.pickyboy.blingBackend.service.ICollectionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 收藏夹控制器
 * 处理收藏夹相关的API请求
 *
 * @author pickyboy
 */
@Slf4j
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class CollectionController {

    private final ICollectionService collectionService;

    /**
     * 获取当前用户的收藏夹分组列表
     * GET /user/collections
     *
     * @return 收藏夹分组列表
     */
    @GetMapping("/user/collections")
    public Result<List<CollectionGroup>> getUserCollectionGroups() {
        log.info("获取用户收藏夹分组列表");
        List<CollectionGroup> collectionGroups = collectionService.getUserCollectionGroups();
        return Result.success(collectionGroups);
    }

    /**
     * 创建新的收藏夹分组
     * POST /user/collections
     *
     * @param request 创建请求
     * @return 创建的收藏夹分组
     */
    @PostMapping("/user/collections")
    public Result<CollectionGroup> createCollectionGroup(@Valid @RequestBody CollectionGroupCreateRequest request) {
        log.info("创建收藏夹分组: groupName={}", request.getGroupName());
        CollectionGroup collectionGroup = collectionService.createCollectionGroup(request);
        return Result.success(collectionGroup);
    }

    /**
     * 删除收藏夹分组
     * DELETE /collections/{groupId}
     *
     * @param groupId 分组ID
     * @return 操作结果
     */
    @DeleteMapping("/collections/{groupId}")
    public Result<Void> deleteCollectionGroup(@PathVariable Long groupId) {
        log.info("删除收藏夹分组: groupId={}", groupId);
        collectionService.deleteCollectionGroup(groupId);
        return Result.success();
    }

    /**
     * 获取指定分组下的收藏文章列表
     * GET /collections/{groupId}/articles
     *
     * @param groupId 分组ID
     * @param page 页码
     * @param limit 每页数量
     * @return 收藏文章列表
     */
    @GetMapping("/collections/{groupId}/articles")
    public Result<List<ActivityRecord>> getArticlesInCollectionGroup(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        log.info("获取收藏夹分组下的文章列表: groupId={}, page={}, limit={}", groupId, page, limit);
        List<ActivityRecord> articles = collectionService.getArticlesInCollectionGroup(groupId, page, limit);
        return Result.success(articles);
    }

    /**
     * 将文章添加到收藏夹
     * POST /collections/{groupId}/articles
     *
     * @param groupId 分组ID
     * @param request 添加请求
     * @return 操作结果
     */
    @PostMapping("/collections/{groupId}/articles")
    public Result<Void> addArticleToCollectionGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody CollectionItemCreateRequest request) {
        log.info("添加文章到收藏夹: groupId={}, resourceId={}", groupId, request.getResourceId());
        collectionService.addArticleToCollectionGroup(groupId, request);
        return Result.success();
    }

    /**
     * 从收藏夹移除文章
     * DELETE /collections/{groupId}/articles/{articleId}
     *
     * @param groupId 分组ID
     * @param articleId 文章ID
     * @return 操作结果
     */
    @DeleteMapping("/collections/{groupId}/articles/{articleId}")
    public Result<Void> removeArticleFromCollectionGroup(
            @PathVariable Long groupId,
            @PathVariable Long articleId) {
        log.info("从收藏夹移除文章: groupId={}, articleId={}", groupId, articleId);
        collectionService.removeArticleFromCollectionGroup(groupId, articleId);
        return Result.success();
    }
}