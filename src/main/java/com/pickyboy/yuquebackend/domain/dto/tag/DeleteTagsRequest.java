package com.pickyboy.yuquebackend.domain.dto.tag;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除标签请求DTO
 *
 * @author shiqi
 */
@Data
public class DeleteTagsRequest {
    @NotEmpty(message = "标签ID列表不能为空")
    @Size(max = 100, message = "最多只能批量删除100个标签")
    private List<String> tagIds;
}