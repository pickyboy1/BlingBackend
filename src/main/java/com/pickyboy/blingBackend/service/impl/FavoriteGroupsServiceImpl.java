package com.pickyboy.blingBackend.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.blingBackend.domain.entity.FavoriteGroups;
import com.pickyboy.blingBackend.mapper.FavoriteGroupsMapper;
import com.pickyboy.blingBackend.service.IFavoriteGroupsService;

/**
 * <p>
 * 收藏夹分组表 服务实现类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Service
public class FavoriteGroupsServiceImpl extends ServiceImpl<FavoriteGroupsMapper, FavoriteGroups> implements IFavoriteGroupsService {

}
