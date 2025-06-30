package com.pickyboy.yuquebackend.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pickyboy.yuquebackend.domain.dto.knowledgebase.InsertKnowledgeBaseRequest;
import com.pickyboy.yuquebackend.domain.entity.KnowledgeBases;
import com.pickyboy.yuquebackend.domain.entity.Resources;
import com.pickyboy.yuquebackend.domain.vo.knowledgebase.KbsWithRecentResourceVo;
import com.pickyboy.yuquebackend.domain.vo.knowledgebase.TrashVO;
import com.pickyboy.yuquebackend.domain.vo.resource.ResourceTreeVo;

/**
 * 知识库服务接口
 *
 * @author pickyboy
 */
public interface IKnowledgeBaseService extends IService<KnowledgeBases> {

    /**
     * 获取当前用户的知识库列表
     * 用于侧边栏展示
     *
     * @return 知识库列表
     */
    List<KbsWithRecentResourceVo> getUserKnowledgeBases(boolean withRecentResources);

    /**
     * 创建新的知识库
     *
     * @param createRequest 创建请求
     * @return 创建的知识库信息
     */
    boolean createKnowledgeBase(InsertKnowledgeBaseRequest createRequest);

    /**
     * 获取指定知识库的详细信息
     *
     * @param kbId 知识库ID
     * @return 知识库详细信息
     */
    KnowledgeBases getKnowledgeBase(Long kbId);
    /**
     * 获取指定知识库的详细信息及其文档树
     *
     * @param kbId 知识库ID
     * @return 知识库详细信息及文档树
     */
    List<ResourceTreeVo> getKnowledgeBaseWithDocuments(Long kbId);

    /**
     * 更新知识库信息
     *
     * @param kbId 知识库ID
     * @param updateRequest 更新请求
     * @return 更新后的知识库信息
     */
    boolean updateKnowledgeBase(Long kbId, InsertKnowledgeBaseRequest updateRequest);

    /**
     * 删除知识库 (逻辑删除)
     *
     *
     * @param kbId 知识库ID
     */
    boolean deleteKnowledgeBase(Long kbId);

    /**
     * 从回收站恢复知识库
     *
     * @param kbId 知识库ID
     */
    void restoreKnowledgeBase(Long kbId);

    /**
     * 获取知识库下最近编辑的文档
     * 用于知识库管理页面，获取最近编辑的3个文档
     *
     * @param kbId 知识库ID
     * @return 最近编辑的文档列表
     */
    List<Resources> getRecentResources(Long kbId);

    /**
     * 获取回收站内容列表
     * 获取当前用户所有已逻辑删除的知识库和文档
     *
     * @return 回收站内容
     */
    TrashVO getTrashContent();

    /**
     * 获取指定用户的公开知识库列表
     * 用于在用户主页展示其公开的知识库，最多返回3个
     *
     * @param userId 用户ID
     * @return 知识库摘要列表
     */
    List<KnowledgeBases> getUserPublicKnowledgeBases(Long userId);

    /**
     * 更新知识库可见性
     *
     * @param kbId 知识库ID
     * @param visibility 可见性
     */
    boolean updateKnowledgeBaseVisibility(Long kbId, Integer visibility);
}