package com.pickyboy.blingBackend.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pickyboy.blingBackend.domain.entity.Comments;
import com.pickyboy.blingBackend.domain.vo.comment.RootCommentVO;
import com.pickyboy.blingBackend.domain.vo.comment.SubCommentVO;

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
