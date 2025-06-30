package com.pickyboy.yuquebackend.domain.dto.resource;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新文档内容请求DTO
 *
 * @author pickyboy
 */
@Data
public class UpdateResourceContentRequest {

    /**
     * 新的文档内容URL
     */
    @Size(max = 500, message = "文档内容URL长度不能超过500个字符")
    private String content;

    /**
     * 新的文档标题
     */
    @Size(max = 200, message = "文档标题长度不能超过200个字符")
    private String title;
}