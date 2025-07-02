package com.pickyboy.blingBackend.domain.dto.resource;

import lombok.Data;

/**
 * 恢复文档请求DTO
 *
 * @author pickyboy
 */
@Data
public class RestoreResourceRequest {

    /**
     * 要恢复到的父目录ID（如果恢复到根目录则为null）
     */
    private Long targetPreId;
}