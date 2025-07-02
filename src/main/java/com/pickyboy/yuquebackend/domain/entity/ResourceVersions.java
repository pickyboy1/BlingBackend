package com.pickyboy.yuquebackend.domain.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 资源版本历史表
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("resource_versions")
public class ResourceVersions implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 版本记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 外键, 关联到resources表的主键
     */
    private Long resourceId;

    /**
     * 文件在MinIO中的完整访问URL
     */
    private String objectUrl;

    /**
     * 版本状态: ACTIVE(当前活动版本), ARCHIVED(已归档的历史版本)
     */
    private String status;

    /**
     * 版本创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;


}
