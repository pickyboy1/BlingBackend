package com.pickyboy.yuquebackend.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pickyboy.yuquebackend.domain.entity.FavoriteGroups;

/**
 * <p>
 * 收藏夹分组表 Mapper 接口
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
public interface FavoriteGroupsMapper extends BaseMapper<FavoriteGroups> {

    /**
     * 原子增加分组计数
     * @param groupId 分组ID
     * @return 影响行数
     */
    int incrementGroupCount(@Param("groupId") Long groupId);

    /**
     * 原子减少分组计数
     * @param groupId 分组ID
     * @return 影响行数
     */
    int decrementGroupCount(@Param("groupId") Long groupId);

}
