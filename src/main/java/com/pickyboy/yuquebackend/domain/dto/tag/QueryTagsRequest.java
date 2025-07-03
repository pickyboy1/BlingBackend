package com.pickyboy.yuquebackend.domain.dto.tag;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 标签列表查询请求DTO
 *
 * @author shiqi
 */
@Data
public class QueryTagsRequest {
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;
    
    @Min(value = 1, message = "页面大小必须大于0")
    @Max(value = 100, message = "页面大小不能超过100")
    private Integer pageSize = 10;
    
    private String keyword; // 搜索关键词
}