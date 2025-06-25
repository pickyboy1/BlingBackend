package com.pickyboy.yuquebackend.domain.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 知识库表
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("knowledge_bases")
public class KnowledgeBases implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 知识库ID, 雪花算法生成
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 外键,关联用户
     */
    private Long userId;

    /**
     * 知识库名
     */
    private String name;

    /**
     * 简介
     */
    private String description;

    /**
     * 使用的图标索引
     */
    private String iconIndex;

    /**
     * 封面地址
     */
    private String coverUrl;

    /**
     * 分享地址
     */
    private String shareId;

    /**
     * 可见性: 0-私密, 1-公开
     */
    private Integer visibility;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记: 0-未删除, 1-已删除
     */
    @TableLogic
    private Boolean isDeleted;


}
