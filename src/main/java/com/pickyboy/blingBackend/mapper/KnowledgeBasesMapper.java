package com.pickyboy.blingBackend.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pickyboy.blingBackend.domain.entity.KnowledgeBases;

/**
 * <p>
 * 知识库表 Mapper 接口
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
public interface KnowledgeBasesMapper extends BaseMapper<KnowledgeBases> {

    // ====== 【原子操作】计数器更新方法 ======

    /**
     * 原子增加知识库浏览量
     * @param kbId 知识库ID
     * @return 影响行数
     */
    int incrementViewCount(@Param("kbId") Long kbId);

}
