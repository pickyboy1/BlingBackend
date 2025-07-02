package com.pickyboy.yuquebackend.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pickyboy.yuquebackend.domain.entity.Comments;
import com.pickyboy.yuquebackend.domain.vo.comment.RootCommentVO;
import com.pickyboy.yuquebackend.domain.vo.comment.SubCommentVO;

/**
 * <p>
 * 评论表 服务类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
public interface ICommentsService extends IService<Comments> {
    List<RootCommentVO> listRootComments(Long articleId, Integer offset, Integer limit);

    List<SubCommentVO> listSubComments(Long commentId, Integer offset, Integer limit);

}
