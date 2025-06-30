package com.pickyboy.yuquebackend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.yuquebackend.common.exception.BusinessException;
import com.pickyboy.yuquebackend.common.exception.ErrorCode;
import com.pickyboy.yuquebackend.common.utils.CurrentHolder;
import com.pickyboy.yuquebackend.domain.dto.resource.CopyResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.CreateResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.MoveResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.UpdateResourceContentRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.UpdateResourceInfoRequest;
import com.pickyboy.yuquebackend.domain.entity.Resources;
import com.pickyboy.yuquebackend.domain.vo.resource.PublicResourceVO;
import com.pickyboy.yuquebackend.domain.vo.resource.ShareUrlVO;
import com.pickyboy.yuquebackend.mapper.ResourcesMapper;
import com.pickyboy.yuquebackend.service.IResourceService;
import com.pickyboy.yuquebackend.service.IResourceVersionsService;

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

    @Override
    public Resources createResource(Long kbId, CreateResourceRequest createRequest) {
        log.info("在知识库中新建资源: kbId={}, request={}", kbId, createRequest);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
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
        if (resource.getUserId() != userId && resource.getVisibility() == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }
        // 增加访问量
        resource.setViewCount(resource.getViewCount() + 1);
        updateById(resource);
        return resource;
    }

    @Override
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
        if (resource.getUserId() != userId) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }
        if (updateRequest.getTitle() != null) {
            resource.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getContent() != null) {
            String oldContent = resource.getContent();
            String newContent = updateRequest.getContent();
            // 如果更新了内容，则创建资源版本
            if (!oldContent.equals(newContent)) {
                resourceVersionsService.createResourceVersion(resource.getId(), oldContent);
            }
            resource.setContent(newContent);
        }
        updateById(resource);
    }

    @Override
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
        if (resource.getUserId() != userId) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }
        // 配置了逻辑删除，所以直接删除
        removeById(resId);
    }

    @Override
    public void renameResource(Long resId, UpdateResourceInfoRequest infoRequest) {
        log.info("重命名资源: resId={}, request={}", resId, infoRequest);
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public void updateResourceVisibility(Long resId, Object visibilityRequest) {
        log.info("更新资源可见性: resId={}, request={}", resId, visibilityRequest);
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public void updateResourceStatus(Long resId, Object statusRequest) {
        log.info("更新资源状态: resId={}, request={}", resId, statusRequest);
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public void restoreResource(Long resId) {
        log.info("从回收站恢复资源: resId={}", resId);
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public void permanentlyDeleteResource(Long resId) {
        log.info("彻底删除资源: resId={}", resId);
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public void moveResource(Long resId, MoveResourceRequest moveRequest) {
        log.info("移动资源: resId={}, request={}", resId, moveRequest);
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public void copyResource(Long resId, CopyResourceRequest copyRequest) {
        log.info("复制资源: resId={}, request={}", resId, copyRequest);
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public void copyResourceTree(Long resId, CopyResourceRequest copyRequest) {
        log.info("复制目录树: resId={}, request={}", resId, copyRequest);
        throw new UnsupportedOperationException("此方法尚未实现");
    }

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
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public void unlikeArticle(Long articleId) {
        log.info("取消点赞文章: articleId={}", articleId);
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public List<?> listArticleComments(Long articleId) {
        log.info("获取文章评论列表: articleId={}", articleId);
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public Object createComment(Long articleId, Object commentRequest) {
        log.info("发表评论: articleId={}", articleId);
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public void deleteComment(Long commentId) {
        log.info("删除评论: commentId={}", commentId);
        throw new UnsupportedOperationException("此方法尚未实现");
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

    @Override
    public List<?> getResourceTree(Long kbId) {
        log.info("获取知识库资源目录树: kbId={}", kbId);
        throw new UnsupportedOperationException("此方法尚未实现");
    }
}