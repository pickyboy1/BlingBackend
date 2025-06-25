package com.pickyboy.yuquebackend.service;

import java.util.List;

import com.pickyboy.yuquebackend.domain.dto.LoginRequest;
import com.pickyboy.yuquebackend.domain.dto.RegisterRequest;
import com.pickyboy.yuquebackend.domain.dto.UpdateUserRequest;
import com.pickyboy.yuquebackend.domain.entity.Users;
import com.pickyboy.yuquebackend.domain.vo.AuthResponse;
import com.pickyboy.yuquebackend.domain.vo.UserPublicProfile;

/**
 * 用户服务接口
 *
 * @author pickyboy
 */
public interface IUserService {

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 认证响应
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
     * @return 用户信息
     */
    Users getCurrentUser();

    /**
     * 更新当前用户信息
     *
     * @param updateRequest 更新请求
     * @return 更新后的用户信息
     */
    Users updateCurrentUser(UpdateUserRequest updateRequest);

    /**
     * 获取指定用户的公开信息
     *
     * @param userId 用户ID
     * @return 用户公开信息
     */
    UserPublicProfile getUserPublicProfile(Long userId);

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
     * 获取用户浏览历史
     *
     * @return 浏览历史列表
     */
    List<?> getUserHistory();

    /**
     * 获取用户点赞的文章列表
     *
     * @return 点赞文章列表
     */
    List<?> getUserLikes();

    /**
     * 获取用户发表的评论列表
     *
     * @return 用户评论列表
     */
    List<?> getUserComments();
}