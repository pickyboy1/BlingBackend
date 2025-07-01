package com.pickyboy.yuquebackend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pickyboy.yuquebackend.domain.entity.UserFollows;
import com.pickyboy.yuquebackend.domain.vo.user.UserSummary;

/**
 * <p>
 * 用户关注表 Mapper 接口
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
public interface UserFollowsMapper extends BaseMapper<UserFollows> {
    @Select("SELECT u.id ,u.nickname, u.avatar_url, u.description  FROM user_follows uf JOIN users u ON uf.followee_id = u.id" +
            " WHERE follower_id = #{userId}  AND u.is_deleted = 0 LIMIT #{limit} OFFSET #{offset}")
    List<UserSummary> getUserFollowing(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("limit") Integer limit);

    @Select("SELECT u.id ,u.nickname, u.avatar_url, u.description  FROM user_follows uf JOIN users u ON uf.follower_id = u.id" +
            " WHERE followee_id = #{userId}  AND u.is_deleted = 0 LIMIT #{limit} OFFSET #{offset}")
    List<UserSummary> getUserFollowers(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("limit") Integer limit);
}
