package com.pickyboy.yuquebackend.service.impl;

import com.pickyboy.yuquebackend.domain.entity.UserFollows;
import com.pickyboy.yuquebackend.mapper.UserFollowsMapper;
import com.pickyboy.yuquebackend.service.IUserFollowsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户关注表 服务实现类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Service
public class UserFollowsServiceImpl extends ServiceImpl<UserFollowsMapper, UserFollows> implements IUserFollowsService {

}
