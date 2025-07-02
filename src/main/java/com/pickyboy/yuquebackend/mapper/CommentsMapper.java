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

    // ====== 【原子操作】计数器更新方法 ======

    /**
     * 原子增加评论回复数
     * @param commentId 评论ID
     * @return 影响行数
     */
    int incrementReplyCount(@Param("commentId") Long commentId);

    /**
     * 原子减少评论回复数
     * @param commentId 评论ID
     * @return 影响行数
     */
    int decrementReplyCount(@Param("commentId") Long commentId);

}
