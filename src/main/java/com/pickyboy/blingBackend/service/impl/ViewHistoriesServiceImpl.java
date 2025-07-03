package com.pickyboy.blingBackend.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.blingBackend.domain.entity.ViewHistories;
import com.pickyboy.blingBackend.mapper.ViewHistoriesMapper;
import com.pickyboy.blingBackend.service.IViewHistoriesService;

/**
 * <p>
 * 浏览历史表 服务实现类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Service
public class ViewHistoriesServiceImpl extends ServiceImpl<ViewHistoriesMapper, ViewHistories> implements IViewHistoriesService {

}
