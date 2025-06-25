package com.pickyboy.yuquebackend.domain.vo;

import java.util.List;

import lombok.Data;

/**
 * 文档节点VO（用于构建文档树）
 *
 * @author pickyboy
 */
@Data
public class DocumentNodeVO {

    /**
     * 文档ID
     */
    private Long id;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档类型
     */
    private String type;

    /**
     * 子文档列表
     */
    private List<DocumentNodeVO> children;
}