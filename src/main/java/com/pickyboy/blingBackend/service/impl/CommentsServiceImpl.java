package com.pickyboy.blingBackend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.blingBackend.domain.entity.Comments;
import com.pickyboy.blingBackend.domain.vo.comment.RootCommentVO;
import com.pickyboy.blingBackend.domain.vo.comment.SubCommentVO;
import com.pickyboy.blingBackend.mapper.CommentsMapper;
import com.pickyboy.blingBackend.service.ICommentsService;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 评论表 服务实现类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Service
@RequiredArgsConstructor
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments> implements ICommentsService {

    private final CommentsMapper commentsMapper;

    @Override
    public List<RootCommentVO> listRootComments(Long articleId, Integer offset, Integer limit) {
        return commentsMapper.listRootComments(articleId, offset, limit);
    }


    @Override
    public List<SubCommentVO> listSubComments(Long commentId, Integer offset, Integer limit) {
        return commentsMapper.listSubComments(commentId, offset, limit);
    }
}
