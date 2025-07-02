package com.pickyboy.blingBackend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.blingBackend.domain.entity.UserFollows;
import com.pickyboy.blingBackend.domain.vo.user.UserSummary;
import com.pickyboy.blingBackend.mapper.UserFollowsMapper;
import com.pickyboy.blingBackend.service.IUserFollowsService;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 用户关注表 服务实现类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Service
@RequiredArgsConstructor
public class UserFollowsServiceImpl extends ServiceImpl<UserFollowsMapper, UserFollows> implements IUserFollowsService {
    private final UserFollowsMapper userFollowsMapper;

    @Override
    public List<UserSummary> getUserFollowing(Long userId, Integer offset, Integer limit) {
        return userFollowsMapper.getUserFollowing(userId, offset, limit);
    }

    @Override
    public List<UserSummary> getUserFollowers(Long userId, Integer offset, Integer limit) {
        return userFollowsMapper.getUserFollowers(userId, offset, limit);
    }
}
