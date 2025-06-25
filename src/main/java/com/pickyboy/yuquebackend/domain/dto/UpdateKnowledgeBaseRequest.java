package com.pickyboy.yuquebackend.domain.dto;

import lombok.Data;

/**
 * 更新知识库请求DTO
 *
 * @author pickyboy
 */
@Data
public class UpdateKnowledgeBaseRequest {

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
     * 可见性: 0-私密, 1-公开
     */
    private Integer visibility;
}