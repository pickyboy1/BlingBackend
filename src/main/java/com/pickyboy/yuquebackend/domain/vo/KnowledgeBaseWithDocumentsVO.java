package com.pickyboy.yuquebackend.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 带文档树的知识库VO
 *
 * @author pickyboy
 */
@Data
public class KnowledgeBaseWithDocumentsVO {

    /**
     * 知识库ID
     */
    private Long id;

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 简介
     */
    private String description;

    /**
     * 图标索引
     */
    private String iconIndex;

    /**
     * 封面地址
     */
    private String coverUrl;

    /**
     * 可见性: 0-私密, 1-公开
     */
    private Integer visibility;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 文档树结构
     */
    private List<DocumentNodeVO> documents;
}