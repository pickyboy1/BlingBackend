package com.pickyboy.blingBackend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.blingBackend.domain.entity.Favorites;
import com.pickyboy.blingBackend.mapper.FavoritesMapper;
import com.pickyboy.blingBackend.service.IFavoriteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 收藏服务实现类
 *
 * @author pickyboy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoritesMapper, Favorites> implements IFavoriteService {

}
