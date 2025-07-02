package com.pickyboy.blingBackend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pickyboy.blingBackend.common.response.Result;
import com.pickyboy.blingBackend.domain.dto.user.LoginRequest;
import com.pickyboy.blingBackend.domain.dto.user.RegisterRequest;
import com.pickyboy.blingBackend.domain.dto.user.UpdateUserRequest;
import com.pickyboy.blingBackend.domain.entity.KnowledgeBases;
import com.pickyboy.blingBackend.domain.vo.user.ActivityRecord;
import com.pickyboy.blingBackend.domain.vo.user.AuthResponse;
import com.pickyboy.blingBackend.domain.vo.user.UserProfileVO;
import com.pickyboy.blingBackend.domain.vo.user.UserPublicProfile;
import com.pickyboy.blingBackend.domain.vo.user.UserSummary;
import com.pickyboy.blingBackend.service.IKnowledgeBaseService;
import com.pickyboy.blingBackend.service.IUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户控制器
 * 处理用户相关的API请求
 *
 * @author pickyboy
 */
@Slf4j
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final IKnowledgeBaseService knowledgeBaseService;

    /**
     * 用户注册
     * POST /auth/register
     *
     * @param registerRequest 注册请求
     * @return 注册响应
     */
    @PostMapping("/auth/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("用户注册请求: registerType={}", registerRequest.getRegisterType());
        userService.register(registerRequest);
        return Result.success();
    }

    /**
     * 用户登录
     * POST /auth/login
     *
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @PostMapping("/auth/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户登录请求: loginType={}", loginRequest.getLoginType());
        AuthResponse response = userService.login(loginRequest);
        return Result.success(response);
    }

    /**
     * 获取当前登录用户信息
     * GET /user/profile
     *
     * @return 当前用户信息（安全版本，不包含敏感数据）
     */
    @GetMapping("/user/profile")
    public Result<UserProfileVO> getCurrentUser() {
        log.info("获取当前用户信息");
        UserProfileVO user = userService.getCurrentUser();
        return Result.success(user);
    }

    /**
     * 更新当前登录用户信息
     * PUT /user/profile
     *
     * @param updateRequest 更新请求
     * @return 更新后的用户信息（安全版本，不包含敏感数据）
     */
    @PutMapping("/user/profile")
    public Result<UserProfileVO> updateCurrentUser(@Valid @RequestBody UpdateUserRequest updateRequest) {
        log.info("更新当前用户信息");
        UserProfileVO user = userService.updateCurrentUser(updateRequest);
        return Result.success(user);
    }

    /**
     * 获取指定用户的公开知识库列表
     * GET /user/{userId}/public-kbs
     *
     * @param userId 用户ID
     * @return 用户公开知识库列表
     */
    @GetMapping("/user/{userId}/public-kbs")
    public Result<List<KnowledgeBases>> getUserKnowledgeBases(@PathVariable Long userId) {
        log.info("获取用户公开知识库: userId={}", userId);
        List<KnowledgeBases> knowledgeBases = userService.getUserPublicKnowledgeBases(userId);
        return Result.success(knowledgeBases);
    }

    /**
     * 查看指定用户的公开主页信息
     * GET /users/{userId}/profile
     *
     * @param userId 用户ID
     * @return 用户公开信息
     */
    @GetMapping("/users/{userId}/profile")
    public Result<UserPublicProfile> getUserProfile(@PathVariable Long userId) {
        log.info("查看用户公开信息: userId={}", userId);
        UserPublicProfile profile = userService.getUserPublicProfile(userId);
        return Result.success(profile);
    }

    /**
     * 关注用户
     * POST /users/{userId}/follow
     *
     * @param userId 要关注的用户ID
     * @return 操作结果
     */
    @PostMapping("/users/{userId}/follow")
    public Result<Void> followUser(@PathVariable Long userId) {
        log.info("关注用户: userId={}", userId);
        userService.followUser(userId);
        return Result.success();
    }

    /**
     * 取消关注用户
     * DELETE /users/{userId}/follow
     *
     * @param userId 要取消关注的用户ID
     * @return 操作结果
     */
    @DeleteMapping("/users/{userId}/follow")
    public Result<Void> unfollowUser(@PathVariable Long userId) {
        log.info("取消关注用户: userId={}", userId);
        userService.unfollowUser(userId);
        return Result.success();
    }

    /**
     * 获取用户关注的列表 (Following)
     * GET /users/{userId}/following
     *
     * @param userId 用户ID
     * @param page 页码
     * @param limit 每页数量
     * @return 关注用户列表
     */
    @GetMapping("/users/{userId}/following")
    public Result<List<UserSummary>> getUserFollowing(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        log.info("获取用户关注列表: userId={}, page={}, limit={}", userId, page, limit);
        List<UserSummary> following = userService.getUserFollowing(userId, page, limit);
        for (UserSummary summary : following) {
            summary.setIsFollowing(true);
        }
        return Result.success(following);
    }

    /**
     * 获取用户的粉丝列表 (Followers)
     * GET /users/{userId}/followers
     *
     * @param userId 用户ID
     * @param page 页码
     * @param limit 每页数量
     * @return 粉丝用户列表
     */
    @GetMapping("/users/{userId}/followers")
    public Result<List<UserSummary>> getUserFollowers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        log.info("获取用户粉丝列表: userId={}, page={}, limit={}", userId, page, limit);
        List<UserSummary> followers = userService.getUserFollowers(userId, page, limit);
        return Result.success(followers);
    }

    /**
     * 获取浏览历史记录
     * GET /user/history/views
     *
     * @param page 页码
     * @param limit 每页数量
     * @return 浏览历史列表
     */
    @GetMapping("/user/history/views")
    public Result<List<ActivityRecord>> getUserViewHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        log.info("获取用户浏览历史: page={}, limit={}", page, limit);
        List<ActivityRecord> history = userService.getUserViewHistory(page, limit);
        return Result.success(history);
    }

    /**
     * 获取点赞历史记录
     * GET /user/history/likes
     *
     * @param page 页码
     * @param limit 每页数量
     * @return 点赞文章列表
     */
    @GetMapping("/user/history/likes")
    public Result<List<ActivityRecord>> getUserLikeHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        log.info("获取用户点赞历史: page={}, limit={}", page, limit);
        List<ActivityRecord> likes = userService.getUserLikeHistory(page, limit);
        return Result.success(likes);
    }

    /**
     * 获取评论历史记录
     * GET /user/history/comments
     *
     * @param page 页码
     * @param limit 每页数量
     * @return 用户评论列表
     */
    @GetMapping("/user/history/comments")
    public Result<List<ActivityRecord>> getUserCommentHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        log.info("获取用户评论历史: page={}, limit={}", page, limit);
        List<ActivityRecord> comments = userService.getUserCommentHistory(page, limit);
        return Result.success(comments);
    }
}
