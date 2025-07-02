package com.pickyboy.yuquebackend.domain.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 投稿审核表
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("submissions")
public class Submissions implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 知识库id
     */
    private Long knowledgeBaseId;

    /**
     * 文档id
     */
    private Long resourceId;

    /**
     * 投稿用户
     */
    private Long userId;

    /**
     * 审核状态
     */
    private Integer status;

    /**
     * 投稿理由
     */
    private String recommendReason;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记: 0-未删除, 1-已删除
     */
    @TableLogic
    private Boolean isDeleted;


}
