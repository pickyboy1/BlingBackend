package com.pickyboy.yuquebackend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pickyboy.yuquebackend.common.response.PageResult;
import com.pickyboy.yuquebackend.common.response.Result;
import com.pickyboy.yuquebackend.domain.dto.comment.CommentCreateRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.CopyResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.CreateResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.MoveResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.RestoreResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.UpdateResourceContentRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.UpdateResourceInfoRequest;
import com.pickyboy.yuquebackend.domain.entity.ResourceVersions;
import com.pickyboy.yuquebackend.domain.entity.Resources;
import com.pickyboy.yuquebackend.domain.vo.comment.RootCommentVO;
import com.pickyboy.yuquebackend.domain.vo.comment.SubCommentVO;
import com.pickyboy.yuquebackend.domain.vo.resource.PublicResourceVO;
import com.pickyboy.yuquebackend.domain.vo.resource.ShareUrlVO;
import com.pickyboy.yuquebackend.service.IResourceService;
import com.pickyboy.yuquebackend.service.IResourceVersionsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档资源管理控制器
 *
 * @author pickyboy
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping
public class ResourceController {

    private final IResourceService resourceService;
    private final IResourceVersionsService resourceVersionsService;

    /**
     * 在知识库中新建资源
     */
    @PostMapping("/knowledge-bases/{kbId}/resources")
    public Result<Resources> createResource(@PathVariable Long kbId,
                                           @Valid @RequestBody CreateResourceRequest request) {
        log.info("在知识库中新建资源: kbId={}, request={}", kbId, request);
        Resources resource = resourceService.createResource(kbId, request);
        return Result.success(resource);
    }

    /**
     * 查看单个资源的完整信息
     */
    @GetMapping("/resources/{resId}")
    public Result<Resources> getResourceById(@PathVariable Long resId) {
        log.info("查看单个资源: resId={}", resId);
        Resources resource = resourceService.getResourceById(resId);
        return Result.success(resource);
    }

    /**
     * 更新资源内容或标题
     */
    @PutMapping("/resources/{resId}")
    public Result<Void> updateResource(@PathVariable Long resId,
                                      @Valid @RequestBody UpdateResourceContentRequest request) {
        log.info("更新资源内容或标题: resId={}, request={}", resId, request);
        resourceService.updateResource(resId, request);
        return Result.success();
    }

    /**
     * 删除资源 (逻辑删除)
     */
    @DeleteMapping("/resources/{resId}")
    public Result<Void> deleteResource(@PathVariable Long resId) {
        log.info("删除资源: resId={}", resId);
        resourceService.deleteResource(resId);
        return Result.success();
    }

    /**
     * 重命名资源
     */
    @PatchMapping("/resources/{resId}/rename")
    public Result<Void> renameResource(@PathVariable Long resId,
                                      @Valid @RequestBody UpdateResourceInfoRequest request) {
        log.info("重命名资源: resId={}, request={}", resId, request);
        resourceService.renameResource(resId, request);
        return Result.success();
    }

    /**
     * 更新资源可见性
     */
    @PatchMapping("/resources/{resId}/visibility")
    public Result<Void> updateResourceVisibility(@PathVariable Long resId,
                                                 @RequestBody Object visibilityRequest) {
        log.info("更新资源可见性: resId={}, request={}", resId, visibilityRequest);
        resourceService.updateResourceVisibility(resId, visibilityRequest);
        return Result.success();
    }

    /**
     * 更新资源上架/下架状态
     */
    @PatchMapping("/resources/{resId}/status")
    public Result<Void> updateResourceStatus(@PathVariable Long resId,
                                            @RequestBody Object statusRequest) {
        log.info("更新资源状态: resId={}, request={}", resId, statusRequest);
        resourceService.updateResourceStatus(resId, statusRequest);
        return Result.success();
    }

    /**
     * 恢复资源
     */
    @PostMapping("/recycle-bin/resources/{resId}")
    public Result<Void> restoreResource(@PathVariable Long resId,
                                       @RequestBody(required = false) RestoreResourceRequest request) {
        log.info("从回收站恢复资源: resId={}, request={}", resId, request);
        resourceService.restoreResource(resId, request);
        return Result.success();
    }

    /**
     * 彻底删除资源
     */
    @DeleteMapping("/recycle-bin/resources/{resId}")
    public Result<Void> permanentlyDeleteResource(@PathVariable Long resId) {
        log.info("彻底删除资源: resId={}", resId);
        resourceService.permanentlyDeleteResource(resId);
        return Result.success();
    }

    /**
     * 移动资源或目录
     */
    @PostMapping("/resources/{resId}/move")
    public Result<Void> moveResource(@PathVariable Long resId,
                                    @Valid @RequestBody MoveResourceRequest request) {
        log.info("移动资源: resId={}, request={}", resId, request);
        resourceService.moveResource(resId, request);
        return Result.success();
    }

    /**
     * 复制资源
     */
    @PostMapping("/resources/{resId}/copy")
    public Result<Void> copyResource(@PathVariable Long resId,
                                    @Valid @RequestBody CopyResourceRequest request) {
        log.info("复制资源: resId={}, request={}", resId, request);
        resourceService.copyResource(resId, request);
        return Result.success();
    }

    /**
     * 复制目录(及目录下所有子资源)
     */
    @PostMapping("/resources/{resId}/copy-tree")
    public Result<Void> copyResourceTree(@PathVariable Long resId,
                                        @Valid @RequestBody CopyResourceRequest request) {
        log.info("复制目录树: resId={}, request={}", resId, request);
        resourceService.copyResourceTree(resId, request);
        return Result.success();
    }

    /**
     * 生成资源分享链接
     */
    @PostMapping("/resources/{resId}/share")
    public Result<ShareUrlVO> generateResourceShareLink(@PathVariable Long resId) {
        log.info("生成资源分享链接: resId={}", resId);
        ShareUrlVO shareUrl = resourceService.generateResourceShareLink(resId);
        return Result.success(shareUrl);
    }

    /**
     * 访问分享链接查看资源
     */
    @GetMapping("/share/{kbShareId}/{resShareId}")
    public Result<PublicResourceVO> accessSharedResource(@PathVariable String kbShareId,
                                                        @PathVariable String resShareId) {
        log.info("访问分享资源: kbShareId={}, resShareId={}", kbShareId, resShareId);
        PublicResourceVO resource = resourceService.accessSharedResource(kbShareId, resShareId);
        return Result.success(resource);
    }

    /**
     * 点赞文章
     */
    @PostMapping("/articles/{articleId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Result<Void> likeArticle(@PathVariable Long articleId) {
        log.info("点赞文章: articleId={}", articleId);
        resourceService.likeArticle(articleId);
        return Result.success();
    }

    /**
     * 取消点赞文章
     */
    @DeleteMapping("/articles/{articleId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Result<Void> unlikeArticle(@PathVariable Long articleId) {
        log.info("取消点赞文章: articleId={}", articleId);
        resourceService.unlikeArticle(articleId);
        return Result.success();
    }

    /**
     * 获取文章的根评论列表(不包含子评论)
     */
    @GetMapping("/articles/{articleId}/comments")
    public Result<List<RootCommentVO>> listArticleComments(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取文章根评论列表: articleId={}, page={}, limit={}", articleId, page, limit);
        List<RootCommentVO> comments = resourceService.listArticleComments(articleId, page, limit);
        return Result.success(comments);
    }

    /**
     * 获取评论的子评论列表
     */
    @GetMapping("/comments/{commentId}/replies")
    public Result<List<SubCommentVO>> listCommentReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        log.info("获取评论回复列表: commentId={}, page={}, limit={}", commentId, page, limit);
        List<SubCommentVO> replies = resourceService.listCommentReplies(commentId, page, limit);
        return Result.success(replies);
    }

    /**
     * 发表评论
     */
    @PostMapping("/articles/{articleId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<RootCommentVO> createComment(@PathVariable Long articleId,
                                              @Valid @RequestBody CommentCreateRequest commentRequest) {
        log.info("发表评论: articleId={}, request={}", articleId, commentRequest);
        RootCommentVO comment = resourceService.createComment(articleId, commentRequest);
        return Result.success(comment);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        log.info("删除评论: commentId={}", commentId);
        resourceService.deleteComment(commentId);
        return Result.success();
    }

    /**
     * 获取推荐文章列表
     */
    @GetMapping("/explore/articles")
    public Result<List<PublicResourceVO>> listExploreArticles(
            @RequestParam(defaultValue = "hot") String sortBy,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        log.info("获取推荐文章列表: sortBy={}, page={}, limit={}", sortBy, page, limit);
        List<PublicResourceVO> articles = resourceService.listExploreArticles(sortBy, page, limit);
        return Result.success(articles);
    }

    /**
     * 提交投稿（申请推荐）
     */
    @PostMapping("/submissions")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Result<Void> createSubmission(@RequestBody Object submissionRequest) {
        log.info("提交投稿: request={}", submissionRequest);
        resourceService.createSubmission(submissionRequest);
        return Result.success();
    }

    /**
     * 分页查询资源版本历史
     * GET /resources/{resId}/versions
     */
    @GetMapping("/resources/{resId}/versions")
    public Result<PageResult<ResourceVersions>> listResourceVersions(
            @PathVariable Long resId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("分页查询资源版本历史: resId={}, page={}, limit={}", resId, page, limit);
        PageResult<ResourceVersions> pageResult = resourceVersionsService.getResourceVersionsPage(resId, page, limit);
        return Result.success(pageResult);
    }

    /**
     * 获取资源版本详情
     * GET /resources/versions/{versionId}
     */
    @GetMapping("/resources/versions/{versionId}")
    public Result<ResourceVersions> getResourceVersion(@PathVariable Long versionId) {
        log.info("获取资源版本详情: versionId={}", versionId);
        ResourceVersions version = resourceVersionsService.getById(versionId);
        return Result.success(version);
    }

    /**
     * 删除指定资源版本
     * DELETE /resources/versions/{versionId}
     */
    @DeleteMapping("/resources/versions/{versionId}")
    public Result<Void> deleteResourceVersion(@PathVariable Long versionId) {
        log.info("删除资源版本: versionId={}", versionId);
        resourceVersionsService.deleteResourceVersionById(versionId);
        return Result.success();
    }

    /**
     * 恢复资源到指定版本
     * POST /resources/{resId}/versions/{versionId}/restore
     */
    @PostMapping("/resources/{resId}/versions/{versionId}/restore")
    public Result<Void> restoreResourceToVersion(@PathVariable Long resId, @PathVariable Long versionId) {
        log.info("恢复资源到指定版本: resId={}, versionId={}", resId, versionId);
        resourceService.restoreResourceToVersion(resId, versionId);
        return Result.success();
    }

    /**
     * 获取知识库下的资源目录树(知识库模块实现)
     */
/*     @GetMapping("/knowledge-bases/{kbId}/resources/tree")
    public Result<List<?>> getResourceTree(@PathVariable Long kbId) {
        log.info("获取知识库资源目录树: kbId={}", kbId);
        List<?> tree = resourceService.getResourceTree(kbId);
        return Result.success(tree);
    } */
}