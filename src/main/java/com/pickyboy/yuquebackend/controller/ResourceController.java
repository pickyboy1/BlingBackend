package com.pickyboy.yuquebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.pickyboy.yuquebackend.common.response.Result;
import com.pickyboy.yuquebackend.domain.dto.resource.CopyResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.CreateResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.MoveResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.RestoreResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.UpdateResourceContentRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.UpdateResourceInfoRequest;
import com.pickyboy.yuquebackend.domain.entity.Resources;
import com.pickyboy.yuquebackend.domain.vo.resource.PublicResourceVO;
import com.pickyboy.yuquebackend.domain.vo.resource.ShareUrlVO;
import com.pickyboy.yuquebackend.service.IResourceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;
import java.util.List;

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

    /**
     * 创建新文档
     */
    @PostMapping("/documents")
    public Result<Resources> createDocument(@Valid @RequestBody CreateResourceRequest request) {
        log.info("创建新文档: {}", request);
        Resources resource = resourceService.createDocument(request);
        return Result.success(resource);
    }

    /**
     * 获取文档内容
     */
    @GetMapping("/documents/{documentId}")
    public Result<Resources> getDocument(@PathVariable Long documentId) {
        log.info("获取文档内容: documentId={}", documentId);
        Resources resource = resourceService.getDocument(documentId);
        return Result.success(resource);
    }

    /**
     * 更新文档内容
     */
    @PutMapping("/documents/{documentId}")
    public Result<Resources> updateDocument(@PathVariable Long documentId,
                                           @Valid @RequestBody UpdateResourceContentRequest request) {
        log.info("更新文档内容: documentId={}, request={}", documentId, request);
        Resources resource = resourceService.updateDocument(documentId, request);
        return Result.success(resource);
    }

    /**
     * 删除文档 (逻辑删除)
     */
    @DeleteMapping("/documents/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Result<Void> deleteDocument(@PathVariable Long documentId) {
        log.info("删除文档: documentId={}", documentId);
        resourceService.deleteDocument(documentId);
        return Result.success();
    }

    /**
     * 更新文档信息 (如重命名, 修改可见性)
     */
    @PatchMapping("/documents/{documentId}/info")
    public Result<Void> updateDocumentInfo(@PathVariable Long documentId,
                                          @Valid @RequestBody UpdateResourceInfoRequest request) {
        log.info("更新文档信息: documentId={}, request={}", documentId, request);
        resourceService.updateDocumentInfo(documentId, request);
        return Result.success();
    }

    /**
     * 从回收站恢复文档
     */
    @PostMapping("/documents/{documentId}/restore")
    public Result<Void> restoreDocument(@PathVariable Long documentId,
                                       @Valid @RequestBody RestoreResourceRequest request) {
        log.info("从回收站恢复文档: documentId={}, request={}", documentId, request);
        resourceService.restoreDocument(documentId, request);
        return Result.success();
    }

    /**
     * 移动文档或目录
     */
    @PostMapping("/documents/{documentId}/move")
    public Result<Void> moveDocument(@PathVariable Long documentId,
                                    @Valid @RequestBody MoveResourceRequest request) {
        log.info("移动文档: documentId={}, request={}", documentId, request);
        resourceService.moveDocument(documentId, request);
        return Result.success();
    }

    /**
     * 复制文档或目录 (递归)
     */
    @PostMapping("/documents/{documentId}/copy")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Void> copyDocument(@PathVariable Long documentId,
                                    @Valid @RequestBody CopyResourceRequest request) {
        log.info("复制文档: documentId={}, request={}", documentId, request);
        resourceService.copyDocument(documentId, request);
        return Result.success();
    }

    /**
     * 生成并获取文档分享链接
     */
    @PostMapping("/documents/{documentId}/share")
    public Result<ShareUrlVO> shareDocument(@PathVariable Long documentId) {
        log.info("生成文档分享链接: documentId={}", documentId);
        ShareUrlVO shareUrl = resourceService.shareDocument(documentId);
        return Result.success(shareUrl);
    }

    /**
     * 查看分享的文档
     */
    @GetMapping("/shared/{shareId}")
    public Result<PublicResourceVO> getSharedDocument(@PathVariable String shareId) {
        log.info("查看分享文档: shareId={}", shareId);
        PublicResourceVO resource = resourceService.getSharedDocument(shareId);
        return Result.success(resource);
    }

    /**
     * 点赞文章
     */
    @PostMapping("/resources/{resourceId}/like")
    public Result<Void> likeResource(@PathVariable Long resourceId) {
        log.info("点赞文章: resourceId={}", resourceId);
        resourceService.likeResource(resourceId);
        return Result.success();
    }

    /**
     * 取消点赞文章
     */
    @DeleteMapping("/resources/{resourceId}/like")
    public Result<Void> unlikeResource(@PathVariable Long resourceId) {
        log.info("取消点赞文章: resourceId={}", resourceId);
        resourceService.unlikeResource(resourceId);
        return Result.success();
    }

    /**
     * 获取文章的评论列表
     */
    @GetMapping("/resources/{resourceId}/comments")
    public Result<List<?>> getResourceComments(@PathVariable Long resourceId) {
        log.info("获取文章评论列表: resourceId={}", resourceId);
        List<?> comments = resourceService.getResourceComments(resourceId);
        return Result.success(comments);
    }

    /**
     * 发表顶级评论
     */
    @PostMapping("/resources/{resourceId}/comments")
    public Result<Object> createComment(@PathVariable Long resourceId,
                                       @RequestBody Object commentRequest) {
        log.info("发表评论: resourceId={}, request={}", resourceId, commentRequest);
        Object comment = resourceService.createComment(resourceId, commentRequest);
        return Result.success(comment);
    }
}