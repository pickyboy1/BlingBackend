package com.pickyboy.blingBackend.domain.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * 文档/资源表
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("resources")
@NoArgsConstructor
@AllArgsConstructor
public class Resources implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资源ID, 雪花算法生成
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 外键,关联知识库
     */
    private Long knowledgeBaseId;

    /**
     * 父文档id
     */
    private Long preId;

    /**
     * 外键,关联用户
     */
    private Long userId;

    /**
     * 文档名
     */
    private String title;

    /**
     * 文档类型
     */
    private String type;

    /**
     * 文档内容url地址
     */
    private String content;

    /**
     * 分享地址
     */
    private String shareId;

    /**
     * 上架/下架状态
     */
    private Integer status;

    /**
     * 可见性: 0-私密, 1-公开
     */
    private Integer visibility;

    /**
     * 发布时间
     */
    private LocalDateTime publishedAt;

    /*
    * 浏览量
    * */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 收藏数
     */
    private Integer favoriteCount;

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

    public Resources(Long id, String title, String type, Long preId) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.preId = preId;
    }

}
