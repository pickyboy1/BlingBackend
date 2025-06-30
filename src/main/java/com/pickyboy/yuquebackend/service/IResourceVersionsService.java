package com.pickyboy.yuquebackend.service;

import java.util.List;

import com.pickyboy.yuquebackend.domain.entity.ResourceVersions;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 资源版本历史表 服务类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-29
 */
public interface IResourceVersionsService extends IService<ResourceVersions> {

    // 创建资源版本
    void createResourceVersion(Long resId, String oldContent);

    // 获取资源版本
    List<ResourceVersions> getResourceVersions(Long resId);

    // 删除资源所有版本
    void deleteResourceVersion(Long resId);

    // 删除指定资源版本
    void deleteResourceVersionById(Long versionId);
}
