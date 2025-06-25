package com.pickyboy.yuquebackend.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.yuquebackend.common.constants.LoginConstants;
import com.pickyboy.yuquebackend.common.context.UserContext;
import com.pickyboy.yuquebackend.common.utils.JwtUtil;
import com.pickyboy.yuquebackend.common.utils.PasswordUtil;
import com.pickyboy.yuquebackend.domain.dto.LoginRequest;
import com.pickyboy.yuquebackend.domain.dto.RegisterRequest;
import com.pickyboy.yuquebackend.domain.dto.UpdateUserRequest;
import com.pickyboy.yuquebackend.domain.entity.Users;
import com.pickyboy.yuquebackend.domain.vo.AuthResponse;
import com.pickyboy.yuquebackend.domain.vo.UserPublicProfile;
import com.pickyboy.yuquebackend.mapper.UsersMapper;
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
    private final UserContext userContext;

    @Override
    public boolean register(RegisterRequest registerRequest) {
        log.info("执行用户注册: registerType={}", registerRequest.getRegisterType());
        if(registerRequest.getRegisterType().equals(LoginConstants.USERNAME)){

            Optional<Users> oneOpt = getOneOpt(new LambdaQueryWrapper<Users>().eq(Users::getUsername, registerRequest.getIdentifier()));
            // 用户名已存在,不允许注册
            if(oneOpt.isPresent()){
                return false;
            }
            Users user = new Users();
            user.setUsername(registerRequest.getIdentifier());
            user.setPasswordHash(PasswordUtil.encryptPassword(registerRequest.getPassword()));
            user.setNickname(registerRequest.getIdentifier());
            save(user);
            return true;
        }
        if (registerRequest.getRegisterType().equals(LoginConstants.PHONE)) {
            // todo: 接入手机号注册
            return false;
        }
        return false;
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("执行用户登录: loginType={}", loginRequest.getLoginType());
        if(loginRequest.getLoginType().equals(LoginConstants.USERNAME)){
            Optional<Users> oneOpt = getOneOpt(new LambdaQueryWrapper<Users>().eq(Users::getUsername, loginRequest.getIdentifier()));
            // 用户存在,比较密码
            if(oneOpt.isPresent()){
                Users user = oneOpt.get();
                // 密码不匹配
                if(!user.getPasswordHash().equals(PasswordUtil.encryptPassword(loginRequest.getCredential()))){
                    return null;
                }
                // 密码匹配,生成token,返回用户信息
                String token = jwtUtil.generateToken(user.getId(), user.getUsername());
                return new AuthResponse(token, user.getUsername(), user.getNickname(), user.getAvatarUrl());
            }
        }
        return null;
    }

    @Override
    public Users getCurrentUser() {
        log.info("获取当前登录用户信息");
        Long userId = userContext.getUserId();
        if(userId == null){
            return null;
        }
        return getById(userId);
    }

    @Override
    public Users updateCurrentUser(UpdateUserRequest updateRequest) {
        log.info("更新当前用户信息");
        Long userId = userContext.getUserId();
        if(userId == null){
            return null;
        }
        Users user = getById(userId);
        if(user == null){
            return null;
        }
        user.setNickname(updateRequest.getNickname());
        user.setAvatarUrl(updateRequest.getAvatarUrl());
        user.setDescription(updateRequest.getDescription());
        user.setLocation(updateRequest.getLocation());
        user.setField(updateRequest.getField());
        updateById(user);
        return user;
    }

    @Override
    public UserPublicProfile getUserPublicProfile(Long userId) {
        // TODO: 实现获取用户公开信息逻辑
        log.info("获取用户公开信息: userId={}", userId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public void followUser(Long userId) {
        // TODO: 实现关注用户逻辑
        log.info("关注用户: userId={}", userId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public void unfollowUser(Long userId) {
        // TODO: 实现取消关注用户逻辑
        log.info("取消关注用户: userId={}", userId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public List<?> getUserHistory() {
        // TODO: 实现获取用户浏览历史逻辑
        log.info("获取用户浏览历史");
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public List<?> getUserLikes() {
        // TODO: 实现获取用户点赞文章列表逻辑
        log.info("获取用户点赞文章列表");
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public List<?> getUserComments() {
        // TODO: 实现获取用户评论列表逻辑
        log.info("获取用户评论列表");
        throw new UnsupportedOperationException("待实现");
    }
}