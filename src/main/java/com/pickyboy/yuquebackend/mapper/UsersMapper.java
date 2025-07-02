package com.pickyboy.yuquebackend.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pickyboy.yuquebackend.domain.entity.Users;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
public interface UsersMapper extends BaseMapper<Users> {

    // ====== 【原子操作】计数器更新方法 ======

    /**
     * 原子增加用户粉丝数
     * @param userId 用户ID
     * @return 影响行数
     */
    int incrementFollowerCount(@Param("userId") Long userId);

    /**
     * 原子减少用户粉丝数
     * @param userId 用户ID
     * @return 影响行数
     */
    int decrementFollowerCount(@Param("userId") Long userId);

    /**
     * 原子增加用户关注数
     * @param userId 用户ID
     * @return 影响行数
     */
    int incrementFollowedCount(@Param("userId") Long userId);

    /**
     * 原子减少用户关注数
     * @param userId 用户ID
     * @return 影响行数
     */
    int decrementFollowedCount(@Param("userId") Long userId);

}
