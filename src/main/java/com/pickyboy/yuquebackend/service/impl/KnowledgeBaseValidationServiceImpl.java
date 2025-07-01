package com.pickyboy.yuquebackend.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.yuquebackend.common.exception.BusinessException;
import com.pickyboy.yuquebackend.common.exception.ErrorCode;
import com.pickyboy.yuquebackend.domain.entity.KnowledgeBases;
import com.pickyboy.yuquebackend.mapper.KnowledgeBasesMapper;
import com.pickyboy.yuquebackend.service.IKnowledgeBaseValidationService;

import lombok.extern.slf4j.Slf4j;

/**
 * 知识库验证服务实现类
 * 用于解决循环依赖问题
 *
 * @author pickyboy
 */
@Slf4j
@Service
public class KnowledgeBaseValidationServiceImpl extends ServiceImpl<KnowledgeBasesMapper, KnowledgeBases>
        implements IKnowledgeBaseValidationService {

    @Override
    public void validateKnowledgeBaseAccess(Long kbId, Long userId) {
        log.debug("验证知识库访问权限: kbId={}, userId={}", kbId, userId);

        if (kbId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "知识库ID不能为空");
        }

        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        KnowledgeBases knowledgeBase = getById(kbId);
        if (knowledgeBase == null) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }

        // 检查访问权限：所有者或公开知识库
        if (!userId.equals(knowledgeBase.getUserId()) && !isPublicKnowledgeBase(knowledgeBase)) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_ACCESS_DENIED);
        }
    }

    @Override
    public void validateKnowledgeBaseOwnership(Long kbId, Long userId) {
        log.debug("验证知识库所有权: kbId={}, userId={}", kbId, userId);

        if (kbId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "知识库ID不能为空");
        }

        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        KnowledgeBases knowledgeBase = getById(kbId);
        if (knowledgeBase == null) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }

        // 检查所有权
        if (!userId.equals(knowledgeBase.getUserId())) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_ACCESS_DENIED);
        }
    }

    @Override
    public KnowledgeBases getKnowledgeBaseById(Long kbId) {
        if (kbId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "知识库ID不能为空");
        }

        KnowledgeBases knowledgeBase = getById(kbId);
        if (knowledgeBase == null) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }

        return knowledgeBase;
    }

    @Override
    public boolean isPublicKnowledgeBase(KnowledgeBases knowledgeBase) {
        return knowledgeBase != null && Integer.valueOf(1).equals(knowledgeBase.getVisibility());
    }
}