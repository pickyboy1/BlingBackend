package com.pickyboy.yuquebackend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pickyboy.yuquebackend.common.response.Result;
import com.pickyboy.yuquebackend.domain.dto.user.LoginRequest;
import com.pickyboy.yuquebackend.domain.dto.user.RegisterRequest;
import com.pickyboy.yuquebackend.domain.dto.user.UpdateUserRequest;
import com.pickyboy.yuquebackend.domain.entity.KnowledgeBases;
import com.pickyboy.yuquebackend.domain.entity.Users;
import com.pickyboy.yuquebackend.domain.vo.user.AuthResponse;
import com.pickyboy.yuquebackend.domain.vo.user.UserPublicProfile;
import com.pickyboy.yuquebackend.service.IKnowledgeBaseService;
import com.pickyboy.yuquebackend.service.IUserService;

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
     * @return 当前用户信息
     */
    @GetMapping("/user/profile")
    public Result<Users> getCurrentUser() {
        log.info("获取当前用户信息");
        Users user = userService.getCurrentUser();
        return Result.success(user);
    }

    /**
     * 更新当前登录用户信息
     * PUT /user/profile
     *
     * @param updateRequest 更新请求
     * @return 更新后的用户信息
     */
    @PutMapping("/user/profile")
    public Result<Users> updateCurrentUser(@Valid @RequestBody UpdateUserRequest updateRequest) {
        log.info("更新当前用户信息");
        Users user = userService.updateCurrentUser(updateRequest);
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

// todo:
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
     * 获取我的浏览历史
     * GET /me/history
     *
     * @return 浏览历史列表
     */
    @GetMapping("/me/history")
    public Result<List<?>> getUserHistory() {
        log.info("获取用户浏览历史");
        List<?> history = userService.getUserHistory();
        return Result.success(history);
    }

    /**
     * 获取我点赞过的文章列表
     * GET /me/likes
     *
     * @return 点赞文章列表
     */
    @GetMapping("/me/likes")
    public Result<List<?>> getUserLikes() {
        log.info("获取用户点赞文章列表");
        List<?> likes = userService.getUserLikes();
        return Result.success(likes);
    }

    /**
     * 获取我发表过的评论列表
     * GET /me/comments
     *
     * @return 用户评论列表
     */
    @GetMapping("/me/comments")
    public Result<List<?>> getUserComments() {
        log.info("获取用户评论列表");
        List<?> comments = userService.getUserComments();
        return Result.success(comments);
    }
}
