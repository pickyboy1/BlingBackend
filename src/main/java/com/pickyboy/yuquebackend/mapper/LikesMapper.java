package com.pickyboy.yuquebackend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pickyboy.yuquebackend.domain.entity.Likes;
import com.pickyboy.yuquebackend.domain.vo.user.ActivityRecord;

/**
 * <p>
 * 点赞表 Mapper 接口
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
public interface LikesMapper extends BaseMapper<Likes> {

    List<ActivityRecord> likeHistory(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("limit") Integer limit);
}
