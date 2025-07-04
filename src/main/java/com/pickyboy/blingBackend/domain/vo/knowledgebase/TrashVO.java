package com.pickyboy.blingBackend.domain.vo.knowledgebase;

import java.util.List;

import com.pickyboy.blingBackend.domain.entity.KnowledgeBases;
import com.pickyboy.blingBackend.domain.entity.Resources;

import lombok.Data;

/**
 * 回收站VO
 *
 * @author pickyboy
 */
@Data
public class TrashVO {

    /**
     * 已删除的知识库列表
     */
    private List<KnowledgeBases> knowledgeBases;

    /**
     * 已删除的文档列表
     */
    private List<Resources> resources;
}