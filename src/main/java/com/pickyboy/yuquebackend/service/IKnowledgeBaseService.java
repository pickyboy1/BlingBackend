package com.pickyboy.yuquebackend.service;

import java.util.List;

/**
 * 知识库服务接口
 *
 * @author pickyboy
 */
public interface IKnowledgeBaseService {

    /**
     * 获取指定用户的公开知识库列表
     * 用于在用户主页展示其公开的知识库，最多返回3个
     *
     * @param userId 用户ID
     * @return 知识库摘要列表
     */
    List<?> getUserPublicKnowledgeBases(Long userId);
}