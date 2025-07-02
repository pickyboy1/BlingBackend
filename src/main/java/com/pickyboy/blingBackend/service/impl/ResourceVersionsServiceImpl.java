package com.pickyboy.blingBackend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.blingBackend.common.response.PageResult;
import com.pickyboy.blingBackend.common.utils.MinioUtil;
import com.pickyboy.blingBackend.domain.entity.ResourceVersions;
import com.pickyboy.blingBackend.mapper.ResourceVersionsMapper;
import com.pickyboy.blingBackend.service.IResourceVersionsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 资源版本历史表 服务实现类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceVersionsServiceImpl extends ServiceImpl<ResourceVersionsMapper, ResourceVersions> implements IResourceVersionsService {

    private final MinioUtil minioUtil;

    @Override
    public void createResourceVersion(Long resId, String oldContent) {
        log.info("创建资源版本: resId={}", resId);
        ResourceVersions resourceVersion = new ResourceVersions();
        resourceVersion.setResourceId(resId);
        resourceVersion.setObjectUrl(oldContent);
        resourceVersion.setStatus("ARCHIVED"); // 历史版本
        save(resourceVersion);
    }

    @Override
    public PageResult<ResourceVersions> getResourceVersionsPage(Long resId, Integer page, Integer limit) {
        log.info("分页查询资源版本: resId={}, page={}, limit={}", resId, page, limit);

        // 创建分页对象
        Page<ResourceVersions> pageObj = new Page<>(page, limit);

        // 构建查询条件
        LambdaQueryWrapper<ResourceVersions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ResourceVersions::getResourceId, resId)
                   .orderByDesc(ResourceVersions::getCreatedAt); // 按创建时间倒序

        // 执行分页查询
        IPage<ResourceVersions> pageResult = page(pageObj, queryWrapper);

        // 封装返回结果
        return new PageResult<>(pageResult.getTotal(), pageResult.getRecords());
    }

    @Override
    public List<ResourceVersions> getResourceVersions(Long resId) {
        log.info("查询资源版本列表: resId={}", resId);

        LambdaQueryWrapper<ResourceVersions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ResourceVersions::getResourceId, resId)
                   .orderByDesc(ResourceVersions::getCreatedAt);

        return list(queryWrapper);
    }

    @Override
    public void deleteResourceVersion(Long resId) {
        log.info("删除资源所有版本: resId={}", resId);

        LambdaQueryWrapper<ResourceVersions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ResourceVersions::getResourceId, resId);

        remove(queryWrapper);
    }

    @Override
    @Transactional // 【优化建议】删除操作也应在事务中进行
    public void deleteResourceVersionById(Long versionId) {
        log.info("删除指定版本: versionId={}", versionId);

        // 【关键修复】在删除数据库记录前，必须先删除MinIO中的物理文件
        ResourceVersions versionToDelete = getById(versionId);
        if (versionToDelete != null && versionToDelete.getObjectUrl() != null) {
            try {
                minioUtil.deleteObjectByUrl(versionToDelete.getObjectUrl());
                log.info("成功删除MinIO中的版本文件: url={}", versionToDelete.getObjectUrl());
            } catch (Exception e) {
                log.error("删除MinIO中的版本文件失败: url={}, error={}", versionToDelete.getObjectUrl(), e.getMessage());
                // 这里可以选择是否继续删除数据库记录，或者抛出异常回滚事务
                // throw new RuntimeException("删除MinIO文件失败", e);
            }
        } else {
            log.warn("未找到要删除的版本记录或其URL为空: versionId={}", versionId);
        }

        // 然后再删除数据库中的记录
        removeById(versionId);
    }


}
