package com.pickyboy.yuquebackend.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.yuquebackend.common.exception.BusinessException;
import com.pickyboy.yuquebackend.common.exception.ErrorCode;
import com.pickyboy.yuquebackend.common.utils.CurrentHolder;
import com.pickyboy.yuquebackend.domain.dto.knowledgebase.InsertKnowledgeBaseRequest;
import com.pickyboy.yuquebackend.domain.entity.KnowledgeBases;
import com.pickyboy.yuquebackend.domain.entity.Resources;
import com.pickyboy.yuquebackend.domain.vo.knowledgebase.KbsWithRecentResourceVo;
import com.pickyboy.yuquebackend.domain.vo.knowledgebase.TrashVO;
import com.pickyboy.yuquebackend.domain.vo.resource.ResourceTreeVo;
import com.pickyboy.yuquebackend.mapper.KnowledgeBasesMapper;
import com.pickyboy.yuquebackend.service.IKnowledgeBaseService;
import com.pickyboy.yuquebackend.service.IResourceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 知识库服务实现类
 *
 * @author pickyboy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBasesMapper, KnowledgeBases> implements IKnowledgeBaseService {

    private final IResourceService resourceService;

    /*
        获取当前用户的知识库列表
        @param withRecentResources 是否包含最近资源
        @return 知识库列表
     */
    @Override
    public List<KbsWithRecentResourceVo> getUserKnowledgeBases(boolean withRecentResources) {
        log.info("获取当前用户的知识库列表");

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // MyBatis Plus会自动过滤逻辑删除的数据
        List<KnowledgeBases> knowledgeBases = list(
            new LambdaQueryWrapper<KnowledgeBases>()
                .eq(KnowledgeBases::getUserId, userId)
        );

        List<KbsWithRecentResourceVo> kbsWithRecentResourceVos = knowledgeBases.stream().map(kb -> {
            KbsWithRecentResourceVo kbsWithRecentResourceVo = new KbsWithRecentResourceVo();
            kbsWithRecentResourceVo.setId(kb.getId());
            kbsWithRecentResourceVo.setName(kb.getName());
            kbsWithRecentResourceVo.setIconIndex(kb.getIconIndex());
            kbsWithRecentResourceVo.setVisibility(kb.getVisibility());

            if (withRecentResources) {
                try {
                    List<Resources> recentResources = getRecentResources(kb.getId());
                    kbsWithRecentResourceVo.setRecentResources(recentResources);
                } catch (Exception e) {
                    log.warn("获取知识库最近资源失败: kbId={}", kb.getId(), e);
                    kbsWithRecentResourceVo.setRecentResources(List.of());
                }
            }
            return kbsWithRecentResourceVo;
        }).collect(Collectors.toList());

        return kbsWithRecentResourceVos;
    }

    /*
        获取知识库最近资源,用于与知识库列表一起展示
        @param kbId 知识库ID
        @return 最近资源列表
     */
    @Override
    public List<Resources> getRecentResources(Long kbId) {
        if (kbId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "知识库ID不能为空");
        }

        // 验证知识库是否存在且当前用户有权限访问
        validateKnowledgeBaseAccess(kbId);

        // MyBatis Plus会自动过滤逻辑删除的数据
        return resourceService.list(
            new LambdaQueryWrapper<Resources>()
                .eq(Resources::getKnowledgeBaseId, kbId)
                .orderByDesc(Resources::getUpdatedAt)
                .last("limit 3")
        );
    }

    @Override
    @Transactional
    public boolean createKnowledgeBase(InsertKnowledgeBaseRequest createRequest) {
        log.info("创建新的知识库: name={}", createRequest.getName());

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 检查知识库名称是否重复（用户范围内）
        // MyBatis Plus会自动过滤逻辑删除的数据
        boolean nameExists = exists(
            new LambdaQueryWrapper<KnowledgeBases>()
                .eq(KnowledgeBases::getUserId, userId)
                .eq(KnowledgeBases::getName, createRequest.getName())
        );

        if (nameExists) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_NAME_DUPLICATE);
        }

        KnowledgeBases knowledgeBase = new KnowledgeBases();
        knowledgeBase.setName(createRequest.getName());
        knowledgeBase.setDescription(createRequest.getDescription());
        knowledgeBase.setIconIndex(createRequest.getIconIndex());
        knowledgeBase.setVisibility(createRequest.getVisibility());
        knowledgeBase.setUserId(userId);

        boolean saved = save(knowledgeBase);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "知识库创建失败");
        }

        log.info("知识库创建成功: id={}, name={}", knowledgeBase.getId(), knowledgeBase.getName());
        return true;
    }

    /*
        获取指定知识库的详细信息(用于知识库编辑页面展示和查看他人知识库详细信息)
        会触发知识库访问量增加
        @param kbId 知识库ID
        @return 知识库详细信息
     */
    @Override
    public KnowledgeBases getKnowledgeBase(Long kbId) {
        if (kbId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "知识库ID不能为空");
        }

        // MyBatis Plus会自动过滤逻辑删除的数据
        KnowledgeBases knowledgeBase = getById(kbId);
        if (knowledgeBase == null) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }

        // 检查访问权限
        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            // 未登录用户只能访问公开知识库
            if (!isPublicKnowledgeBase(knowledgeBase)) {
                throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_ACCESS_DENIED);
            }
        } else if (!currentUserId.equals(knowledgeBase.getUserId())) {
            // 其他用户只能访问公开知识库
            if (!isPublicKnowledgeBase(knowledgeBase)) {
                throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_ACCESS_DENIED);
            }
        }

        if(currentUserId != null){
            // 增加访问量
            // 不需要过滤用户自身,方便测试,实现过滤功能后,也可以防止用户自己刷访问量
            // todo: 使用Redis缓存访问量,根据时间窗口,过滤掉重复访问
            knowledgeBase.setViewCount(knowledgeBase.getViewCount() + 1);
            updateById(knowledgeBase);
            // 不回显shareId
            knowledgeBase.setShareId(null);
        }
        return knowledgeBase;
    }

    /*
        获取指定知识库下文档树(点进知识库,或者点进知识库的文档)
        会触发知识库访问量增加
        @param kbId 知识库ID
        @return 知识库下文档树
     */
    @Override
    public List<ResourceTreeVo> getKnowledgeBaseWithDocuments(Long kbId) {
        log.info("获取知识库文档树: kbId={}", kbId);

        // 验证知识库访问权限
        // 会触发知识库访问量增加,所以这里不需要再增加访问量
        getKnowledgeBase(kbId);

        // MyBatis Plus会自动过滤逻辑删除的数据
        List<Resources> resources = resourceService.list(
            new LambdaQueryWrapper<Resources>()
                .eq(Resources::getKnowledgeBaseId, kbId)
        );

        // 1. 将所有资源转换为 ResourceTreeVo，并用 Map 存储，方便快速查找
        Map<Long, ResourceTreeVo> map = resources.stream()
                .map(resource -> {
                    ResourceTreeVo vo = new ResourceTreeVo();
                    vo.setId(resource.getId());
                    vo.setTitle(resource.getTitle());
                    vo.setType(resource.getType());
                    vo.setPreId(resource.getPreId());
                    vo.setChildren(new ArrayList<>()); // 初始化 children 列表
                    return vo;
                })
                .collect(Collectors.toMap(ResourceTreeVo::getId, vo -> vo));

        // 2. 再次遍历，将每个节点放入其父节点的 children 列表中
        List<ResourceTreeVo> rootNodes = new ArrayList<>();
        map.values().forEach(node -> {
            Long preId = node.getPreId();
            if (preId == null) {
                // 如果 preId 是 null，说明是根节点
                rootNodes.add(node);
            } else {
                // 如果不是根节点，就从 map 中找到它的父节点
                ResourceTreeVo parent = map.get(preId);
                if (parent != null) {
                    // 将当前节点加入父节点的 children 列表
                    parent.getChildren().add(node);
                }
            }
        });

        return rootNodes;
    }

    @Override
    @Transactional
    public boolean updateKnowledgeBase(Long kbId, InsertKnowledgeBaseRequest updateRequest) {
        log.info("更新知识库: kbId={}, request={}", kbId, updateRequest);

        if (kbId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "知识库ID不能为空");
        }

        // 验证知识库是否存在且当前用户有权限修改
        validateKnowledgeBaseOwnership(kbId);

        KnowledgeBases knowledgeBase = getById(kbId);

        // 如果要修改名称，检查是否重复
        if (updateRequest.getName() != null && !updateRequest.getName().equals(knowledgeBase.getName())) {
            // MyBatis Plus会自动过滤逻辑删除的数据
            boolean nameExists = exists(
                new LambdaQueryWrapper<KnowledgeBases>()
                    .eq(KnowledgeBases::getUserId, knowledgeBase.getUserId())
                    .eq(KnowledgeBases::getName, updateRequest.getName())
                    .ne(KnowledgeBases::getId, kbId)
            );

            if (nameExists) {
                throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_NAME_DUPLICATE);
            }
        }

        // 更新字段
        if (updateRequest.getName() != null) {
            knowledgeBase.setName(updateRequest.getName());
        }
        if (updateRequest.getDescription() != null) {
            knowledgeBase.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getIconIndex() != null) {
            knowledgeBase.setIconIndex(updateRequest.getIconIndex());
        }
        if (updateRequest.getVisibility() != null) {
            knowledgeBase.setVisibility(updateRequest.getVisibility());
        }

        boolean updated = updateById(knowledgeBase);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "知识库更新失败");
        }

        log.info("知识库更新成功: kbId={}", kbId);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteKnowledgeBase(Long kbId) {
        log.info("删除知识库: kbId={}", kbId);

        if (kbId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "知识库ID不能为空");
        }

        // 验证知识库是否存在且当前用户有权限删除
        validateKnowledgeBaseOwnership(kbId);

        // 先删除知识库下的所有资源（逻辑删除）
        List<Resources> resources = resourceService.list(
            new LambdaQueryWrapper<Resources>()
                .eq(Resources::getKnowledgeBaseId, kbId)
        );

        if (!resources.isEmpty()) {
            List<Long> resourceIds = resources.stream()
                .map(Resources::getId)
                .collect(Collectors.toList());

            // 批量逻辑删除资源
            resourceService.removeByIds(resourceIds);
            log.info("已删除知识库下的{}个资源", resourceIds.size());
        }

        // 删除知识库（逻辑删除）
        boolean deleted = removeById(kbId);
        if (!deleted) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_DELETE_FAILED);
        }

        log.info("知识库删除成功: kbId={}", kbId);
        return true;
    }

    @Override
    public boolean updateKnowledgeBaseVisibility(Long kbId, Integer visibility) {
        log.info("更新知识库可见性: kbId={}, visibility={}", kbId, visibility);

        if (kbId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "知识库ID不能为空");
        }

        if (visibility == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "可见性设置不能为空");
        }

        // 验证知识库是否存在且当前用户有权限修改
        validateKnowledgeBaseOwnership(kbId);

        KnowledgeBases knowledgeBase = getById(kbId);
        knowledgeBase.setVisibility(visibility);

        boolean updated = updateById(knowledgeBase);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "知识库可见性更新失败");
        }

        log.info("知识库可见性更新成功: kbId={}", kbId);
        return true;
    }

    @Override
    @Transactional
    public void restoreKnowledgeBase(Long kbId) {
        log.info("恢复知识库: kbId={}", kbId);

        if (kbId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "知识库ID不能为空");
        }

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        KnowledgeBases knowledgeBase = getById(kbId);
        if (knowledgeBase == null) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }

        // 检查权限
        if (!userId.equals(knowledgeBase.getUserId())) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_ACCESS_DENIED);
        }

        knowledgeBase.setIsDeleted(false);
        boolean updated = updateById(knowledgeBase);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "知识库恢复失败");
        }

        log.info("知识库恢复成功: kbId={}", kbId);
    }

    @Override
    public TrashVO getTrashContent() {
        log.info("获取回收站内容列表");
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 获取已删除的知识库
        List<KnowledgeBases> deletedKnowledgeBases = list(
            new LambdaQueryWrapper<KnowledgeBases>()
                .eq(KnowledgeBases::getUserId, userId)
                .eq(KnowledgeBases::getIsDeleted, true)
        );

        // 获取用户的所有未删除知识库的ID列表
        List<Long> existingKbIds = list(
            new LambdaQueryWrapper<KnowledgeBases>()
                .eq(KnowledgeBases::getUserId, userId)
                .eq(KnowledgeBases::getIsDeleted, false)
        ).stream().map(KnowledgeBases::getId).collect(Collectors.toList());

        // 获取文档：只显示知识库还存在，但文档自身被删除的文档
        List<Resources> deletedResources;
        if (existingKbIds.isEmpty()) {
            // 如果用户没有任何未删除的知识库，则回收站文档列表为空
            deletedResources = List.of();
        } else {
            // 查找属于未删除知识库的已删除文档
            deletedResources = resourceService.getBaseMapper().selectList(
                new LambdaQueryWrapper<Resources>()
                    .eq(Resources::getUserId, userId)
                    .eq(Resources::getIsDeleted, true)
                    .in(Resources::getKnowledgeBaseId, existingKbIds)
            );
        }

        TrashVO trashVO = new TrashVO();
        trashVO.setKnowledgeBases(deletedKnowledgeBases);
        trashVO.setResources(deletedResources);

        log.info("回收站内容: 知识库{}个, 文档{}个", deletedKnowledgeBases.size(), deletedResources.size());
        return trashVO;
    }

    @Override
    public List<KnowledgeBases> getUserPublicKnowledgeBases(Long userId) {
        log.info("获取用户公开知识库: userId={}", userId);

        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }

        // MyBatis Plus会自动过滤逻辑删除的数据
        return list(
            new LambdaQueryWrapper<KnowledgeBases>()
                .eq(KnowledgeBases::getUserId, userId)
                .eq(KnowledgeBases::getVisibility, 1) // 1表示公开
        );
    }

    /**
     * 验证知识库访问权限
     */
    private void validateKnowledgeBaseAccess(Long kbId) {
        // MyBatis Plus会自动过滤逻辑删除的数据
        KnowledgeBases knowledgeBase = getById(kbId);
        if (knowledgeBase == null) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }

        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            // 未登录用户只能访问公开知识库
            if (!isPublicKnowledgeBase(knowledgeBase)) {
                throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_ACCESS_DENIED);
            }
        } else if (!currentUserId.equals(knowledgeBase.getUserId())) {
            // 其他用户只能访问公开知识库
            if (!isPublicKnowledgeBase(knowledgeBase)) {
                throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_ACCESS_DENIED);
            }
        }
    }

    /**
     * 验证知识库所有权
     */
    private void validateKnowledgeBaseOwnership(Long kbId) {
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // MyBatis Plus会自动过滤逻辑删除的数据
        KnowledgeBases knowledgeBase = getById(kbId);
        if (knowledgeBase == null) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }

        if (!userId.equals(knowledgeBase.getUserId())) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_ACCESS_DENIED);
        }
    }

    /**
     * 判断是否为公开知识库
     */
    private boolean isPublicKnowledgeBase(KnowledgeBases knowledgeBase) {
        return knowledgeBase != null && Integer.valueOf(1).equals(knowledgeBase.getVisibility());
    }
}