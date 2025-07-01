package com.pickyboy.yuquebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pickyboy.yuquebackend.domain.entity.ViewHistories;

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
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 影响的行数
     */
    int insertOrUpdateViewHistory(ViewHistories viewHistory);
}
