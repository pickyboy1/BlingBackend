package com.pickyboy.blingBackend.mapper;

import com.pickyboy.blingBackend.domain.entity.Tags;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * <p>
 * 标签表 Mapper 接口
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
public interface TagsMapper extends BaseMapper<Tags> {

    int batchDecrementReferedCount(@Param("tagDecrementMap") Map<Long, Long> tagDecrementMap);
}
