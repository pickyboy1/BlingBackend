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
 * AI对话内容表
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("ai_messages")
public class AiMessages implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID, 雪花算法生成
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 对话id
     */
    private Long conversationId;

    /**
     * 对话角色
     */
    private String role;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 对话内容
     */
    private String content;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标记: 0-未删除, 1-已删除
     */
    @TableLogic
    private Boolean isDeleted;


}
