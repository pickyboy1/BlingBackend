package com.pickyboy.yuquebackend.service.impl;

import java.util.List;

import com.pickyboy.yuquebackend.domain.entity.ResourceVersions;
import com.pickyboy.yuquebackend.mapper.ResourceVersionsMapper;
import com.pickyboy.yuquebackend.service.IResourceVersionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 资源版本历史表 服务实现类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-29
 */
@Service
public class ResourceVersionsServiceImpl extends ServiceImpl<ResourceVersionsMapper, ResourceVersions> implements IResourceVersionsService {

    @Override
    public void createResourceVersion(Long resId, String oldContent) {
        ResourceVersions resourceVersion = new ResourceVersions();
        resourceVersion.setResourceId(resId);
        resourceVersion.setObjectUrl(oldContent);
        save(resourceVersion);
    }

    @Override
    public List<ResourceVersions> getResourceVersions(Long resId) {
        // todo: 实现
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public void deleteResourceVersion(Long resId) {
        // todo: 实现
        throw new UnsupportedOperationException("此方法尚未实现");
    }

    @Override
    public void deleteResourceVersionById(Long versionId) {
        // todo: 实现
        throw new UnsupportedOperationException("此方法尚未实现");
    }
}
