package com.pickyboy.yuquebackend.domain.dto.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 批量删除小记请求DTO
 *
 * @author shiqi
 */
@Data
public class DeleteNotesRequest {
    @NotBlank(message = "小记ID列表不能为空")
    @Size(max = 50, message = "最多只能批量删除50个小记")
    private Long[] noteIds;
}