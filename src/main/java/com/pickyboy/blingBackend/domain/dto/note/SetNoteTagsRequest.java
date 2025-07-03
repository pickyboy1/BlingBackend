package com.pickyboy.blingBackend.domain.dto.note;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 设置小记标签请求DTO
 *
 * @author shiqi
 */
@Data
public class SetNoteTagsRequest {
    
    @Size(max = 50, message = "最多只能关联50个标签")
    private List<String> tagIds;
}