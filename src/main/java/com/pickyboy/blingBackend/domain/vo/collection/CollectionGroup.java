package com.pickyboy.blingBackend.domain.vo.collection;

import lombok.Data;

/**
 * 收藏夹分组信息VO
 *
 * @author pickyboy
 */
@Data
public class CollectionGroup {

    /**
     * 分组ID
     */
    private String id;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 分组内收藏的文章数量
     */
    private Integer count;
}