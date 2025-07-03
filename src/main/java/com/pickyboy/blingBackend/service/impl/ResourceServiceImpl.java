package com.pickyboy.blingBackend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.blingBackend.common.constants.RedisKeyConstants;
import com.pickyboy.blingBackend.common.constants.ResourceTypeConstants;
import com.pickyboy.blingBackend.common.exception.BusinessException;
import com.pickyboy.blingBackend.common.exception.ErrorCode;
import com.pickyboy.blingBackend.common.utils.CurrentHolder;
import com.pickyboy.blingBackend.common.utils.MinioUtil;
import com.pickyboy.blingBackend.common.utils.RedisUtil;
import com.pickyboy.blingBackend.domain.dto.comment.CommentCreateRequest;
import com.pickyboy.blingBackend.domain.dto.resource.CopyResourceRequest;
import com.pickyboy.blingBackend.domain.dto.resource.CreateResourceRequest;
import com.pickyboy.blingBackend.domain.dto.resource.MoveResourceRequest;
import com.pickyboy.blingBackend.domain.dto.resource.RestoreResourceRequest;
import com.pickyboy.blingBackend.domain.dto.resource.UpdateResourceContentRequest;
import com.pickyboy.blingBackend.domain.dto.resource.UpdateResourceInfoRequest;
import com.pickyboy.blingBackend.domain.entity.Comments;
import com.pickyboy.blingBackend.domain.entity.Likes;
import com.pickyboy.blingBackend.domain.entity.Resources;
import com.pickyboy.blingBackend.domain.entity.Users;
import com.pickyboy.blingBackend.domain.entity.ViewHistories;
import com.pickyboy.blingBackend.domain.vo.comment.RootCommentVO;
import com.pickyboy.blingBackend.domain.vo.comment.SubCommentVO;
import com.pickyboy.blingBackend.domain.vo.resource.PublicResourceVO;
import com.pickyboy.blingBackend.domain.vo.resource.ShareUrlVO;
import com.pickyboy.blingBackend.mapper.CommentsMapper;
import com.pickyboy.blingBackend.mapper.ResourcesMapper;
import com.pickyboy.blingBackend.mapper.UsersMapper;
import com.pickyboy.blingBackend.mapper.ViewHistoriesMapper;
import com.pickyboy.blingBackend.service.ICommentsService;
import com.pickyboy.blingBackend.service.IKnowledgeBaseValidationService;
import com.pickyboy.blingBackend.service.ILikesService;
import com.pickyboy.blingBackend.service.IResourceService;
import com.pickyboy.blingBackend.service.IResourceVersionsService;

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
    private final CommentsMapper commentsMapper;
    private final UsersMapper usersMapper;
    private final RedisUtil redisUtil;
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
        // 检查知识库所有权：只能向自己的知识库添加资源
        knowledgeBaseValidationService.validateKnowledgeBaseOwnership(kbId, userId);

        Resources resource = new Resources();
        resource.setKnowledgeBaseId(kbId);
        resource.setUserId(userId);
        resource.setTitle(createRequest.getTitle()==null?"无标题":createRequest.getTitle());
        resource.setType(createRequest.getType());
        resource.setPreId(createRequest.getPreId());
        if(resource.getType().equals(ResourceTypeConstants.RESOURCE_TYPE_FOLDER)){
            resource.setVisibility(1); // 目录默认公开,跟随知识库可见性
        }
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

        // 【重构】使用带权限验证的JOIN查询，同时检查资源和知识库的可见性
        Resources resource = baseMapper.selectResourceInActiveKbWithUser(resId, userId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在、其知识库已被删除或无访问权限");
        }
        if(resource.getType().equals(ResourceTypeConstants.RESOURCE_TYPE_FOLDER)){
            // 目录去掉内容直接返回
            resource.setContent(null);
            resource.setStatus(-1);
            resource.setLikeCount(null);
            resource.setCommentCount(null);
            resource.setViewCount(null);
            resource.setFavoriteCount(null);
            return resource;
        }
        // 异步记录浏览历史
        recordViewHistoryAsync(userId, resId);

        // 【修复并发问题+性能优化】原子操作更新访问量

        if(!redisUtil.hasKey(RedisKeyConstants.getResourceViewKey(resId, userId))){
            redisUtil.set(RedisKeyConstants.getResourceViewKey(resId, userId), true,30, TimeUnit.MINUTES);
            baseMapper.incrementViewCount(resId);
            log.info("用户访问了资源,更新了访问量: resId={},userId={}", resId,userId);
        }
        // TODO: 引入kafka,批处理访问量更新,计算热力值

        return resource;
    }

    /**
     * 异步记录浏览历史
     *  已存在则更新时间
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

        // 【重构】使用JOIN查询直接验证资源及其知识库状态
        Resources resource = baseMapper.selectResourceInActiveKb(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在或其知识库已被删除");
        }

        // 权限验证：只有资源所有者可以修改
        if (!resource.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }

        if (updateRequest.getTitle() != null) {
            resource.setTitle(updateRequest.getTitle());
        }
        // 目录不支持更新内容,自然不创建版本记录
        if (updateRequest.getContent() != null&&!resource.getType().equals(ResourceTypeConstants.RESOURCE_TYPE_FOLDER)) {
            String oldContent = resource.getContent();
            String newContent = updateRequest.getContent();
            // 每次更新内容都创建资源版本记录
            if (oldContent==null||!oldContent.equals(newContent)) {
                log.info("创建版本记录: resId={}, content={}", resource.getId(), newContent);
                resourceVersionsService.createResourceVersion(resource.getId(), newContent);
                resource.setContent(newContent);
            }
            else {
                log.info("内容路径未发生变化,不更新内容,不创建版本记录: resId={}, content={}", resource.getId(), newContent);
            }
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

        // 【重构】使用JOIN查询直接验证资源及其知识库状态
        Resources resource = baseMapper.selectResourceInActiveKb(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在或其知识库已被删除");
        }

        // 权限验证：只有资源所有者可以删除
        if (!resource.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }

        // 递归删除当前资源及其子资源
        recursiveDeleteResource(resId);
    }

        /**
     * 【重构后】逻辑删除资源及其整个子树（无论子节点删除状态）
     * 原递归方式：N+1查询问题，存在逻辑错误
     * 优化后：统一查询所有子孙节点，一次性批量删除
     */
    private void recursiveDeleteResource(Long parentId) {
        // 【修正】使用统一递归查询获取所有子孙节点ID（无论删除状态）
        List<Resources> allDescendants = baseMapper.selectAllDescendants(parentId);

        // 将当前节点ID也加入待删除列表
        List<Long> allIdsToDelete = new ArrayList<>();
        allIdsToDelete.add(parentId);

        if (!allDescendants.isEmpty()) {
            List<Long> descendantIds = allDescendants.stream()
                .map(Resources::getId)
                .collect(Collectors.toList());
            allIdsToDelete.addAll(descendantIds);
        }

        // 【优化】执行一次批量的逻辑删除
        if (allIdsToDelete.size() > 1) { // 只有当有子节点时才批量删除
            int deletedCount = baseMapper.batchLogicalDelete(allIdsToDelete);
            log.info("成功逻辑删除了 resId={} 的整个子树，共 {} 个节点", parentId, deletedCount);
        } else {
            // 如果只有当前节点，使用常规删除
            removeById(parentId);
            log.info("已删除资源: resId={}", parentId);
        }
    }

    @Override
    public void renameResource(Long resId, UpdateResourceInfoRequest infoRequest) {
        log.info("重命名资源: resId={}, request={}", resId, infoRequest);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 【重构】使用JOIN查询直接验证资源及其知识库状态
        Resources resource = baseMapper.selectResourceInActiveKb(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在或其知识库已被删除");
        }

        // 权限验证：只有资源所有者可以重命名
        if (!resource.getUserId().equals(userId)) {
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

        // 【重构】使用JOIN查询直接验证资源及其知识库状态
        Resources resource = baseMapper.selectResourceInActiveKb(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在或其知识库已被删除");
        }

        // 目录不支持修改可见性
        if (resource.getType().equals(ResourceTypeConstants.RESOURCE_TYPE_FOLDER)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED, "目录不支持修改可见性");
        }

        // 权限验证：只有资源所有者可以修改可见性
        if (!resource.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }

        resource.setVisibility(vRequest.getVisibility());
        updateById(resource);
    }

    /*
     * 更新资源状态(上架/下架) 0:下架 1:上架 2: 强制下架(管理员)
     */
    @Override
    public void updateResourceStatus(Long resId, Object statusRequest) {
        StatusRequest sRequest = (StatusRequest) statusRequest;
        log.info("更新资源状态: resId={}, request={}", resId, sRequest);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 【重构】使用JOIN查询直接验证资源及其知识库状态
        Resources resource = baseMapper.selectResourceInActiveKb(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在或其知识库已被删除");
        }

        // 目录不支持修改状态
        if (resource.getType().equals(ResourceTypeConstants.RESOURCE_TYPE_FOLDER)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED, "目录不支持修改状态");
        }

        // 被强制下架的资源不能修改状态
        if (resource.getStatus() == 2) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED, "被强制下架的资源不能修改状态");
        }

        // 不能修改资源状态为2(强制下架)
        if (sRequest.getStatus() == 2) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED, "不能修改资源状态为2(强制下架),请联系管理员");
        }

        // 权限验证：只有资源所有者可以修改状态
        if (!resource.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }

        resource.setStatus(sRequest.getStatus());
        updateById(resource);
    }

    @Override
    @Transactional
    public void restoreResource(Long resId, RestoreResourceRequest request) {
        log.info("从回收站恢复资源: resId={}, request={}", resId, request);

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 【重构】使用JOIN查询验证已删除资源及其知识库状态
        Resources resource = baseMapper.selectDeletedResourceInActiveKb(resId, userId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在、未被删除或其知识库已被删除");
        }

        // 【智能恢复】确定恢复位置
        Long targetPreId = determineRestorePosition(resource, request);

        // 使用UpdateWrapper恢复资源
        LambdaUpdateWrapper<Resources> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Resources::getId, resId)
                    .set(Resources::getIsDeleted, false)
                    .set(Resources::getPreId, targetPreId);

        boolean updated = update(updateWrapper);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "资源恢复失败");
        }

        log.info("资源恢复成功: resId={}, targetPreId={}", resId, targetPreId);
    }

    /**
     * 智能确定恢复位置
     * @param resource 要恢复的资源
     * @param request 恢复请求
     * @return 目标父节点ID
     */
    private Long determineRestorePosition(Resources resource, RestoreResourceRequest request) {
        // 1. 如果用户明确指定了恢复位置，使用用户指定的位置
        if (request != null && request.getTargetPreId() != null) {
            Long targetPreId = request.getTargetPreId();

            // 【修正】0L也表示根节点，与null等价
            if (targetPreId.equals(0L)) {
                log.info("用户指定恢复到根节点（传入0）");
                return null; // 恢复到根节点
            }

            // 验证目标父节点是否存在且在同一知识库中
            Resources targetParent = baseMapper.selectResourceInActiveKb(targetPreId);
            if (targetParent == null) {
                log.warn("指定的目标父节点不存在或已被删除，将恢复到根节点: targetPreId={}", targetPreId);
                return null;
            }
            if (!targetParent.getKnowledgeBaseId().equals(resource.getKnowledgeBaseId())) {
                log.warn("指定的目标父节点不在同一知识库中，将恢复到根节点: targetKbId={}, resourceKbId={}",
                        targetParent.getKnowledgeBaseId(), resource.getKnowledgeBaseId());
                return null;
            }

            log.info("使用用户指定的恢复位置: targetPreId={}", targetPreId);
            return targetPreId;
        }

        // 2. 尝试恢复到原位置
        Long originalPreId = resource.getPreId();
        if (originalPreId != null) {
            Resources originalParent = baseMapper.selectResourceInActiveKb(originalPreId);
            if (originalParent != null && originalParent.getKnowledgeBaseId().equals(resource.getKnowledgeBaseId())) {
                log.info("恢复到原始位置: originalPreId={}", originalPreId);
                return originalPreId;
            }
        }

        // 3. 默认恢复到根节点
        log.info("恢复到根节点");
        return null;
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

        // 【重构】使用JOIN查询验证源资源及其知识库状态
        Resources resource = baseMapper.selectResourceInActiveKb(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "源资源不存在或其知识库已被删除");
        }

        // 权限验证：只有资源所有者可以移动
        if (!resource.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }

        // 验证目标知识库是否存在且有权限访问
        knowledgeBaseValidationService.validateKnowledgeBaseOwnership(moveRequest.getTargetKbId(), userId);

        // 验证目标父节点是否存在（如果不为null）
        if (moveRequest.getTargetPreId() != null) {
            // 【重构】使用JOIN查询验证目标父节点及其知识库状态
            Resources targetParent = baseMapper.selectResourceInActiveKb(moveRequest.getTargetPreId());
            if (targetParent == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "目标父节点不存在或其知识库已被删除");
            }
            if (!targetParent.getKnowledgeBaseId().equals(moveRequest.getTargetKbId())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "目标父节点不属于目标知识库");
            }
        }

        // 【重构】判断是否需要更新子资源的知识库ID（跨知识库移动）
        boolean needUpdateChildrenKb = !resource.getKnowledgeBaseId().equals(moveRequest.getTargetKbId());

        // 使用UpdateWrapper来支持将preId设置为null（移动到根节点）
        LambdaUpdateWrapper<Resources> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Resources::getId, resId)
                    .set(Resources::getKnowledgeBaseId, moveRequest.getTargetKbId())
                    .set(Resources::getPreId, moveRequest.getTargetPreId()); // 支持设置为null

        boolean updated = update(updateWrapper);
        if (!updated) {
            throw new BusinessException(ErrorCode.RESOURCE_MOVE_FAILED);
        }

        // 【优化】只有跨知识库移动时才需要更新子资源的知识库ID
        if (needUpdateChildrenKb) {
            updateChildrenKnowledgeBaseId(resId, moveRequest.getTargetKbId());
        }

        log.info("资源移动成功: resId={}, targetKbId={}, targetPreId={}, updatedChildren={}",
                resId, moveRequest.getTargetKbId(), moveRequest.getTargetPreId(), needUpdateChildrenKb);
    }

        /**
     * 【重构后】批量更新子资源的知识库ID（包括已删除的子资源）
     * 原递归方式：移动100个资源需要100+次UPDATE操作，分两次查询低效
     * 优化后：统一查询所有子孙节点，一次性批量更新
     */
    private void updateChildrenKnowledgeBaseId(Long parentId, Long newKbId) {
        // 【修正】使用统一递归查询获取所有子孙节点（无论删除状态）
        List<Resources> allDescendants = baseMapper.selectAllDescendants(parentId);

        if (!allDescendants.isEmpty()) {
            List<Long> descendantIds = allDescendants.stream()
                .map(Resources::getId)
                .collect(Collectors.toList());

            // 【优化】执行一次批量更新，同时处理已删除和未删除的子资源
            int updatedCount = baseMapper.batchUpdateKnowledgeBaseId(descendantIds, newKbId);
            log.info("批量更新所有子孙资源知识库ID: parentId={}, updatedCount={}", parentId, updatedCount);
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

        // 【重构】使用JOIN查询直接验证源资源及其知识库状态
        Resources resource = baseMapper.selectResourceInActiveKb(resId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "源资源不存在或其知识库已被删除");
        }

        // 权限验证：只有资源所有者可以复制
        if (!resource.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }

        // 验证目标知识库是否存在且有权限访问
        knowledgeBaseValidationService.validateKnowledgeBaseOwnership(copyRequest.getTargetKbId(), userId);

        // 目录不复制具体内容
        // 2. 深拷贝文件内容
        String uploadType = "resource";
        String newContentUrl =null;
        if(!resource.getType().equals(ResourceTypeConstants.RESOURCE_TYPE_FOLDER)){
        newContentUrl = minioUtil.copyObject(
            resource.getContent(),      // 源文件URL
            resource.getTitle(),        // 使用原标题作为新文件名
                uploadType,                 // 目标上传类型 (决定存储桶)
                userId.toString()           // 当前用户ID
            );
        }

        // 3. 准备并保存新的资源实体 (这里不能直接修改原resource对象)
        Resources newResource = new Resources();
        newResource.setTitle(resource.getTitle()); // ... 其他属性

        // 设置新的、不同的属性
        newResource.setId(null); // 清除ID，让数据库自动生成
        newResource.setKnowledgeBaseId(copyRequest.getTargetKbId());
        newResource.setPreId(copyRequest.getTargetPreId());
        newResource.setType(resource.getType());
        newResource.setUserId(userId);
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
        // 目录默认公开,跟随知识库可见性
        if(resource.getType().equals(ResourceTypeConstants.RESOURCE_TYPE_FOLDER)){
            newResource.setVisibility(1);
        }
        else {
            newResource.setVisibility(0);
        }
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
     * 【重构后】递归复制子节点（包括回收站中的文件）
     * 原递归方式：复制100个资源需要100+次数据库操作，只复制未删除文件导致结构不完整
     * 优化后：统一查询所有子孙节点，确保复制结构完整
     * @param sourceParentId 源父节点ID
     * @param newParentId 新创建的父节点ID
     * @param targetKbId 目标知识库ID
     */
    private void recursiveCopyChildren(Long sourceParentId, Long newParentId, Long targetKbId) {
        // 【修正】使用统一递归查询获取所有子孙节点（无论删除状态）
        List<Resources> allDescendants = baseMapper.selectAllDescendants(sourceParentId);

        if (allDescendants.isEmpty()) {
            return; // 如果没有子节点，直接返回
        }

        // 存储原ID到新ID的映射关系，用于重建父子关系
        Map<Long, Long> idMapping = new HashMap<>();
        idMapping.put(sourceParentId, newParentId); // 添加根节点映射

        // 【优化】按层级顺序遍历，确保父节点总是先于子节点被创建
        for (Resources child : allDescendants) {
            // 从映射中找到新的父节点ID
            Long newParentForChild = idMapping.get(child.getPreId());

            // 复制当前子节点
            CopyResourceRequest childCopyRequest = new CopyResourceRequest(targetKbId, newParentForChild);
            Resources newChildNode = this.copyResource(child.getId(), childCopyRequest);

            // 记录新的ID映射关系，供后续子节点使用
            idMapping.put(child.getId(), newChildNode.getId());

            log.debug("复制子资源: sourceId={}, newId={}, newParent={}, isDeleted={}",
                     child.getId(), newChildNode.getId(), newParentForChild, child.getIsDeleted());
        }

        log.info("批量复制完成: sourceParent={}, copiedCount={} (包含已删除文件)", sourceParentId, allDescendants.size());
    }

    // todo:
    @Override
    public ShareUrlVO generateResourceShareLink(Long resId) {
        log.info("生成资源分享链接: resId={}", resId);

        // 【重构】未来实现时需要使用JOIN查询验证资源及其知识库状态
        // 1. 验证用户登录和权限
        // 2. 使用selectResourceInActiveKb()验证资源状态
        // 3. 验证用户是否为资源所有者
        // 4. 生成分享链接

        throw new UnsupportedOperationException("此方法尚未实现 - 实现时需要验证知识库状态");
    }

    @Override
    public PublicResourceVO accessSharedResource(String kbShareId, String resShareId) {
        log.info("访问分享资源: kbShareId={}, resShareId={}", kbShareId, resShareId);

        // 【重构】未来实现时需要验证知识库状态
        // 1. 通过shareId查询资源
        // 2. 使用JOIN查询验证资源及其知识库状态:
        //    SELECT r.* FROM resources r
        //    JOIN knowledge_bases kb ON r.knowledge_base_id = kb.id
        //    WHERE r.share_id = #{resShareId} AND kb.share_id = #{kbShareId}
        //      AND r.is_deleted = 0 AND kb.is_deleted = 0
        // 3. 验证分享权限和可见性

        throw new UnsupportedOperationException("此方法尚未实现 - 实现时需要验证知识库状态");
    }

    @Override
    public void likeArticle(Long articleId) {
        log.info("点赞文章: articleId={}", articleId);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 【重构】使用JOIN查询验证资源、知识库状态及用户权限
        Resources resource = baseMapper.selectResourceInActiveKbWithUser(articleId, userId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "文章不存在、其知识库已被删除或无访问权限");
        }
        // 目录不支持点赞
        if (resource.getType().equals(ResourceTypeConstants.RESOURCE_TYPE_FOLDER)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED, "目录不支持点赞");
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

        // 【修复并发问题】原子操作增加资源点赞数
        baseMapper.incrementLikeCount(articleId);

        // todo: 触发计分,用于推荐系统
    }

    @Override
    public void unlikeArticle(Long articleId) {
        log.info("取消点赞文章: articleId={}", articleId);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 【重构】使用JOIN查询验证资源、知识库状态及用户权限
        Resources resource = baseMapper.selectResourceInActiveKbWithUser(articleId, userId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "文章不存在、其知识库已被删除或无访问权限");
        }
        // 目录不支持取消点赞
        if (resource.getType().equals(ResourceTypeConstants.RESOURCE_TYPE_FOLDER)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED, "目录不支持取消点赞");
        }

        Likes like = likesService.getOne(new LambdaQueryWrapper<Likes>()
            .eq(Likes::getUserId, userId)
            .eq(Likes::getResourceId, articleId)
        );
        if (like == null) {
            throw new BusinessException(ErrorCode.RESOURCE_ALREADY_UNLIKED);
        }
        likesService.removeById(like.getId());

        // 【修复并发问题】原子操作减少资源点赞数
        baseMapper.decrementLikeCount(articleId);

        // todo: 触发计分,用于推荐系统
    }

    @Override
    public List<RootCommentVO> listArticleComments(Long articleId, Integer page, Integer limit) {
        log.info("获取文章根评论列表: articleId={}, page={}, limit={}", articleId, page, limit);
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 【重构】使用JOIN查询验证资源、知识库状态及用户权限（包含可见性验证）
        Resources resource = baseMapper.selectResourceInActiveKbWithUser(articleId, userId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "文章不存在、其知识库已被删除或无访问权限");
        }
        // 目录不支持评论
        if (resource.getType().equals(ResourceTypeConstants.RESOURCE_TYPE_FOLDER)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED, "目录不支持评论");
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

        // 【重构】需要通过评论ID间接验证资源及知识库状态和权限
        // 由于需要同时验证资源和知识库的可见性，我们需要：
        // 1. 先获取评论对应的资源ID
        // 2. 使用带权限验证的查询方法

        Comments comment = commentsService.getById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND, "评论不存在");
        }

        // 使用带权限验证的查询，确保同时检查资源和知识库的可见性
        Resources resource = baseMapper.selectResourceInActiveKbWithUser(comment.getResourceId(), userId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "评论对应的资源不存在、其知识库已被删除或无访问权限");
        }

        // 查询评论回复
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

        // 【重构】使用JOIN查询验证资源、知识库状态及用户权限（包含可见性验证）
        Resources resource = baseMapper.selectResourceInActiveKbWithUser(articleId, userId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "文章不存在、其知识库已被删除或无访问权限");
        }
        // 目录不支持评论
        if (resource.getType().equals(ResourceTypeConstants.RESOURCE_TYPE_FOLDER)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED, "目录不支持评论");
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

                        // 【修复并发问题】原子操作增加父评论回复数
            commentsMapper.incrementReplyCount(preComment.getId());

            // 如果有根评论且不是同一个评论，也要增加根评论的回复数
            if(preComment.getRootId() != null) {
                commentsMapper.incrementReplyCount(preComment.getRootId());
            }
        }
        commentsService.save(comment);

        // 【修复并发问题】原子操作增加资源评论数
        baseMapper.incrementCommentCount(articleId);
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

        // 验证评论权限：只有评论作者可以删除
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }

        // 【重构】验证评论对应的资源及知识库状态和可见性
        // 使用带权限验证的查询，确保用户有权限访问该资源
        Resources resource = baseMapper.selectResourceInActiveKbWithUser(comment.getResourceId(), userId);
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "评论对应的资源不存在、其知识库已被删除或无访问权限");
        }

        // 【修复并发问题】原子操作减少资源评论计数
        baseMapper.decrementCommentCount(comment.getResourceId());

        // 【修复并发问题】原子操作更新评论回复计数
        if (comment.getPreId() != null) {
            commentsMapper.decrementReplyCount(comment.getPreId());
        }
        if (comment.getRootId() != null && !comment.getRootId().equals(comment.getId())) {
            commentsMapper.decrementReplyCount(comment.getRootId());
        }
        commentsService.removeById(commentId);
    }

    @Override
    public void createSubmission(Object submissionRequest) {
        log.info("提交投稿: request={}", submissionRequest);
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public List<PublicResourceVO> listExploreArticles(String sortBy, Integer page, Integer limit) {
        log.info("获取推荐文章列表: sortBy={}, page={}, limit={}", sortBy, page, limit);

        // 【重构】未来实现时需要使用JOIN查询过滤已删除知识库的资源
        // 示例SQL:
        // SELECT r.* FROM resources r
        // JOIN knowledge_bases kb ON r.knowledge_base_id = kb.id
        // WHERE r.is_deleted = 0 AND kb.is_deleted = 0
        //   AND r.visibility = 1 AND kb.visibility = 1
        //   AND r.status = 1
        // ORDER BY
        //   CASE WHEN #{sortBy} = 'hot' THEN r.like_count * 0.6 + r.view_count * 0.3 + r.comment_count * 0.1 END DESC,
        //   CASE WHEN #{sortBy} = 'new' THEN r.created_at END DESC
        // LIMIT #{offset}, #{limit}

        throw new UnsupportedOperationException("此方法尚未实现 - 实现时需要使用JOIN查询过滤已删除知识库，支持hot/new排序");
    }

    @Override
    public List<Resources> listDeletedResourcesInActiveKbs(Long userId) {
        // 直接调用Mapper中的自定义方法
        return baseMapper.selectDeletedResourcesInActiveKbs(userId);
    }

    @Override
    @Transactional // 【关键修复】恢复操作涉及多次数据库写操作，必须保证原子性
    public void restoreResourceToVersion(Long resId, Long versionId) {
        log.info("恢复资源到指定版本: resId={}, versionId={}", resId, versionId);

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 1. 验证目标版本是否存在且属于该资源
        com.pickyboy.blingBackend.domain.entity.ResourceVersions targetVersion = resourceVersionsService.getById(versionId);
        if (targetVersion == null || !targetVersion.getResourceId().equals(resId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "指定版本不存在或不属于该资源");
        }

        // 2. 验证资源权限
        Resources resource = baseMapper.selectResourceInActiveKb(resId); // 简化处理，常规场景下已足够
        if (resource == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在或其知识库已被删除");
        }

        if (!resource.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_ACCESS_DENIED);
        }

        String currentContentUrl = resource.getContent();
        String targetContentUrl = targetVersion.getObjectUrl();

        // 3. 如果当前内容与目标内容不同，则将当前内容归档为新版本
        if (currentContentUrl != null && !currentContentUrl.equals(targetContentUrl)) {
            log.info("当前内容与目标版本不同，将当前内容归档: resId={}, currentContent={}", resId, currentContentUrl);
            resourceVersionsService.createResourceVersion(resId, currentContentUrl);
        }

        // 4. 将目标版本的内容URL更新到主资源记录中
        resource.setContent(targetContentUrl);
        boolean updated = updateById(resource);
        if (!updated) {
            throw new BusinessException(ErrorCode.VERSION_RESTORE_FAILED, "恢复版本时更新资源失败");
        }

        // 【优化建议】恢复版本后，不应删除该历史版本记录，以便未来还能再次恢复
        // resourceVersionsService.deleteResourceVersionById(versionId);

        log.info("成功恢复资源到指定版本: resId={}, versionId={}, newContent={}", resId, versionId, targetContentUrl);
    }

    @Data
    public static class VisibilityRequest {
        private Integer visibility;
    }

    @Data
    public static class StatusRequest {
        private Integer status;
    }


}