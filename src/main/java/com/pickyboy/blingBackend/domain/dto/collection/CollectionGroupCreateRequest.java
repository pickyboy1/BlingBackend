package com.pickyboy.blingBackend.domain.dto.collection;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建收藏夹分组请求DTO
 *
 * @author pickyboy
 */
@Data
public class CollectionGroupCreateRequest {

    /**
     * 新分组的名称
     */
    @NotBlank(message = "收藏夹分组名称不能为空")
    private String groupName;
}