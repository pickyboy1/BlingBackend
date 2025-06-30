package com.pickyboy.yuquebackend.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.pickyboy.yuquebackend.domain.vo.user.AuthResponse;
import com.pickyboy.yuquebackend.domain.vo.user.UserPublicProfile;
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

    private final JwtUtil jwtUtil;
    private final IKnowledgeBaseService knowledgeBaseService;

    @Override
    public boolean register(RegisterRequest registerRequest) {
        log.info("执行用户注册: registerType={}", registerRequest.getRegisterType());

        // 验证注册类型
        if (!LoginConstants.USERNAME.equals(registerRequest.getRegisterType()) &&
            !LoginConstants.PHONE.equals(registerRequest.getRegisterType())) {
            throw new BusinessException(ErrorCode.INVALID_REGISTER_TYPE);
        }

        if (LoginConstants.USERNAME.equals(registerRequest.getRegisterType())) {
            // 检查用户名是否已存在
            Optional<Users> existingUser = getOneOpt(
                new LambdaQueryWrapper<Users>().eq(Users::getUsername, registerRequest.getIdentifier())
            );
            if (existingUser.isPresent()) {
                throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
            }

            // 创建新用户
            Users user = new Users();
            user.setUsername(registerRequest.getIdentifier());
            user.setPasswordHash(PasswordUtil.encryptPassword(registerRequest.getPassword()));
            user.setNickname(registerRequest.getIdentifier());

            boolean saved = save(user);
            if (!saved) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户注册失败");
            }

            log.info("用户注册成功: username={}", registerRequest.getIdentifier());
            return true;
        }

        if (LoginConstants.PHONE.equals(registerRequest.getRegisterType())) {
            // TODO: 实现手机号注册逻辑
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "手机号注册功能暂未开放");
        }

        return false;
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("执行用户登录: loginType={}", loginRequest.getLoginType());

        // 验证登录类型
        if (!LoginConstants.USERNAME.equals(loginRequest.getLoginType()) &&
            !LoginConstants.PHONE.equals(loginRequest.getLoginType())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN_TYPE);
        }

        if (LoginConstants.USERNAME.equals(loginRequest.getLoginType())) {
            // 查找用户
            Optional<Users> userOpt = getOneOpt(
                new LambdaQueryWrapper<Users>().eq(Users::getUsername, loginRequest.getIdentifier())
            );

            if (!userOpt.isPresent()) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户名不存在");
            }

            Users user = userOpt.get();

            // 检查用户状态
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                throw new BusinessException(ErrorCode.USER_DISABLED, "用户已被删除");
            }

            // 验证密码
            String encryptedPassword = PasswordUtil.encryptPassword(loginRequest.getCredential());
            if (!user.getPasswordHash().equals(encryptedPassword)) {
                throw new BusinessException(ErrorCode.INVALID_PASSWORD);
            }

            // 生成Token并返回用户信息
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());

            log.info("用户登录成功: username={}", user.getUsername());
            return new AuthResponse(token, user.getUsername(), user.getNickname(), user.getAvatarUrl());
        }

        if (LoginConstants.PHONE.equals(loginRequest.getLoginType())) {
            // TODO: 实现手机号登录逻辑
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "手机号登录功能暂未开放");
        }

        throw new BusinessException(ErrorCode.INVALID_LOGIN_TYPE);
    }

    @Override
    public Users getCurrentUser() {
        log.info("获取当前登录用户信息");
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Users user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查用户状态
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new BusinessException(ErrorCode.USER_DISABLED, "用户已被删除");
        }

        return user;
    }

    @Override
    public Users updateCurrentUser(UpdateUserRequest updateRequest) {
        log.info("更新当前用户信息");
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Users user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查用户状态
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new BusinessException(ErrorCode.USER_DISABLED, "用户已被删除");
        }

        // 更新用户信息（只更新非空字段）
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

        boolean updated = updateById(user);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户信息更新失败");
        }

        log.info("用户信息更新成功: userId={}", userId);
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

        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }

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

        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }

        if (currentUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "不能关注自己");
        }

        // 检查目标用户是否存在
        Users targetUser = getById(userId);
        if (targetUser == null || Boolean.TRUE.equals(targetUser.getIsDeleted())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // TODO: 实现关注用户逻辑
        throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "关注功能暂未实现");
    }

    @Override
    public void unfollowUser(Long userId) {
        log.info("取消关注用户: userId={}", userId);

        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }

        // TODO: 实现取消关注用户逻辑
        throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "取消关注功能暂未实现");
    }

    @Override
    public List<?> getUserHistory() {
        log.info("获取用户浏览历史");

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // TODO: 实现获取用户浏览历史逻辑
        throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "浏览历史功能暂未实现");
    }

    @Override
    public List<?> getUserLikes() {
        log.info("获取用户点赞文章列表");

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // TODO: 实现获取用户点赞文章列表逻辑
        throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "点赞列表功能暂未实现");
    }

    @Override
    public List<?> getUserComments() {
        log.info("获取用户评论列表");

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // TODO: 实现获取用户评论列表逻辑
        throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "评论列表功能暂未实现");
    }
}