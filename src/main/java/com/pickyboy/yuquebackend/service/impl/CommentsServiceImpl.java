package com.pickyboy.yuquebackend.service.impl;

import com.pickyboy.yuquebackend.domain.entity.Comments;
import com.pickyboy.yuquebackend.mapper.CommentsMapper;
import com.pickyboy.yuquebackend.service.ICommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 评论表 服务实现类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Service
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments> implements ICommentsService {

}
