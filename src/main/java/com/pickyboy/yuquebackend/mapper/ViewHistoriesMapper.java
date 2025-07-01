package com.pickyboy.yuquebackend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pickyboy.yuquebackend.domain.entity.ViewHistories;
import com.pickyboy.yuquebackend.domain.vo.user.ActivityRecord;

/**
 * <p>
 * 浏览历史表 Mapper 接口
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
public interface ViewHistoriesMapper extends BaseMapper<ViewHistories> {

    /**
     * 插入或更新浏览记录
     * 如果用户已经浏览过该资源，则更新lastViewAt时间
     * 如果用户未浏览过该资源，则插入新记录
     *
     * @param viewHistory 浏览历史对象
     * @return 影响的行数
     */
    int insertOrUpdateViewHistory(ViewHistories viewHistory);

    /**
     * 获取用户浏览历史
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 浏览历史列表
     */
    List<ActivityRecord> getUserViewHistory(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("limit") Integer limit);
}
