package com.pickyboy.yuquebackend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pickyboy.yuquebackend.domain.entity.Resources;

/**
 * <p>
 * 文档/资源表 Mapper 接口
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
public interface ResourcesMapper extends BaseMapper<Resources> {

    /**
     * 自定义SQL查询，使用JOIN获取回收站中的文档
     * @param userId 用户ID
     * @return 文档列表
     */
    List<Resources> selectDeletedResourcesInActiveKbs(@Param("userId") Long userId);

}
