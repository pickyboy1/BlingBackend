package com.pickyboy.blingBackend.domain.dto.knowledgebase;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建知识库请求DTO
 *
 * @author pickyboy
 */
@Data
public class InsertKnowledgeBaseRequest {

    /**
     * 知识库名称 (必需)
     */
    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 100, message = "知识库名称长度不能超过100个字符")
    private String name;

    /**
     * 简介
     */
    @Size(max = 500, message = "简介长度不能超过500个字符")
    private String description;

    /**
     * 图标索引
     */
    @Size(max = 20, message = "图标索引长度不能超过20个字符")
    private String iconIndex;

    /**
     * 可见性: 0-私密, 1-公开
     */
    @Min(value = 0, message = "可见性值必须为0或1")
    @Max(value = 1, message = "可见性值必须为0或1")
    private Integer visibility;
}