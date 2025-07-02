package com.pickyboy.blingBackend.service;

import com.pickyboy.blingBackend.common.exception.BusinessException;
import com.pickyboy.blingBackend.domain.entity.KnowledgeBases;


/**
 * 知识库验证服务接口
 * 用于解决 KnowledgeBaseService 和 ResourceService 之间的循环依赖
 *
 * @author pickyboy
 */
public interface IKnowledgeBaseValidationService {

    /**
     * 验证知识库是否存在且用户有访问权限
     *
     * @param kbId 知识库ID
     * @param userId 用户ID
     * @throws BusinessException 如果知识库不存在或用户无权限访问
     */
    void validateKnowledgeBaseAccess(Long kbId, Long userId);

    /**
     * 验证知识库是否存在且用户有所有者权限
     *
     * @param kbId 知识库ID
     * @param userId 用户ID
     * @throws BusinessException 如果知识库不存在或用户非所有者
     */
    void validateKnowledgeBaseOwnership(Long kbId, Long userId);

    /**
     * 根据ID获取知识库（仅用于验证目的）
     * 不包含复杂的业务逻辑，避免循环依赖
     *
     * @param kbId 知识库ID
     * @return 知识库实体
     */
    KnowledgeBases getKnowledgeBaseById(Long kbId);

    /**
     * 判断知识库是否为公开
     *
     * @param knowledgeBase 知识库实体
     * @return 是否公开
     */
    boolean isPublicKnowledgeBase(KnowledgeBases knowledgeBase);
}