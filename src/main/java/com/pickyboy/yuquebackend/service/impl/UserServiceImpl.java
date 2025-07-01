package com.pickyboy.yuquebackend.service.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.pickyboy.yuquebackend.domain.entity.Resources;
import com.pickyboy.yuquebackend.domain.entity.UserFollows;
import com.pickyboy.yuquebackend.domain.entity.Users;
import com.pickyboy.yuquebackend.domain.entity.ViewHistories;
import com.pickyboy.yuquebackend.domain.vo.user.ActivityRecord;
import com.pickyboy.yuquebackend.domain.vo.user.AuthResponse;
import com.pickyboy.yuquebackend.domain.vo.user.UserPublicProfile;
import com.pickyboy.yuquebackend.domain.vo.user.UserSummary;
import com.pickyboy.yuquebackend.mapper.CommentsMapper;
import com.pickyboy.yuquebackend.mapper.LikesMapper;
import com.pickyboy.yuquebackend.mapper.UsersMapper;
import com.pickyboy.yuquebackend.service.IKnowledgeBaseService;
import com.pickyboy.yuquebackend.service.IResourceService;
import com.pickyboy.yuquebackend.service.IUserFollowsService;
import com.pickyboy.yuquebackend.service.IUserService;
import com.pickyboy.yuquebackend.service.IViewHistoriesService;

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
    private final IViewHistoriesService viewHistoriesService;
    private final IResourceService resourceService;
    private final LikesMapper likesMapper;
    private final CommentsMapper commentsMapper;
    private final IUserFollowsService userFollowsService;

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

    @Transactional
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

        // 检查目标用户是否存在
        Users targetUser = getById(userId);
        if (targetUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查是否已经关注
        boolean isFollowed = userFollowsService.lambdaQuery()
                .eq(UserFollows::getFolloweeId, userId)
                .eq(UserFollows::getFollowerId, currentUserId)
                .exists();
        if (isFollowed) {
            throw new BusinessException(ErrorCode.USER_ALREADY_FOLLOWED);
        }

        // 插入关注记录
        UserFollows userFollows = new UserFollows();
        userFollows.setFolloweeId(userId);
        userFollows.setFollowerId(currentUserId);
        userFollowsService.save(userFollows);

        // 更新关注者数量和被关注者粉丝数量
        targetUser.setFollowerCount(targetUser.getFollowerCount() + 1);
        Users currentUser = getById(currentUserId);
        currentUser.setFollowedCount(currentUser.getFollowedCount() + 1);
        // 更新用户信息
        updateById(targetUser);
        updateById(currentUser);
        log.info("关注用户成功: userId={}, currentUserId={}", userId, currentUserId);
    }

    @Transactional
    @Override
    public void unfollowUser(Long userId) {
        log.info("取消关注用户: userId={}", userId);
        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        if (currentUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能取消关注自己");
        }

        // 检查是否关注该用户
        boolean isFollowed = userFollowsService.lambdaQuery()
                .eq(UserFollows::getFolloweeId, userId)
                .eq(UserFollows::getFollowerId, currentUserId)
                .exists();
        if (!isFollowed) {
            throw new BusinessException(ErrorCode.USER_ALREADY_UNFOLLOWED);
        }

        // 删除关注记录
        userFollowsService.remove(new LambdaQueryWrapper<UserFollows>()
                .eq(UserFollows::getFolloweeId, userId)
                .eq(UserFollows::getFollowerId, currentUserId));

        // 更新关注者数量和被关注者粉丝数量
        Users targetUser = getById(userId);
        targetUser.setFollowerCount(targetUser.getFollowerCount() - 1);
        Users currentUser = getById(currentUserId);
        currentUser.setFollowedCount(currentUser.getFollowedCount() - 1);
        // 更新用户信息
        updateById(targetUser);
        updateById(currentUser);
        log.info("取消关注用户成功: userId={}, currentUserId={}", userId, currentUserId);
    }

    @Override
    public List<UserSummary> getUserFollowing(Long userId, Integer page, Integer limit) {
        log.info("获取用户关注列表: userId={}, page={}, limit={}", userId, page, limit);

        List<UserSummary> userSummaries = userFollowsService.getUserFollowing(userId, (page - 1) * limit, limit);
        return userSummaries;
    }

    @Override
    public List<UserSummary> getUserFollowers(Long userId, Integer page, Integer limit) {
        log.info("获取用户粉丝列表: userId={}, page={}, limit={}", userId, page, limit);

        List<UserSummary> userSummaries = userFollowsService.getUserFollowers(userId, (page - 1) * limit, limit);
        return userSummaries;
    }

    @Override
    public List<ActivityRecord> getUserViewHistory(Integer page, Integer limit) {
        log.info("获取用户浏览历史: page={}, limit={}", page, limit);
        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 1. 从 view_histories 表查询该用户的浏览记录
        List<ViewHistories> viewHistories = viewHistoriesService.page(new Page<>(page, limit),
                new LambdaQueryWrapper<ViewHistories>()
                        .eq(ViewHistories::getUserId, currentUserId)
                        .orderByDesc(ViewHistories::getLastViewAt))
                .getRecords();

        // 2. 转换为 ActivityRecord VO
        List<ActivityRecord> activityRecords = viewHistories.stream()
                .map(viewHistory -> {
                    ActivityRecord activityRecord = new ActivityRecord();
                    activityRecord.setResourceId(viewHistory.getResourceId());
                    activityRecord.setActionAt(viewHistory.getLastViewAt());
                    return activityRecord;
                })
                .collect(Collectors.toList());
        // 3. 关联 resources、knowledge_bases、users 表获取完整信息
        // 资源id列表
        List<Long> resourceIds = activityRecords.stream()
                .map(ActivityRecord::getResourceId)
                .collect(Collectors.toList());
        // 资源列表
        List<Resources> resources = resourceService.listByIds(resourceIds);
        // 资源id与资源映射,用于快速查找资源
        Map<Long, Resources> resourceMap = resources.stream()
                .collect(Collectors.toMap(Resources::getId, Function.identity()));
        // 知识库id列表
        List<Long> knowledgeBaseIds = resources.stream()
                .map(Resources::getKnowledgeBaseId)
                .collect(Collectors.toList());
        // 知识库列表
        List<KnowledgeBases> knowledgeBases = knowledgeBaseService.listByIds(knowledgeBaseIds);
        // 知识库id与知识库映射,用于快速查找知识库
        Map<Long, KnowledgeBases> knowledgeBaseMap = knowledgeBases.stream()
                .collect(Collectors.toMap(KnowledgeBases::getId, Function.identity()));

        // 作者id列表
        List<Long> userIds = resources.stream()
                .map(Resources::getUserId)
                .collect(Collectors.toList());
        // 作者列表
        List<Users> users = listByIds(userIds);
        // 作者id与作者映射,用于快速查找作者
        Map<Long, Users> userMap = users.stream()
                .collect(Collectors.toMap(Users::getId, Function.identity()));
        // 填充信息
        activityRecords.forEach(activityRecord -> {
            Resources resource = resourceMap.get(activityRecord.getResourceId());
            if (resource != null) {
                activityRecord.setResourceTitle(resource.getTitle());
                activityRecord.setResourceType(resource.getType());
                activityRecord.setKbId(resource.getKnowledgeBaseId());
                activityRecord.setKbName(knowledgeBaseMap.get(resource.getKnowledgeBaseId()).getName());
                activityRecord.setAuthorId(resource.getUserId());
                activityRecord.setAuthorName(userMap.get(resource.getUserId()).getNickname());
            }
        });
        return activityRecords;
    }

    @Override
    public List<ActivityRecord> getUserLikeHistory(Integer page, Integer limit) {
        log.info("获取用户点赞历史: page={}, limit={}", page, limit);
        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        List<ActivityRecord> activityRecords = likesMapper.likeHistory(currentUserId, (page - 1) * limit, limit);
        return activityRecords;
    }

    @Override
    public List<ActivityRecord> getUserCommentHistory(Integer page, Integer limit) {
        log.info("获取用户评论历史: page={}, limit={}", page, limit);
        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        List<ActivityRecord> activityRecords = commentsMapper.commentHistory(currentUserId, (page - 1) * limit, limit);
        return activityRecords;
    }
}