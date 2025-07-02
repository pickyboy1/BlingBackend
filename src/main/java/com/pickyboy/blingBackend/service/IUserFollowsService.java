package com.pickyboy.blingBackend.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pickyboy.blingBackend.domain.entity.UserFollows;
import com.pickyboy.blingBackend.domain.vo.user.UserSummary;

/**
 * <p>
 * 用户关注表 服务类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
public interface IUserFollowsService extends IService<UserFollows> {
    List<UserSummary> getUserFollowing(Long userId, Integer offset, Integer limit);

    List<UserSummary> getUserFollowers(Long userId, Integer offset, Integer limit);
}
