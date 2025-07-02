package com.pickyboy.yuquebackend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pickyboy.yuquebackend.domain.entity.Comments;
import com.pickyboy.yuquebackend.domain.vo.comment.RootCommentVO;
import com.pickyboy.yuquebackend.domain.vo.comment.SubCommentVO;
import com.pickyboy.yuquebackend.domain.vo.user.ActivityRecord;

/**
 * <p>
 * 评论表 Mapper 接口
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
public interface CommentsMapper extends BaseMapper<Comments> {
    List<ActivityRecord> commentHistory(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("limit") Integer limit);

    List<RootCommentVO> listRootComments(@Param("articleId") Long articleId, @Param("offset") Integer offset, @Param("limit") Integer limit);
    List<SubCommentVO> listSubComments(@Param("commentId") Long commentId, @Param("offset") Integer offset, @Param("limit") Integer limit);

}
