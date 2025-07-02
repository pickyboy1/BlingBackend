package com.pickyboy.yuquebackend.domain.dto.note;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 设置小记标签请求DTO
 *
 * @author shiqi
 */
@Data
public class SetNoteTagsRequest {
    @NotNull(message = "小记ID不能为空")
    private Long noteId;
    
    @Size(max = 10, message = "最多只能关联10个标签")
    private Long[] tagIds;
}