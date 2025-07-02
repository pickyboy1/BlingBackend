package com.pickyboy.blingBackend.common.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.pickyboy.blingBackend.common.config.JwtProperties;
import com.pickyboy.blingBackend.common.context.UserContext;
import com.pickyboy.blingBackend.common.exception.JwtException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT工具类
 *
 * @author pickyboy
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    /**
     * 生成JWT Token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return 生成的Token
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpireHours() * 60 * 60 * 1000L);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("iat", now.getTime());

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 刷新Token（如果满足刷新条件）
     *
     * @param token 原Token
     * @return 新Token，如果不需要刷新则返回原Token
     */
    public String refreshTokenIfNeeded(String token) {
        if (!jwtProperties.getAutoRefresh()) {
            return token;
        }

        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();

            // 检查是否需要刷新（剩余时间少于阈值）
            long remainingTime = expiration.getTime() - now.getTime();
            long thresholdTime = jwtProperties.getRefreshThresholdHours() * 60 * 60 * 1000L;

            if (remainingTime < thresholdTime && remainingTime > 0) {
                // 生成新Token
                Long userId = ((Number) claims.get("userId")).longValue();
                String username = (String) claims.get("username");

                log.info("Token即将过期，自动刷新. 用户: {}, 剩余时间: {}ms", username, remainingTime);
                return generateToken(userId, username);
            }

            return token;
        } catch (Exception e) {
            log.warn("Token刷新失败: {}", e.getMessage());
            return token;
        }
    }

    /**
     * 解析JWT Token
     *
     * @param token JWT字符串
     * @return 解析后的Claims
     * @throws JwtException Token解析异常
     */
    public Claims parseToken(String token) {
        try {
        return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            throw new JwtException.TokenExpiredException("Token已过期");
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的Token格式: {}", e.getMessage());
            throw new JwtException.TokenInvalidException("不支持的Token格式");
        } catch (MalformedJwtException e) {
            log.warn("Token格式错误: {}", e.getMessage());
            throw new JwtException.TokenInvalidException("Token格式错误");
        } catch (SignatureException e) {
            log.warn("Token签名验证失败: {}", e.getMessage());
            throw new JwtException.TokenInvalidException("Token签名验证失败");
        } catch (IllegalArgumentException e) {
            log.warn("Token为空或格式不正确: {}", e.getMessage());
            throw new JwtException.TokenInvalidException("Token为空或格式不正确");
        }
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT字符串
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 从Token中获取用户上下文信息
     *
     * @param token JWT字符串
     * @return 用户上下文信息
     */
    public UserContext getUserContext(String token) {
        Claims claims = parseToken(token);

        Long userId = ((Number) claims.get("userId")).longValue();
        String username = (String) claims.get("username");
        Long issuedAt = ((Number) claims.get("iat")).longValue();
        Long expiration = claims.getExpiration().getTime();

        return new UserContext(userId, username, issuedAt, expiration);
    }

    /**
     * 从HTTP请求头中提取Token
     *
     * @param request HTTP请求
     * @return Token字符串，如果不存在返回null
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtProperties.getHeaderName());

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtProperties.getTokenPrefix())) {
            return bearerToken.substring(jwtProperties.getTokenPrefix().length());
        }

        return null;
    }

    /**
     * 从Token中提取用户ID
     *
     * @param token JWT字符串
     * @return 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return ((Number) claims.get("userId")).longValue();
    }

    /**
     * 从Token中提取用户名
     *
     * @param token JWT字符串
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("username");
    }



    /**
     * 检查Token是否即将过期
     *
     * @param token JWT字符串
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();

            long remainingTime = expiration.getTime() - now.getTime();
            long thresholdTime = jwtProperties.getRefreshThresholdHours() * 60 * 60 * 1000L;

            return remainingTime < thresholdTime && remainingTime > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取Token剩余有效时间（毫秒）
     *
     * @param token JWT字符串
     * @return 剩余有效时间，如果已过期返回0
     */
    public long getRemainingTime(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();

            long remainingTime = expiration.getTime() - now.getTime();
            return Math.max(0, remainingTime);
        } catch (Exception e) {
            return 0;
        }
    }
}
