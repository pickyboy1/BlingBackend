package com.pickyboy.yuquebackend.domain.dto.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建小记请求DTO
 *
 * @author shiqi
 */
@Data
public class CreateNoteRequest {
    /**
     * 小记的完整内容
     */
    @NotBlank(message = "小记内容不能为空")
    @Size(max = 65535, message = "小记内容长度不能超过65535个字符")
    private String content;

    /**
     * 关联的标签ID列表（可选）
     */
    @Size(max = 50, message = "一个小记最多关联50个标签")
    private List<String> tagIds;
}