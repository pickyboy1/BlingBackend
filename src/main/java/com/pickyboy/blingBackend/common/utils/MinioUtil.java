package com.pickyboy.blingBackend.common.utils;

import com.pickyboy.blingBackend.common.config.MinioConfig;
import com.pickyboy.blingBackend.domain.vo.upload.UploadURLResponseVO;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtil {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * 【新增方法】 核心方法：复制一个MinIO对象，并返回新对象的访问URL。
     * 这个方法利用了MinIO的服务端复制功能，性能极高。
     *
     * @param sourceUrl  源对象的完整访问URL
     * @param fileName   新对象的文件名 (通常是资源的标题)
     * @param uploadType 新对象的上传类型 ("avatar", "resource", "cover")
     * @param userId     操作用户ID
     * @return 新对象的完整访问URL
     */
    public String copyObject(String sourceUrl, String fileName, String uploadType, String userId) {
        if (sourceUrl == null || sourceUrl.isEmpty()) {
            return null; // 如果源URL为空，直接返回null
        }

        try {
            // 1. 解析源URL，获取源存储桶和对象名
            ObjectLocation sourceLocation = parseUrl(sourceUrl);
            if (sourceLocation == null) {
                throw new IllegalArgumentException("无法解析源文件URL: " + sourceUrl);
            }

            // 2. 根据上传类型，确定目标存储桶
            String destBucketName = minioConfig.getBucket().get(uploadType);
            if (destBucketName == null) {
                throw new IllegalArgumentException("无效的目标上传类型: " + uploadType);
            }

            // 3. 为新对象生成一个唯一的名称
            String destObjectName = generateUniqueObjectName(fileName, userId);

            // 4. 配置并执行服务端复制
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .source(CopySource.builder()
                                    .bucket(sourceLocation.getBucketName())
                                    .object(sourceLocation.getObjectName())
                                    .build())
                            .bucket(destBucketName)
                            .object(destObjectName)
                            .build()
            );

            // 5. 构造并返回新对象的访问URL
            String accessUrl = String.format("%s/%s/%s", minioConfig.getEndpoint(), destBucketName, destObjectName);
            log.info("成功复制对象 {} -> {}", sourceUrl, accessUrl);
            return accessUrl;

        } catch (Exception e) {
            log.error("复制MinIO对象失败, 源URL: {}", sourceUrl, e);
            throw new RuntimeException("文件复制失败，请稍后重试", e);
        }
    }

    /**
     * 核心方法：生成预签名上传URL
     *
     * @param fileName   原始文件名
     * @param uploadType 上传类型 ("avatar", "resource_content", "kb_cover")
     * @param userId     执行上传操作的用户ID
     * @return 包含uploadUrl和accessUrl的VO对象
     */
    public UploadURLResponseVO generatePresignedUploadUrl(String fileName, String uploadType, String userId) {
        try {
            String bucketName = minioConfig.getBucket().get(uploadType);
            if (bucketName == null) {
                throw new IllegalArgumentException("无效的上传类型: " + uploadType);
            }
            String objectName = generateUniqueObjectName(fileName, userId);
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(15, TimeUnit.MINUTES)
                    .build();
            String uploadUrl = minioClient.getPresignedObjectUrl(args);
            String accessUrl = String.format("%s/%s/%s", minioConfig.getEndpoint(), bucketName, objectName);
            return new UploadURLResponseVO(uploadUrl, accessUrl);
        } catch (Exception e) {
            log.error("生成预签名URL失败", e);
            throw new RuntimeException("无法生成上传链接，请稍后重试", e);
        }
    }

    /**
     * 根据文件的完整访问URL删除MinIO中的对象
     *
     * @param objectUrl 文件的完整访问URL
     */
    public void deleteObjectByUrl(String objectUrl) {
        if (objectUrl == null || objectUrl.isEmpty()) {
            log.warn("尝试删除一个空的URL, 操作已跳过。");
            return;
        }
        try {
            ObjectLocation location = parseUrl(objectUrl);
            if (location == null) {
                return;
            }
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(location.getBucketName())
                            .object(location.getObjectName())
                            .build()
            );
            log.info("成功删除MinIO对象: {}", objectUrl);
        } catch (Exception e) {
            log.error("从MinIO删除文件失败, URL: {}", objectUrl, e);
        }
    }

    /**
     * 【辅助方法】 从完整的URL中解析出存储桶和对象名
     */
    private ObjectLocation parseUrl(String objectUrl) throws Exception {
        URL url = new URL(objectUrl);
        String path = url.getPath();
        String[] parts = path.substring(1).split("/", 2);
        if (parts.length < 2) {
            log.error("无法从URL中解析出bucket和object名: {}", objectUrl);
            return null;
        }
        return new ObjectLocation(parts[0], parts[1]);
    }

    /**
     * 生成带用户和日期路径的唯一对象名
     */
    private String generateUniqueObjectName(String originalFilename, String userId) {
        java.time.LocalDate today = java.time.LocalDate.now();
        String path = String.format("%s/%d/%02d/%02d/",
                userId,
                today.getYear(),
                today.getMonthValue(),
                today.getDayOfMonth());
        return path + UUID.randomUUID().toString() + "-" + originalFilename;
    }

    /**
     * 服务启动时执行，检查并创建所有配置的存储桶
     */
    @PostConstruct
    private void initBuckets() {
        try {
            for (String bucketName : minioConfig.getBucket().values()) {
                boolean found = minioClient.bucketExists(io.minio.BucketExistsArgs.builder().bucket(bucketName).build());
                if (!found) {
                    minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
                    log.info("存储桶 '{}' 创建成功.", bucketName);
                } else {
                    log.info("存储桶 '{}' 已存在.", bucketName);
                }
            }
        } catch (Exception e) {
            log.error("初始化MinIO存储桶失败", e);
            throw new RuntimeException("初始化MinIO存储桶失败", e);
        }
    }

    /**
     * 【内部类】 用于封装解析出的位置信息
     */
    @Data
    @AllArgsConstructor
    private static class ObjectLocation {
        private String bucketName;
        private String objectName;
    }
}
