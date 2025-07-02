package com.pickyboy.blingBackend.domain.dto.resource;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
    @Size(max = 200, message = "文档标题长度不能超过200个字符")
    private String title;

    /**
     * 新的可见性：0-私密，1-公开
     */
    @Min(value = 0, message = "可见性值必须为0或1")
    @Max(value = 1, message = "可见性值必须为0或1")
    private Integer visibility;
}