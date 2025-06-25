package com.pickyboy.yuquebackend.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.yuquebackend.common.context.UserContext;
import com.pickyboy.yuquebackend.domain.dto.InsertKnowledgeBaseRequest;
import com.pickyboy.yuquebackend.domain.entity.KnowledgeBases;
import com.pickyboy.yuquebackend.domain.entity.Resources;
import com.pickyboy.yuquebackend.domain.vo.KbsWithRecentResourceVo;
import com.pickyboy.yuquebackend.domain.vo.ResourceTreeVo;
import com.pickyboy.yuquebackend.domain.vo.TrashVO;
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

    private final UserContext userContext;
    private final IResourceService resourceService;
    @Override
    public List<KbsWithRecentResourceVo> getUserKnowledgeBases(boolean withRecentResources) {
        log.info("获取当前用户的知识库列表");
        List<KnowledgeBases> knowledgeBases = list(new LambdaQueryWrapper<KnowledgeBases>().eq(KnowledgeBases::getUserId, userContext.getUserId()));
        List<KbsWithRecentResourceVo> kbsWithRecentResourceVos = knowledgeBases.stream().map(kb -> {
            KbsWithRecentResourceVo kbsWithRecentResourceVo = new KbsWithRecentResourceVo();
            kbsWithRecentResourceVo.setId(kb.getId());
            kbsWithRecentResourceVo.setName(kb.getName());
            kbsWithRecentResourceVo.setIconIndex(kb.getIconIndex());
            kbsWithRecentResourceVo.setVisibility(kb.getVisibility());
            if (withRecentResources) {
                List<Resources> recentResources = getRecentResources(kb.getId());
                kbsWithRecentResourceVo.setRecentResources(recentResources);
            }
            return kbsWithRecentResourceVo;
        }).collect(Collectors.toList());
        return kbsWithRecentResourceVos;
    }

    private List<Resources> getRecentResources(Long kbId) {
        return resourceService.list(new LambdaQueryWrapper<Resources>().eq(Resources::getKnowledgeBaseId, kbId).orderByDesc(Resources::getUpdatedAt).last("limit 3").eq(Resources::getIsDeleted, false));
    }

    @Override
    public boolean createKnowledgeBase(InsertKnowledgeBaseRequest createRequest) {

        log.info("创建新的知识库: name={}", createRequest.getName());
        KnowledgeBases knowledgeBase = new KnowledgeBases();
        knowledgeBase.setName(createRequest.getName());
        knowledgeBase.setDescription(createRequest.getDescription());
        knowledgeBase.setIconIndex(createRequest.getIconIndex());
        knowledgeBase.setVisibility(createRequest.getVisibility());
        knowledgeBase.setUserId(userContext.getUserId());
        return save(knowledgeBase);
    }

    @Override
    public KnowledgeBases getKnowledgeBase(Long kbId) {
        return getById(kbId);
    }


    @Override
    public List<ResourceTreeVo> getKnowledgeBaseWithDocuments(Long kbId) {
        log.info("获取知识库文档树: kbId={}", kbId);
        List<Resources> resources = resourceService.list(new LambdaQueryWrapper<Resources>().eq(Resources::getKnowledgeBaseId, kbId).eq(Resources::getIsDeleted, false));
/*         List<ResourceTreeVo> resourceTreeVos = resources.stream().filter(resource -> resource.getPreId() == null).map(resource -> {
            ResourceTreeVo resourceTreeVo = new ResourceTreeVo();
            resourceTreeVo.setId(resource.getId());
            resourceTreeVo.setTitle(resource.getTitle());
            resourceTreeVo.setType(resource.getType());
            resourceTreeVo.setPreId(resource.getPreId());
            return resourceTreeVo;
        }).collect(Collectors.toList());
        resourceTreeVos.forEach(resourceTreeVo -> {
            resourceTreeVo.setChildren(getChildren(resources, resourceTreeVo.getId()));
        });
        return resourceTreeVos;
    }

    private List<ResourceTreeVo> getChildren(List<Resources> resources, Long preId) {
        List<Resources> childResources = resources.stream().filter(resource -> resource.getPreId() != null && resource.getPreId().equals(preId)).collect(Collectors.toList());
        return childResources.stream().map(resource -> {
            ResourceTreeVo resourceTreeVo = new ResourceTreeVo();
            resourceTreeVo.setId(resource.getId());
            resourceTreeVo.setTitle(resource.getTitle());
            resourceTreeVo.setType(resource.getType());
            resourceTreeVo.setPreId(resource.getPreId());
            if(hasChildren(resources, resource.getId())){
                resourceTreeVo.setChildren(getChildren(resources, resource.getId()));
            }
            return resourceTreeVo;
        }).collect(Collectors.toList());
    }


    private boolean hasChildren(List<Resources> resources, Long preId) {
        // 过滤掉preId为null的资源,避免空指针异常
        return resources.stream().filter(resource -> resource.getPreId() != null).anyMatch(resource -> resource.getPreId().equals(preId));
    }
    */
// 优化版本
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
    public KnowledgeBases updateKnowledgeBase(Long kbId, InsertKnowledgeBaseRequest updateRequest) {
        // TODO: 实现更新知识库逻辑
        log.info("更新知识库信息: kbId={}", kbId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public void deleteKnowledgeBase(Long kbId) {
        // TODO: 实现删除知识库逻辑（逻辑删除）
        log.info("删除知识库: kbId={}", kbId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public void restoreKnowledgeBase(Long kbId) {
        // TODO: 实现恢复知识库逻辑
        log.info("从回收站恢复知识库: kbId={}", kbId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public List<Resources> getRecentDocuments(Long kbId) {
        // TODO: 实现获取最近编辑文档逻辑
        log.info("获取知识库下最近编辑的文档: kbId={}", kbId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public TrashVO getTrashContent() {
        // TODO: 实现获取回收站内容逻辑
        log.info("获取回收站内容列表");
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public List<KnowledgeBases> getUserPublicKnowledgeBases(Long userId) {
        log.info("获取用户公开知识库: userId={}", userId);
        return list(new LambdaQueryWrapper<KnowledgeBases>().eq(KnowledgeBases::getUserId, userId).eq(KnowledgeBases::getVisibility, true).orderByDesc(KnowledgeBases::getCreatedAt).last("limit 3"));
    }
}