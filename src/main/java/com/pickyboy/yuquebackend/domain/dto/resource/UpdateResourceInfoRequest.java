package com.pickyboy.yuquebackend.domain.dto.resource;

import lombok.Data;

/**
 * 更新文档信息请求DTO
 *
 * @author pickyboy
 */
@Data
public class UpdateResourceInfoRequest {

    /**
     * 新的文档标题
     */
    private String title;

    /**
     * 新的可见性：0-私密，1-公开
     */
    private Integer visibility;
}