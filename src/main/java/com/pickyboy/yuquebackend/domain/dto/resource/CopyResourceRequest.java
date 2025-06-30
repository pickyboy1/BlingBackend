package com.pickyboy.yuquebackend.domain.dto.resource;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 复制文档请求DTO
 *
 * @author pickyboy
 */
@Data
@AllArgsConstructor
public class CopyResourceRequest {

    /**
     * 目标知识库ID
     */
    @NotNull(message = "目标知识库ID不能为空")
    private Long targetKbId;

    /**
     * 目标父文档ID（如果是根目录则为null）
     */
    private Long targetPreId;
}