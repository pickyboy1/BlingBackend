package com.pickyboy.yuquebackend.service.impl;

import com.pickyboy.yuquebackend.domain.entity.AiMessages;
import com.pickyboy.yuquebackend.mapper.AiMessagesMapper;
import com.pickyboy.yuquebackend.service.IAiMessagesService;
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
