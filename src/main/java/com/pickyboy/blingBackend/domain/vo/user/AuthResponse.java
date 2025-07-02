package com.pickyboy.blingBackend.domain.vo.user;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 认证响应VO
 *
 * @author pickyboy
 */
@Data
@AllArgsConstructor
public class AuthResponse {

    /**
     * JWT认证令牌
     */
    private String token;

    /**
     * 用户信息
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatarUrl;
}
