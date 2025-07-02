package com.pickyboy.blingBackend.service;

import java.util.List;

/**
 * AI服务接口
 *
 * @author pickyboy
 */
public interface IAiService {

    /**
     * 获取AI对话历史记录列表
     *
     * @return 对话历史列表
     */
    List<?> getConversations();

    /**
     * 创建新对话并发送第一条消息
     *
     * @param createRequest 创建请求
     * @return 对话信息
     */
    Object createConversation(Object createRequest);

    /**
     * 获取指定对话的所有消息
     *
     * @param conversationId 对话ID
     * @return 对话详情
     */
    Object getConversationMessages(Long conversationId);

    /**
     * 重命名对话记录
     *
     * @param conversationId 对话ID
     * @param renameRequest 重命名请求
     * @return 更新后的对话信息
     */
    Object renameConversation(Long conversationId, Object renameRequest);

    /**
     * 删除对话记录 (逻辑删除)
     *
     * @param conversationId 对话ID
     */
    void deleteConversation(Long conversationId);

    /**
     * 在现有对话中发送消息
     *
     * @param conversationId 对话ID
     * @param messageRequest 消息请求
     * @return 消息响应
     */
    Object sendMessage(Long conversationId, Object messageRequest);

    /**
     * 将AI消息导出为新文档
     *
     * @param messageId 消息ID
     * @param exportRequest 导出请求
     * @return 文档信息
     */
    Object exportMessageToDocument(Long messageId, Object exportRequest);

    /**
     * 将AI消息导出为小记
     *
     * @param messageId 消息ID
     * @param exportRequest 导出请求
     * @return 小记信息
     */
    Object exportMessageToNote(Long messageId, Object exportRequest);
}