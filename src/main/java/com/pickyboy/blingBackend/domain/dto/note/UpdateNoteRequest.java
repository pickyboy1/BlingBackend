package com.pickyboy.blingBackend.domain.dto.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑小记请求DTO
 *
 * @author shiqi
 */
@Data
public class UpdateNoteRequest {

    @NotBlank(message = "小记内容不能为空")
    @Size(max = 65535, message = "小记内容长度不能超过65535个字符")
    private String content;
}