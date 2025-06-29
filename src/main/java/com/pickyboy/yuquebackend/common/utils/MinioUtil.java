package com.pickyboy.yuquebackend.common.utils;


import com.pickyboy.yuquebackend.common.config.MinioConfig;
import com.pickyboy.yuquebackend.domain.vo.upload.UploadURLResponseVO;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor // 使用Lombok自动注入final字段
public class MinioUtil {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * 核心方法：生成预签名上传URL (已更新)
     *
     * @param fileName   原始文件名
     * @param uploadType 上传类型 ("avatar", "resource", "cover")
     * @param userId     执行上传操作的用户ID
     * @return 包含uploadUrl和accessUrl的VO对象
     */
    public UploadURLResponseVO generatePresignedUploadUrl(String fileName, String uploadType, String userId) {
        try {
            // 1. 根据上传类型获取存储桶名称
            String bucketName = minioConfig.getBucket().get(uploadType);
            if (bucketName == null) {
                throw new IllegalArgumentException("无效的上传类型: " + uploadType);
            }

            // 2. 生成唯一的对象名称 (存储在MinIO中的文件名) - 已更新，加入userId
            // 格式: userId/2025/06/29/uuid-原始文件名
            String objectName = generateUniqueObjectName(fileName, userId);

            // 3. 设置预签名URL的参数
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT) // 我们需要的是上传链接，所以用PUT方法
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(15, TimeUnit.MINUTES) // 设置URL有效期，例如15分钟
                    .build();

            // 4. 生成预签名URL
            String uploadUrl = minioClient.getPresignedObjectUrl(args);

            // 5. 构造最终可访问的URL
            // 这个URL的格式取决于你的MinIO配置和反向代理设置
            // 通常是 endpoint/bucketName/objectName
            String accessUrl = String.format("%s/%s/%s", minioConfig.getEndpoint(), bucketName, objectName);

            // 6. 返回结果
            return new UploadURLResponseVO(uploadUrl, accessUrl);

        } catch (Exception e) {
            log.error("生成预签名URL失败", e);
            throw new RuntimeException("无法生成上传链接，请稍后重试", e);
        }
    }

    /**
     * 新增方法：根据文件的完整访问URL删除MinIO中的对象
     *
     * @param objectUrl 文件的完整访问URL
     */
    public void deleteObjectByUrl(String objectUrl) {
        if (objectUrl == null || objectUrl.isEmpty()) {
            log.warn("尝试删除一个空的URL, 操作已跳过。");
            return;
        }

        try {
            URL url = new URL(objectUrl);
            String path = url.getPath(); // 获取路径部分，例如 "/avatars/123/2025/06/29/uuid-avatar.png"

            // 解析出 bucketName 和 objectName
            // 路径的第一部分是 bucketName
            String[] parts = path.substring(1).split("/", 2); // 移除开头的'/'后，按第一个'/'分割
            if (parts.length < 2) {
                log.error("无法从URL中解析出bucket和object名: {}", objectUrl);
                return;
            }
            String bucketName = parts[0];
            String objectName = parts[1];

            // 执行删除
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("成功删除MinIO对象: {}", objectUrl);

        } catch (Exception e) {
            log.error("从MinIO删除文件失败, URL: {}", objectUrl, e);
            // 在定时任务中，可以考虑不抛出异常，只记录日志，以免中断整个任务
        }
    }

    /**
     * 生成带用户和日期路径的唯一对象名 (已更新)
     */
    private String generateUniqueObjectName(String originalFilename, String userId) {
        // 使用 "userId/年/月/日/UUID-原文件名" 的格式
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
            // 抛出运行时异常，如果存储桶都无法创建，服务启动失败是合理的
            throw new RuntimeException("初始化MinIO存储桶失败", e);
        }
    }


}