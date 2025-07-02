package com.pickyboy.yuquebackend.domain.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 批量删除标签请求DTO
 *
 * @author shiqi
 */
@Data
public class DeleteTagsRequest {
    @NotBlank(message = "标签ID列表不能为空")
    @Size(max = 50, message = "最多只能批量删除50个标签")
    private Long[] tagIds;
}