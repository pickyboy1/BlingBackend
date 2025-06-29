package com.pickyboy.yuquebackend.domain.dto.resource;

import lombok.Data;

/**
 * 更新文档内容请求DTO
 *
 * @author pickyboy
 */
@Data
public class UpdateResourceContentRequest {

    /**
     * 新的文档内容
     */
    private String content;
}