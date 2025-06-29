package com.pickyboy.yuquebackend.domain.dto.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;




/**
 * 创建文档资源请求DTO
 *
 * @author pickyboy
 */
@Data
public class CreateResourceRequest {

    /**
     * 知识库ID
     */
    @NotNull(message = "知识库ID不能为空")
    private Long knowledgeBaseId;

    /**
     * 父文档ID（如果是根目录则为null）
     */
    private Long preId;

    /**
     * 文档标题
     */
    @NotBlank(message = "文档标题不能为空")
    private String title;

    /**
     * 文档类型
     */
    @NotBlank(message = "文档类型不能为空")
    private String type;

    /**
     * 可见性：0-私密，1-公开
     */
    private Integer visibility = 0;

    /**
     * 文档内容
     */
    private String content;
}