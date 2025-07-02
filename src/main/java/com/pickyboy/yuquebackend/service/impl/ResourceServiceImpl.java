package com.pickyboy.yuquebackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.yuquebackend.common.exception.BusinessException;
import com.pickyboy.yuquebackend.common.exception.ErrorCode;
import com.pickyboy.yuquebackend.common.utils.CurrentHolder;
import com.pickyboy.yuquebackend.common.utils.MinioUtil;
import com.pickyboy.yuquebackend.domain.dto.comment.CommentCreateRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.CopyResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.CreateResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.MoveResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.UpdateResourceContentRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.UpdateResourceInfoRequest;
import com.pickyboy.yuquebackend.domain.entity.Comments;
import com.pickyboy.yuquebackend.domain.entity.Likes;
import com.pickyboy.yuquebackend.domain.entity.Resources;
import com.pickyboy.yuquebackend.domain.entity.Users;
import com.pickyboy.yuquebackend.domain.entity.ViewHistories;
import com.pickyboy.yuquebackend.domain.vo.comment.RootCommentVO;
import com.pickyboy.yuquebackend.domain.vo.comment.SubCommentVO;
import com.pickyboy.yuquebackend.domain.vo.resource.PublicResourceVO;
import com.pickyboy.yuquebackend.domain.vo.resource.ShareUrlVO;
import com.pickyboy.yuquebackend.mapper.ResourcesMapper;
import com.pickyboy.yuquebackend.mapper.UsersMapper;
import com.pickyboy.yuquebackend.mapper.ViewHistoriesMapper;
import com.pickyboy.yuquebackend.service.ICommentsService;
import com.pickyboy.yuquebackend.service.IKnowledgeBaseValidationService;
import com.pickyboy.yuquebackend.service.ILikesService;
import com.pickyboy.yuquebackend.service.IResourceService;
import com.pickyboy.yuquebackend.service.IResourceVersionsService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档/资源服务实现类
 *
 * @author pickyboy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl extends ServiceImpl<ResourcesMapper, Resources> implements IResourceService {

    private final IResourceVersionsService resourceVersionsService;
    private final IKnowledgeBaseValidationService knowledgeBaseValidationService;
    private final MinioUtil minioUtil;
    private final ViewHistoriesMapper viewHistoriesMapper;
    private final ILikesService likesService;
    private final ICommentsService commentsService;
    private final UsersMapper usersMapper;
    /* 在知识库中新建资源
     * 只新建资源记录,无实际内容
     */
    @Override
    @Transactional
    public Resources createResource(Long kbId, CreateResourceRequest createRequest) {
        log.info("在知识库中新建资源: kbId={}, request={}", kbId, createRequest);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 检查知识库是否存在且未被删除
        validateKnowledgeBaseAccess(kbId, userId);

        Resources resource = new Resources();
        resource.setKnowledgeBaseId(kbId);
        resource.setUserId(userId);
        resource.setTitle(createRequest.getTitle());
        resource.setType(createRequest.getType());
        resource.setPreId(createRequest.getPreId());
        save(resource);
        return resource;
    }

    @Override
    public Resources getResourceById(Long resId) {
        log.info("查看单个资源: resId={}", resId);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Resources resource = getById(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        // 检查对应知识库是否被删除
        validateKnowledgeBaseAccess(resource.getKnowledgeBaseId(), userId);

        if (resource.getUserId() != userId && resource.getVisibility() == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }

        // 异步记录浏览历史
        recordViewHistoryAsync(userId, resId);

        // 增加访问量
        // todo: 使用Redis缓存访问量,根据时间窗口,过滤掉重复访问
        resource.setViewCount(resource.getViewCount() + 1);
        updateById(resource);
        return resource;
    }

    /**
     * 异步记录浏览历史
     *
     * @param userId 用户ID
     * @param resourceId 资源ID
     */
    @Async
    public void recordViewHistoryAsync(Long userId, Long resourceId) {
        try {
            log.debug("异步记录浏览历史: userId={}, resourceId={}", userId, resourceId);
            ViewHistories viewHistory = new ViewHistories();
            viewHistory.setUserId(userId);
            viewHistory.setResourceId(resourceId);
            viewHistory.setLastViewAt(LocalDateTime.now());
            viewHistoriesMapper.insertOrUpdateViewHistory(viewHistory);
        } catch (Exception e) {
            log.warn("记录浏览历史失败: userId={}, resourceId={}, error={}", userId, resourceId, e.getMessage());
            // 不抛出异常，避免影响主业务流程
        }
    }

    @Override
    @Transactional
    public void updateResource(Long resId, UpdateResourceContentRequest updateRequest) {
        log.info("更新资源内容或标题: resId={}, request={}", resId, updateRequest);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Resources resource = getById(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        // 检查对应知识库是否被删除
        validateKnowledgeBaseAccess(resource.getKnowledgeBaseId(), userId);

        if (resource.getUserId() != userId) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }
        if (updateRequest.getTitle() != null) {
            resource.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getContent() != null) {
            String oldContent = resource.getContent();
            String newContent = updateRequest.getContent();
            // 如果更新了内容，则创建资源版本记录
            if (!oldContent.equals(newContent)) {
                resourceVersionsService.createResourceVersion(resource.getId(), oldContent);
            }
            resource.setContent(newContent);
        }
        updateById(resource);
    }

    @Override
    @Transactional
    public void deleteResource(Long resId) {
        log.info("删除资源: resId={}", resId);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Resources resource = getById(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        // 检查对应知识库是否被删除
        validateKnowledgeBaseAccess(resource.getKnowledgeBaseId(), userId);

        if (resource.getUserId() != userId) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }
        // 递归删除当前资源及其子资源
        recursiveDeleteResource(resId);
    }

    /**
     * 递归删除资源及其子资源
     */
    private void recursiveDeleteResource(Long parentId) {
        // 查找所有子资源
        List<Resources> children = list(
                new LambdaQueryWrapper<Resources>()
                        .eq(Resources::getPreId, parentId)
        );

        // 递归删除子资源
        for (Resources child : children) {
            recursiveDeleteResource(child.getId());
        }

        // 删除当前资源（逻辑删除）
        removeById(parentId);
        log.info("已删除资源: resId={}", parentId);
    }

    @Override
    public void renameResource(Long resId, UpdateResourceInfoRequest infoRequest) {
        log.info("重命名资源: resId={}, request={}", resId, infoRequest);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Resources resource = getById(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        // 检查对应知识库是否被删除
        validateKnowledgeBaseAccess(resource.getKnowledgeBaseId(), userId);

        if (resource.getUserId() != userId) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }
        if (infoRequest.getTitle() != null) {
            resource.setTitle(infoRequest.getTitle());
        }
        updateById(resource);
    }

    @Override
    public void updateResourceVisibility(Long resId, Object visibilityRequest) {
        VisibilityRequest vRequest = (VisibilityRequest) visibilityRequest;
        log.info("更新资源可见性: resId={}, request={}", resId, vRequest);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Resources resource = getById(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        // 检查对应知识库是否被删除
        validateKnowledgeBaseAccess(resource.getKnowledgeBaseId(), userId);

        if (resource.getUserId() != userId) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }
        resource.setVisibility(vRequest.getVisibility());
        updateById(resource);
    }

    /*
     * 更新资源状态(上架/下架) 0:下架 1:上架
     */
    @Override
    public void updateResourceStatus(Long resId, Object statusRequest) {
        StatusRequest sRequest = (StatusRequest) statusRequest;
        log.info("更新资源状态: resId={}, request={}", resId, sRequest);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Resources resource = getById(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        // 检查对应知识库是否被删除
        validateKnowledgeBaseAccess(resource.getKnowledgeBaseId(), userId);

        if (resource.getUserId() != userId) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }
        resource.setStatus(sRequest.getStatus());
        updateById(resource);
    }

    @Override
    @Transactional
    public void restoreResource(Long resId) {
        log.info("从回收站恢复资源: resId={}", resId);

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 查询已删除的资源（需要绕过逻辑删除）
        Resources resource = getBaseMapper().selectById(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        // 验证权限
        if (!resource.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }

        // 验证资源是否已被删除
        if (!Boolean.TRUE.equals(resource.getIsDeleted())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "资源未被删除");
        }

        // 使用UpdateWrapper恢复资源，默认恢复到根节点（preId设为null）
        LambdaUpdateWrapper<Resources> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Resources::getId, resId)
                .set(Resources::getIsDeleted, false)
                .set(Resources::getPreId, null); // 恢复到根节点

        boolean updated = update(updateWrapper);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "资源恢复失败");
        }

        log.info("资源恢复成功: resId={}", resId);
    }

    @Override
    public void permanentlyDeleteResource(Long resId) {
        log.info("彻底删除资源: resId={}", resId);
        return ;
        /* 仅参考实现
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Resources resource = getById(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        if (resource.getUserId() != userId) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }
        // 删除所有关联记录(资源版本,资源点赞,资源评论等)
        resourceVersionsService.deleteResourceVersion(resId);
        resourceLikesService.deleteResourceLike(resId);
        resourceCommentsService.deleteResourceComment(resId);
        removeById(resId);
        */
    }

    /*
     * 移动资源或目录(递归移动)
     */
    @Override
    @Transactional
    public void moveResource(Long resId, MoveResourceRequest moveRequest) {
        log.info("移动资源: resId={}, request={}", resId, moveRequest);

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 验证源资源是否存在且有权限操作
        Resources resource = getById(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        if (!resource.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }

        // 验证目标知识库是否存在且有权限访问
        knowledgeBaseValidationService.validateKnowledgeBaseOwnership(moveRequest.getTargetKbId(), userId);

        // 验证目标父节点是否存在（如果不为null）
        if (moveRequest.getTargetPreId() != null) {
            Resources targetParent = getById(moveRequest.getTargetPreId());
            if (targetParent == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "目标父节点不存在");
            }
            if (!targetParent.getKnowledgeBaseId().equals(moveRequest.getTargetKbId())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "目标父节点不属于目标知识库");
            }
        }

        // 使用UpdateWrapper来支持将preId设置为null（移动到根节点）
        LambdaUpdateWrapper<Resources> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Resources::getId, resId)
                .set(Resources::getKnowledgeBaseId, moveRequest.getTargetKbId())
                .set(Resources::getPreId, moveRequest.getTargetPreId()); // 支持设置为null

        boolean updated = update(updateWrapper);
        if (!updated) {
            throw new BusinessException(ErrorCode.RESOURCE_MOVE_FAILED);
        }

        // 递归更新所有子资源的知识库ID，保持原有的父子关系
        updateChildrenKnowledgeBaseId(resId, moveRequest.getTargetKbId());

        log.info("资源移动成功: resId={}, targetKbId={}, targetPreId={}",
                resId, moveRequest.getTargetKbId(), moveRequest.getTargetPreId());
    }

    /**
     * 递归更新子资源的知识库ID
     */
    private void updateChildrenKnowledgeBaseId(Long parentId, Long newKbId) {
        List<Resources> children = list(new LambdaQueryWrapper<Resources>()
                .eq(Resources::getPreId, parentId)
        );

        for (Resources child : children) {
            child.setKnowledgeBaseId(newKbId);
            updateById(child);
            // 递归处理子资源的子资源
            updateChildrenKnowledgeBaseId(child.getId(), newKbId);
        }
    }

    /*
     * 复制资源(非递归复制)
     */
    @Override
    @Transactional
    public Resources copyResource(Long resId, CopyResourceRequest copyRequest) {
        log.info("复制资源: resId={}, request={}", resId, copyRequest);
        // 1. 权限和存在性验证
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Resources resource = getById(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        if (!resource.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }

        // 检查源资源对应的知识库是否被删除
        validateKnowledgeBaseAccess(resource.getKnowledgeBaseId(), userId);

        // 验证目标知识库是否存在且有权限访问
        knowledgeBaseValidationService.validateKnowledgeBaseOwnership(copyRequest.getTargetKbId(), userId);

        // 2. 深拷贝文件内容
        // 假设资源类型存储在resource.getType()中，如果没有则用默认值
        String uploadType = "resource";
        String newContentUrl = minioUtil.copyObject(
                resource.getContent(),      // 源文件URL
                resource.getTitle(),        // 使用原标题作为新文件名
                uploadType,                 // 目标上传类型 (决定存储桶)
                userId.toString()           // 当前用户ID
        );

        // 3. 准备并保存新的资源实体 (这里不能直接修改原resource对象)
        Resources newResource = new Resources();
        newResource.setTitle(resource.getTitle()); // ... 其他属性

        // 设置新的、不同的属性
        newResource.setId(null); // 清除ID，让数据库自动生成
        newResource.setKnowledgeBaseId(copyRequest.getTargetKbId());
        newResource.setPreId(copyRequest.getTargetPreId());
        newResource.setContent(newContentUrl); // 设置新的文件URL
        newResource.setCreatedAt(null);
        newResource.setUpdatedAt(null);
        newResource.setIsDeleted(false);
        newResource.setStatus(0);
        newResource.setViewCount(0);
        newResource.setLikeCount(0);
        newResource.setCommentCount(0);
        newResource.setFavoriteCount(0);
        newResource.setShareId(null);
        newResource.setVisibility(0);
        newResource.setPublishedAt(null);

        save(newResource);
        return newResource;
    }

    /*
     * 复制目录(及目录下所有子资源)
     */
    @Override
    @Transactional
    public void copyResourceTree(Long resId, CopyResourceRequest copyRequest) {
        // 复制根资源
        Resources copiedRoot = copyResource(resId, copyRequest);

        // 递归复制子资源
        recursiveCopyChildren(resId, copiedRoot.getId(), copyRequest.getTargetKbId());
    }

    /**
     * 递归辅助方法，用于复制子节点
     * @param sourceParentId 源父节点ID
     * @param newParentId 新创建的父节点ID
     * @param targetKbId 目标知识库ID
     */
    private void recursiveCopyChildren(Long sourceParentId, Long newParentId, Long targetKbId) {
        // 1. 查找所有直接子节点
        List<Resources> children = list(new LambdaQueryWrapper<Resources>()
                .eq(Resources::getPreId, sourceParentId)
        );

        if (children.isEmpty()) {
            return; // 如果没有子节点，递归结束
        }

        // 2. 遍历子节点
        for (Resources child : children) {
            // 2.1 复制当前子节点，并将其父节点设置为新创建的父节点
            CopyResourceRequest childCopyRequest = new CopyResourceRequest(targetKbId, newParentId);
            Resources newChildNode = this.copyResource(child.getId(), childCopyRequest);

            // 2.2 如果这个子节点本身也是一个目录，则继续递归
            // (我们通过判断它是否还有子节点来简化，或者可以根据type字段判断)
            recursiveCopyChildren(child.getId(), newChildNode.getId(), targetKbId);
        }
    }

    // todo:
    @Override
    public ShareUrlVO generateResourceShareLink(Long resId) {
        log.info("生成资源分享链接: resId={}", resId);
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public PublicResourceVO accessSharedResource(String kbShareId, String resShareId) {
        log.info("访问分享资源: kbShareId={}, resShareId={}", kbShareId, resShareId);
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public void likeArticle(Long articleId) {
        log.info("点赞文章: articleId={}", articleId);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Resources resource = getById(articleId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        // 查询是否已点赞
        Likes like = likesService.getOne(new LambdaQueryWrapper<Likes>()
                .eq(Likes::getUserId, userId)
                .eq(Likes::getResourceId, articleId)
        );
        if (like != null) {
            throw new BusinessException(ErrorCode.RESOURCE_ALREADY_LIKED);
        }
        // 点赞
        Likes newLike = new Likes();
        newLike.setUserId(userId);
        newLike.setResourceId(articleId);
        likesService.save(newLike);
        resource.setLikeCount(resource.getLikeCount() + 1);
        updateById(resource);

        // todo: 触发计分,用于推荐系统
    }

    @Override
    public void unlikeArticle(Long articleId) {
        log.info("取消点赞文章: articleId={}", articleId);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Resources resource = getById(articleId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        Likes like = likesService.getOne(new LambdaQueryWrapper<Likes>()
                .eq(Likes::getUserId, userId)
                .eq(Likes::getResourceId, articleId)
        );
        if (like == null) {
            throw new BusinessException(ErrorCode.RESOURCE_ALREADY_UNLIKED);
        }
        likesService.removeById(like.getId());
        resource.setLikeCount(resource.getLikeCount() - 1);
        updateById(resource);

        // todo: 触发计分,用于推荐系统
    }

    @Override
    public List<RootCommentVO> listArticleComments(Long articleId, Integer page, Integer limit) {
        log.info("获取文章根评论列表: articleId={}, page={}, limit={}", articleId, page, limit);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Resources resource = getById(articleId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        if (resource.getVisibility() == 0 && !resource.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }
        // 查询根评论(preId为null)
        List<RootCommentVO> comments = commentsService.listRootComments(articleId, (page - 1) * limit, limit);
        return comments;
    }

    @Override
    public List<SubCommentVO> listCommentReplies(Long commentId, Integer page, Integer limit) {
        log.info("获取评论回复列表: commentId={}, page={}, limit={}", commentId, page, limit);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 直接查询,父评论删除不影响子评论
        List<SubCommentVO> comments = commentsService.listSubComments(commentId, (page - 1) * limit, limit);
        return comments;
    }

    @Override
    public RootCommentVO createComment(Long articleId, CommentCreateRequest commentRequest) {
        log.info("发表评论: articleId={}, request={}", articleId, commentRequest);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 1. 验证文章是否存在
        Resources resource = getById(articleId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        if (resource.getVisibility() == 0 && !resource.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }

        // 2. 构造评论
        Comments comment = new Comments();
        comment.setResourceId(articleId);
        comment.setUserId(userId);
        comment.setContent(commentRequest.getContent());
        comment.setPreId(commentRequest.getParentId());
        if (commentRequest.getParentId() != null) {
            Comments preComment = commentsService.getById(commentRequest.getParentId());
            if (preComment == null) {
                throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
            }
            comment.setRootId(preComment.getRootId() == null ? preComment.getId() : preComment.getRootId());
            preComment.setReplyCount(preComment.getReplyCount() + 1);
            if(preComment.getRootId() != null) {
                Comments rootComment = commentsService.getById(preComment.getRootId());
                if (rootComment != null) {
                    rootComment.setReplyCount(rootComment.getReplyCount() + 1);
                    commentsService.updateById(rootComment);
                }
            }
            commentsService.updateById(preComment);
        }
        commentsService.save(comment);
        resource.setCommentCount(resource.getCommentCount() + 1);
        updateById(resource);
        RootCommentVO rootCommentVO = new RootCommentVO();
        rootCommentVO.setId(comment.getId());
        rootCommentVO.setContent(comment.getContent());
        rootCommentVO.setCreatedAt(comment.getCreatedAt());
        rootCommentVO.setUserId(comment.getUserId());
        rootCommentVO.setReplyCount(comment.getReplyCount());
        rootCommentVO.setStatus(comment.getStatus());
        Users user = usersMapper.selectById(comment.getUserId());
        if (user != null) {
            rootCommentVO.setNickname(user.getNickname());
            rootCommentVO.setAvatarUrl(user.getAvatarUrl());
        }
        return rootCommentVO;

    }

    @Override
    public void deleteComment(Long commentId) {
        log.info("删除评论: commentId={}", commentId);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Comments comment = commentsService.getById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (comment.getUserId().equals(userId)) {
            Resources resource = getById(comment.getResourceId());
            if (resource != null) {
                resource.setCommentCount(resource.getCommentCount() - 1);
                updateById(resource);
            }
            if (comment.getPreId() != null) {
                Comments preComment = commentsService.getById(comment.getPreId());
                if (preComment != null) {
                    preComment.setReplyCount(preComment.getReplyCount() - 1);
                    commentsService.updateById(preComment);
                }
            }
            if (comment.getRootId() != null&&!comment.getRootId().equals(comment.getId())) {
                Comments rootComment = commentsService.getById(comment.getRootId());
                if (rootComment != null) {
                    rootComment.setReplyCount(rootComment.getReplyCount() - 1);
                    commentsService.updateById(rootComment);
                }
            }
            commentsService.removeById(commentId);

        } else {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }
    }

    @Override
    public void createSubmission(Object submissionRequest) {
        log.info("提交投稿: request={}", submissionRequest);
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public List<PublicResourceVO> listExploreArticles() {
        log.info("获取推荐文章列表");
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Data
    public static class VisibilityRequest {
        private Integer visibility;
    }

    @Data
    public static class StatusRequest {
        private Integer status;
    }

    /**
     * 验证知识库访问权限（检查知识库是否存在且未被删除）
     */
    private void validateKnowledgeBaseAccess(Long kbId, Long userId) {
        knowledgeBaseValidationService.validateKnowledgeBaseAccess(kbId, userId);
    }
}