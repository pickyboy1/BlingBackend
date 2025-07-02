package com.pickyboy.blingBackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.blingBackend.common.constants.LoginConstants;
import com.pickyboy.blingBackend.common.exception.BusinessException;
import com.pickyboy.blingBackend.common.exception.ErrorCode;
import com.pickyboy.blingBackend.common.utils.CurrentHolder;
import com.pickyboy.blingBackend.common.utils.JwtUtil;
import com.pickyboy.blingBackend.common.utils.PasswordUtil;
import com.pickyboy.blingBackend.domain.dto.user.LoginRequest;
import com.pickyboy.blingBackend.domain.dto.user.RegisterRequest;
import com.pickyboy.blingBackend.domain.dto.user.UpdateUserRequest;
import com.pickyboy.blingBackend.domain.entity.KnowledgeBases;
import com.pickyboy.blingBackend.domain.entity.UserFollows;
import com.pickyboy.blingBackend.domain.entity.Users;
import com.pickyboy.blingBackend.domain.vo.user.ActivityRecord;
import com.pickyboy.blingBackend.domain.vo.user.AuthResponse;
import com.pickyboy.blingBackend.domain.vo.user.UserProfileVO;
import com.pickyboy.blingBackend.domain.vo.user.UserPublicProfile;
import com.pickyboy.blingBackend.domain.vo.user.UserSummary;
import com.pickyboy.blingBackend.mapper.CommentsMapper;
import com.pickyboy.blingBackend.mapper.LikesMapper;
import com.pickyboy.blingBackend.mapper.UsersMapper;
import com.pickyboy.blingBackend.mapper.ViewHistoriesMapper;
import com.pickyboy.blingBackend.service.IKnowledgeBaseService;
import com.pickyboy.blingBackend.service.IResourceService;
import com.pickyboy.blingBackend.service.IUserFollowsService;
import com.pickyboy.blingBackend.service.IUserService;
import com.pickyboy.blingBackend.service.IViewHistoriesService;

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
    private final ViewHistoriesMapper viewHistoriesMapper;

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

        // 更新用户最后登录时间
        user.setLastLogin(LocalDateTime.now());
        updateById(user);

        log.info("用户登录成功: userId={}", user.getId());
        return response;
    }

    @Override
    public UserProfileVO getCurrentUser() {
        Long currentUserId = CurrentHolder.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Users user = getById(currentUserId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        return convertToUserProfileVO(user);
    }

    @Override
    @Transactional
    public UserProfileVO updateCurrentUser(UpdateUserRequest updateRequest) {
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
        return convertToUserProfileVO(user);
    }

    /**
     * 将Users实体转换为UserProfileVO（安全版本，不包含敏感数据）
     *
     * @param user Users实体
     * @return UserProfileVO
     */
    private UserProfileVO convertToUserProfileVO(Users user) {
        UserProfileVO userProfileVO = new UserProfileVO();
        userProfileVO.setId(user.getId());
        userProfileVO.setUsername(user.getUsername());
        userProfileVO.setNickname(user.getNickname());
        userProfileVO.setPhone(user.getPhone());
        userProfileVO.setAvatarUrl(user.getAvatarUrl());
        userProfileVO.setDescription(user.getDescription());
        userProfileVO.setLocation(user.getLocation());
        userProfileVO.setField(user.getField());
        userProfileVO.setFollowerCount(user.getFollowerCount());
        userProfileVO.setFollowedCount(user.getFollowedCount());
        userProfileVO.setLastLogin(user.getLastLogin());
        userProfileVO.setCreatedAt(user.getCreatedAt());
        userProfileVO.setUpdatedAt(user.getUpdatedAt());
        return userProfileVO;
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

        Long currentUserId = CurrentHolder.getCurrentUserId();

        // 检查当前用户是否已关注目标用户（仅在已登录时检查）
        boolean isFollowed = false;
        if (currentUserId != null) {
            isFollowed = userFollowsService.getOne(new LambdaQueryWrapper<UserFollows>()
                    .eq(UserFollows::getFollowerId, currentUserId)
                    .eq(UserFollows::getFolloweeId, userId)) != null;
        }

        UserPublicProfile userPublicProfile = new UserPublicProfile();
        userPublicProfile.setId(user.getId());
        userPublicProfile.setNickname(user.getNickname());
        userPublicProfile.setAvatarUrl(user.getAvatarUrl());
        userPublicProfile.setDescription(user.getDescription());
        userPublicProfile.setLocation(user.getLocation());
        userPublicProfile.setFollowerCount(user.getFollowerCount());
        userPublicProfile.setFollowedCount(user.getFollowedCount());
        userPublicProfile.setIsFollowed(isFollowed);

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

        // 【修复并发问题】原子操作更新关注者数量和被关注者粉丝数量
        baseMapper.incrementFollowerCount(userId);
        baseMapper.incrementFollowedCount(currentUserId);
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

        // 【修复并发问题】原子操作更新关注者数量和被关注者粉丝数量
        baseMapper.decrementFollowerCount(userId);
        baseMapper.decrementFollowedCount(currentUserId);
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

        Integer offset = (page - 1) * limit;
        List<ActivityRecord> activityRecords = viewHistoriesMapper.getUserViewHistory(currentUserId, offset, limit);
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