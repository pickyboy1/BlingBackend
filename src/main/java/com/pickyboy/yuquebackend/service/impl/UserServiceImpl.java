package com.pickyboy.yuquebackend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.yuquebackend.common.constants.LoginConstants;
import com.pickyboy.yuquebackend.common.exception.BusinessException;
import com.pickyboy.yuquebackend.common.exception.ErrorCode;
import com.pickyboy.yuquebackend.common.utils.CurrentHolder;
import com.pickyboy.yuquebackend.common.utils.JwtUtil;
import com.pickyboy.yuquebackend.common.utils.PasswordUtil;
import com.pickyboy.yuquebackend.domain.dto.user.LoginRequest;
import com.pickyboy.yuquebackend.domain.dto.user.RegisterRequest;
import com.pickyboy.yuquebackend.domain.dto.user.UpdateUserRequest;
import com.pickyboy.yuquebackend.domain.entity.KnowledgeBases;
import com.pickyboy.yuquebackend.domain.entity.Users;
import com.pickyboy.yuquebackend.domain.vo.user.ActivityRecord;
import com.pickyboy.yuquebackend.domain.vo.user.AuthResponse;
import com.pickyboy.yuquebackend.domain.vo.user.UserPublicProfile;
import com.pickyboy.yuquebackend.domain.vo.user.UserSummary;
import com.pickyboy.yuquebackend.mapper.UsersMapper;
import com.pickyboy.yuquebackend.service.IKnowledgeBaseService;
import com.pickyboy.yuquebackend.service.IUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户服务实现类
 *
 * @author pickyboy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUserService {

    private final IKnowledgeBaseService knowledgeBaseService;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public boolean register(RegisterRequest registerRequest) {
        log.info("执行用户注册: registerType={}", registerRequest.getRegisterType());

        // 检查用户是否已存在
        if (LoginConstants.USERNAME.equals(registerRequest.getRegisterType())) {
            boolean exists = lambdaQuery()
                    .eq(Users::getUsername, registerRequest.getIdentifier())
                    .exists();
            if (exists) {
                throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
            }
        } else if (LoginConstants.PHONE.equals(registerRequest.getRegisterType())) {
            // TODO: 实现手机号注册逻辑
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "手机号注册功能暂未开放");
        }

        // 创建新用户
        Users user = new Users();
        user.setUsername(registerRequest.getIdentifier());

        // 对密码进行加密
        String encryptedPassword = PasswordUtil.encryptPassword(registerRequest.getPassword());
        user.setPasswordHash(encryptedPassword);
        user.setNickname(registerRequest.getIdentifier()); // 默认昵称为用户名

        boolean saved = save(user);
        log.info("用户注册结果: success={}", saved);
        return saved;
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("用户登录: loginType={}", loginRequest.getLoginType());

        Users user = null;
        // 根据登录类型查找用户
        if (LoginConstants.USERNAME.equals(loginRequest.getLoginType())) {
            user = lambdaQuery()
                    .eq(Users::getUsername, loginRequest.getIdentifier())
                    .one();
        } else if (LoginConstants.PHONE.equals(loginRequest.getLoginType())) {
            // TODO: 实现手机号登录逻辑
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "手机号登录功能暂未开放");
        }

        // 检查用户是否存在
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        // 验证密码
        if (!PasswordUtil.matches(loginRequest.getCredential(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD, "密码错误");
        }

        // 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        AuthResponse response = new AuthResponse(token, user.getUsername(), user.getNickname(), user.getAvatarUrl());

        log.info("用户登录成功: userId={}", user.getId());
        return response;
    }

    @Override
    public Users getCurrentUser() {
        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Users user = getById(currentUserId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        return user;
    }

    @Override
    @Transactional
    public Users updateCurrentUser(UpdateUserRequest updateRequest) {
        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Users user = getById(currentUserId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 更新用户信息
        if (updateRequest.getNickname() != null) {
            user.setNickname(updateRequest.getNickname());
        }
        if (updateRequest.getAvatarUrl() != null) {
            user.setAvatarUrl(updateRequest.getAvatarUrl());
        }
        if (updateRequest.getDescription() != null) {
            user.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getLocation() != null) {
            user.setLocation(updateRequest.getLocation());
        }
        if (updateRequest.getField() != null) {
            user.setField(updateRequest.getField());
        }

        updateById(user);
        return user;
    }

    @Override
    public List<KnowledgeBases> getUserPublicKnowledgeBases(Long userId) {
        log.info("获取用户公开知识库: userId={}", userId);
        return knowledgeBaseService.getUserPublicKnowledgeBases(userId);
    }

    @Override
    public UserPublicProfile getUserPublicProfile(Long userId) {
        log.info("获取用户公开信息: userId={}", userId);

        Users user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查用户状态
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        UserPublicProfile userPublicProfile = new UserPublicProfile();
        userPublicProfile.setId(user.getId());
        userPublicProfile.setNickname(user.getNickname());
        userPublicProfile.setAvatarUrl(user.getAvatarUrl());
        userPublicProfile.setDescription(user.getDescription());
        userPublicProfile.setLocation(user.getLocation());
        userPublicProfile.setFollowerCount(user.getFollowerCount());
        userPublicProfile.setFollowedCount(user.getFollowedCount());

        // 获取用户公开知识库
        try {
            userPublicProfile.setKnowledgeBases(knowledgeBaseService.getUserPublicKnowledgeBases(userId));
        } catch (Exception e) {
            log.warn("获取用户公开知识库失败: userId={}", userId, e);
            userPublicProfile.setKnowledgeBases(List.of()); // 设置为空列表
        }

        return userPublicProfile;
    }

    @Override
    public void followUser(Long userId) {
        log.info("关注用户: userId={}", userId);
        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        if (currentUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能关注自己");
        }

        // TODO: 实现关注用户逻辑
        // 1. 检查目标用户是否存在
        // 2. 检查是否已经关注
        // 3. 插入关注记录到 user_follows 表
        log.warn("关注用户功能暂未实现");
    }

    @Override
    public void unfollowUser(Long userId) {
        log.info("取消关注用户: userId={}", userId);
        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // TODO: 实现取消关注用户逻辑
        // 1. 检查是否已关注该用户
        // 2. 删除关注记录
        log.warn("取消关注用户功能暂未实现");
    }

    @Override
    public List<UserSummary> getUserFollowing(Long userId, Integer page, Integer limit) {
        log.info("获取用户关注列表: userId={}, page={}, limit={}", userId, page, limit);

        // TODO: 实现获取关注列表逻辑
        // 1. 从 user_follows 表查询该用户关注的用户列表
        // 2. 分页查询
        // 3. 转换为 UserSummary VO
        log.warn("获取用户关注列表功能暂未实现");
        return List.of();
    }

    @Override
    public List<UserSummary> getUserFollowers(Long userId, Integer page, Integer limit) {
        log.info("获取用户粉丝列表: userId={}, page={}, limit={}", userId, page, limit);

        // TODO: 实现获取粉丝列表逻辑
        // 1. 从 user_follows 表查询关注该用户的用户列表
        // 2. 分页查询
        // 3. 转换为 UserSummary VO
        log.warn("获取用户粉丝列表功能暂未实现");
        return List.of();
    }

    @Override
    public List<ActivityRecord> getUserViewHistory(Integer page, Integer limit) {
        log.info("获取用户浏览历史: page={}, limit={}", page, limit);
        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // TODO: 实现获取浏览历史逻辑
        // 1. 从 view_histories 表查询该用户的浏览记录
        // 2. 关联 resources、knowledge_bases、users 表获取完整信息
        // 3. 分页查询，按浏览时间倒序
        // 4. 转换为 ActivityRecord VO
        log.warn("获取用户浏览历史功能暂未实现");
        return List.of();
    }

    @Override
    public List<ActivityRecord> getUserLikeHistory(Integer page, Integer limit) {
        log.info("获取用户点赞历史: page={}, limit={}", page, limit);
        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // TODO: 实现获取点赞历史逻辑
        // 1. 从 likes 表查询该用户的点赞记录
        // 2. 关联 resources、knowledge_bases、users 表获取完整信息
        // 3. 分页查询，按点赞时间倒序
        // 4. 转换为 ActivityRecord VO
        log.warn("获取用户点赞历史功能暂未实现");
        return List.of();
    }

    @Override
    public List<ActivityRecord> getUserCommentHistory(Integer page, Integer limit) {
        log.info("获取用户评论历史: page={}, limit={}", page, limit);
        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // TODO: 实现获取评论历史逻辑
        // 1. 从 comments 表查询该用户的评论记录
        // 2. 关联 resources、knowledge_bases、users 表获取完整信息
        // 3. 分页查询，按评论时间倒序
        // 4. 转换为 ActivityRecord VO
        log.warn("获取用户评论历史功能暂未实现");
        return List.of();
    }
}