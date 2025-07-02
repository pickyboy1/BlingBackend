package com.pickyboy.blingBackend.service.impl;

import com.pickyboy.blingBackend.domain.entity.AiMessages;
import com.pickyboy.blingBackend.mapper.AiMessagesMapper;
import com.pickyboy.blingBackend.service.IAiMessagesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * AI对话内容表 服务实现类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Service
public class AiMessagesServiceImpl extends ServiceImpl<AiMessagesMapper, AiMessages> implements IAiMessagesService {

}
