package com.pickyboy.yuquebackend.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pickyboy.yuquebackend.common.response.PageResult;
import com.pickyboy.yuquebackend.domain.entity.ResourceVersions;

/**
 * <p>
 * 资源版本历史表 服务类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-29
 */
public interface IResourceVersionsService extends IService<ResourceVersions> {

    /**
     * 创建资源版本
     *
     * @param resId 资源ID
     * @param oldContent 旧版本内容
     */
    void createResourceVersion(Long resId, String oldContent);

    /**
     * 分页获取资源版本历史
     *
     * @param resId 资源ID
     * @param page 页码
     * @param limit 每页数量
     * @return 分页结果
     */
    PageResult<ResourceVersions> getResourceVersionsPage(Long resId, Integer page, Integer limit);

    /**
     * 获取资源版本列表（非分页）
     *
     * @param resId 资源ID
     * @return 版本列表
     */
    List<ResourceVersions> getResourceVersions(Long resId);

    /**
     * 删除资源所有版本
     *
     * @param resId 资源ID
     */
    void deleteResourceVersion(Long resId);

    /**
     * 删除指定资源版本
     *
     * @param versionId 版本ID
     */
    void deleteResourceVersionById(Long versionId);


}
