package com.pickyboy.blingBackend.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pickyboy.blingBackend.common.exception.BusinessException;
import com.pickyboy.blingBackend.common.exception.ErrorCode;
import com.pickyboy.blingBackend.common.response.Result;
import com.pickyboy.blingBackend.common.utils.CurrentHolder;
import com.pickyboy.blingBackend.common.utils.MinioUtil;
import com.pickyboy.blingBackend.domain.vo.upload.UploadURLResponseVO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文件上传控制器
 *
 * @author pickyboy
 */
@Slf4j
@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final MinioUtil minioUtil;

    // 支持的上传类型
    private static final List<String> SUPPORTED_UPLOAD_TYPES = Arrays.asList(
        "avatar", "resource", "cover"
    );

    // 文件大小限制（字节）
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    // 支持的文件类型
    private static final List<String> SUPPORTED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> SUPPORTED_DOCUMENT_TYPES = Arrays.asList(
        "text/html", "text/plain", "application/json"
    );

    /**
     * 获取文件上传链接
     *
     * @param requestVO 上传请求
     * @return 上传链接信息
     */
    @PostMapping("/request-url")
    public Result<UploadURLResponseVO> requestUploadUrl(@Valid @RequestBody UploadURLRequestVO requestVO) {
        log.info("申请文件上传链接: {}", requestVO);

        // 1. 验证用户登录状态
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 2. 验证请求参数
        validateUploadRequest(requestVO);

        try {
            // 3. 生成预签名上传URL
            UploadURLResponseVO response = minioUtil.generatePresignedUploadUrl(
                    requestVO.getFileName(),
                    requestVO.getUploadType(),
                    userId.toString()
            );

            log.info("文件上传链接生成成功: userId={}, fileName={}", userId, requestVO.getFileName());
            return Result.success(response);

        } catch (Exception e) {
            log.error("生成文件上传链接失败: userId={}, fileName={}", userId, requestVO.getFileName(), e);
            throw new BusinessException(ErrorCode.UPLOAD_URL_GENERATE_FAILED);
        }
    }

    /**
     * 验证上传请求参数
     *
     * @param requestVO 上传请求
     */
    private void validateUploadRequest(UploadURLRequestVO requestVO) {
        // 验证文件名
        if (!StringUtils.hasText(requestVO.getFileName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }

        // 验证文件类型
        if (!StringUtils.hasText(requestVO.getFileType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型不能为空");
        }

        // 验证文件大小
        if (requestVO.getFileSize() == null || requestVO.getFileSize() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小无效");
        }

        if (requestVO.getFileSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED,
                String.format("文件大小超出限制，最大允许 %d MB", MAX_FILE_SIZE / 1024 / 1024));
        }

        // 验证上传类型
        if (!StringUtils.hasText(requestVO.getUploadType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传类型不能为空");
        }

        if (!SUPPORTED_UPLOAD_TYPES.contains(requestVO.getUploadType())) {
            throw new BusinessException(ErrorCode.INVALID_UPLOAD_TYPE,
                "不支持的上传类型: " + requestVO.getUploadType());
        }

        // 根据上传类型验证文件格式
        validateFileTypeByUploadType(requestVO.getUploadType(), requestVO.getFileType());
    }

    /**
     * 根据上传类型验证文件格式
     *
     * @param uploadType 上传类型
     * @param fileType   文件MIME类型
     */
    private void validateFileTypeByUploadType(String uploadType, String fileType) {
        switch (uploadType) {
            case "avatar":
            case "cover":
                // 头像和封面只支持图片格式
                if (!SUPPORTED_IMAGE_TYPES.contains(fileType.toLowerCase())) {
                    throw new BusinessException(ErrorCode.INVALID_FILE_TYPE,
                        "该上传类型只支持图片格式: " + String.join(", ", SUPPORTED_IMAGE_TYPES));
                }
                break;

            case "resource":
                // 资源内容支持文档格式
                if (!SUPPORTED_DOCUMENT_TYPES.contains(fileType.toLowerCase()) &&
                    !SUPPORTED_IMAGE_TYPES.contains(fileType.toLowerCase())) {
                    throw new BusinessException(ErrorCode.INVALID_FILE_TYPE,
                        "该上传类型不支持的文件格式: " + fileType);
                }
                break;

            default:
                throw new BusinessException(ErrorCode.INVALID_UPLOAD_TYPE, "未知的上传类型: " + uploadType);
        }
    }

    /**
     * 上传请求VO类
     */
    @Data
    public static class UploadURLRequestVO {
        /**
         * 原始文件名
         */
        @NotBlank(message = "文件名不能为空")
        @Size(max = 255, message = "文件名长度不能超过255个字符")
        private String fileName;

        /**
         * 文件的MIME类型
         */
        @NotBlank(message = "文件类型不能为空")
        private String fileType;

        /**
         * 文件大小（字节）
         */
        @NotNull(message = "文件大小不能为空")
        @Min(value = 1, message = "文件大小必须大于0")
        @Max(value = 52428800, message = "文件大小不能超过50MB") // 50MB = 50 * 1024 * 1024
        private Long fileSize;

        /**
         * 上传用途分类：avatar（头像）、resource（资源内容）、cover（知识库封面）
         */
        @NotBlank(message = "上传类型不能为空")
        @Pattern(regexp = "^(avatar|resource|cover)$", message = "上传类型只能是avatar、resource或cover")
        private String uploadType;
    }
}