package com.pickyboy.blingBackend.common.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户上下文信息
 *
 * @author pickyboy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;


    /**
     * Token签发时间
     */
    private Long issuedAt;

    /**
     * Token过期时间
     */
    private Long expiration;
}