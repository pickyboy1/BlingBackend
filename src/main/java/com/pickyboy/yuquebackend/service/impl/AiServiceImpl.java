package com.pickyboy.yuquebackend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.yuquebackend.domain.entity.AiConversations;
import com.pickyboy.yuquebackend.mapper.AiConversationsMapper;
import com.pickyboy.yuquebackend.service.IAiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI服务实现类
 *
 * @author pickyboy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl extends ServiceImpl<AiConversationsMapper, AiConversations> implements IAiService {

    @Override
    public List<?> getConversations() {
        // TODO: 实现获取AI对话历史列表逻辑
        log.info("获取AI对话历史列表");
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public Object createConversation(Object createRequest) {
        // TODO: 实现创建新对话逻辑
        log.info("创建新对话");
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public Object getConversationMessages(Long conversationId) {
        // TODO: 实现获取对话消息逻辑
        log.info("获取对话消息: conversationId={}", conversationId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public Object renameConversation(Long conversationId, Object renameRequest) {
        // TODO: 实现重命名对话逻辑
        log.info("重命名对话: conversationId={}", conversationId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public void deleteConversation(Long conversationId) {
        // TODO: 实现删除对话逻辑
        log.info("删除对话: conversationId={}", conversationId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public Object sendMessage(Long conversationId, Object messageRequest) {
        // TODO: 实现发送消息逻辑
        log.info("发送消息: conversationId={}", conversationId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public Object exportMessageToDocument(Long messageId, Object exportRequest) {
        // TODO: 实现导出消息为文档逻辑
        log.info("导出消息为文档: messageId={}", messageId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public Object exportMessageToNote(Long messageId, Object exportRequest) {
        // TODO: 实现导出消息为小记逻辑
        log.info("导出消息为小记: messageId={}", messageId);
        throw new UnsupportedOperationException("待实现");
    }
}