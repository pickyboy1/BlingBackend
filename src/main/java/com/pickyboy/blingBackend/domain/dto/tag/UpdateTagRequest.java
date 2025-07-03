package com.pickyboy.blingBackend.domain.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改标签请求DTO
 */
@Data
public class UpdateTagRequest {
    
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 10, message = "标签名称长度不能超过10个字符")
    private String name;
}