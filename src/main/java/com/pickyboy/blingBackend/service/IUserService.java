package com.pickyboy.blingBackend.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pickyboy.blingBackend.domain.dto.user.LoginRequest;
import com.pickyboy.blingBackend.domain.dto.user.RegisterRequest;
import com.pickyboy.blingBackend.domain.dto.user.UpdateUserRequest;
import com.pickyboy.blingBackend.domain.entity.KnowledgeBases;
import com.pickyboy.blingBackend.domain.entity.Users;
import com.pickyboy.blingBackend.domain.vo.user.ActivityRecord;
import com.pickyboy.blingBackend.domain.vo.user.AuthResponse;
import com.pickyboy.blingBackend.domain.vo.user.UserProfileVO;
import com.pickyboy.blingBackend.domain.vo.user.UserPublicProfile;
import com.pickyboy.blingBackend.domain.vo.user.UserSummary;

/**
 * 用户服务接口
 *
 * @author pickyboy
 */
public interface IUserService extends IService<Users> {

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    boolean register(RegisterRequest registerRequest);

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 认证响应
     */
    AuthResponse login(LoginRequest loginRequest);

    /**
     * 获取当前用户信息
     *
     * @return 用户信息（安全版本，不包含敏感数据）
     */
    UserProfileVO getCurrentUser();

    /**
     * 更新当前用户信息
     *
     * @param updateRequest 更新请求
     * @return 更新后的用户信息（安全版本，不包含敏感数据）
     */
    UserProfileVO updateCurrentUser(UpdateUserRequest updateRequest);

    /**
     * 获取指定用户的公开信息
     *
     * @param userId 用户ID
     * @return 用户公开信息
     */
    UserPublicProfile getUserPublicProfile(Long userId);

    /**
     * 获取指定用户的公开知识库列表
     *
     * @param userId 用户ID
     * @return 用户公开知识库列表
     */
    List<KnowledgeBases> getUserPublicKnowledgeBases(Long userId);

    /**
     * 关注用户
     *
     * @param userId 要关注的用户ID
     */
    void followUser(Long userId);

    /**
     * 取消关注用户
     *
     * @param userId 要取消关注的用户ID
     */
    void unfollowUser(Long userId);

    /**
     * 获取用户关注的列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param limit 每页数量
     * @return 关注用户列表
     */
    List<UserSummary> getUserFollowing(Long userId, Integer page, Integer limit);

    /**
     * 获取用户的粉丝列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param limit 每页数量
     * @return 粉丝用户列表
     */
    List<UserSummary> getUserFollowers(Long userId, Integer page, Integer limit);

    /**
     * 获取用户浏览历史
     *
     * @param page 页码
     * @param limit 每页数量
     * @return 浏览历史列表
     */
    List<ActivityRecord> getUserViewHistory(Integer page, Integer limit);

    /**
     * 获取用户点赞的文章列表
     *
     * @param page 页码
     * @param limit 每页数量
     * @return 点赞文章列表
     */
    List<ActivityRecord> getUserLikeHistory(Integer page, Integer limit);

    /**
     * 获取用户发表的评论列表
     *
     * @param page 页码
     * @param limit 每页数量
     * @return 用户评论列表
     */
    List<ActivityRecord> getUserCommentHistory(Integer page, Integer limit);
}