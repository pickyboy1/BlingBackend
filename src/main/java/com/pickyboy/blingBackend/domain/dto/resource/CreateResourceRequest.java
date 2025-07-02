package com.pickyboy.blingBackend.domain.dto.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建文档资源请求DTO
 *
 * @author pickyboy
 */
@Data
public class CreateResourceRequest {

    /**
     * 父文档ID（如果是根目录则为null）
     */
    private Long preId;

    /**
     * 文档标题
     */
    @NotBlank(message = "文档标题不能为空")
    @Size(max = 200, message = "文档标题长度不能超过200个字符")
    private String title;

    /**
     * 文档类型
     */
    @NotBlank(message = "文档类型不能为空")
    @Size(max = 50, message = "文档类型长度不能超过50个字符")
    private String type;

    /**
     * 可见性：0-私密，1-公开
     */
    @Min(value = 0, message = "可见性值必须为0或1")
    @Max(value = 1, message = "可见性值必须为0或1")
    private Integer visibility = 0;

    /**
     * 文档内容url
     */
    @Size(max = 500, message = "文档内容URL长度不能超过500个字符")
    private String content;
}