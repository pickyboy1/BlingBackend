package com.pickyboy.blingBackend.domain.dto.note;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除小记请求DTO
 *
 * @author shiqi
 */
@Data
public class DeleteNotesRequest {
    @NotEmpty(message = "小记ID列表不能为空")
    @Size(max = 100, message = "最多只能批量删除100个小记")
    private List<String> noteIds;
}