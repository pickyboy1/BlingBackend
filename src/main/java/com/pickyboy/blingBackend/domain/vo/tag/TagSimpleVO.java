package com.pickyboy.blingBackend.domain.vo.tag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简单标签响应VO
 *
 * @author shiqi
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagSimpleVO {

    /**
     * 标签ID
     */
    private String id;

    /**
     * 标签名称
     */
    private String name;

}
