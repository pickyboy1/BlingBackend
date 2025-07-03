package com.pickyboy.blingBackend.domain.vo.tag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签响应VO
 *
 * @author shiqi
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagVO {

    /**
     * 标签ID
     */
    private String id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 引用次数
     */
    private Integer count;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 更新时间
     */
    private String updatedAt;
}
