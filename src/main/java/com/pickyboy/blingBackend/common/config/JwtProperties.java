package com.pickyboy.blingBackend.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * JWT配置属性类
 *
 * @author pickyboy
 */
@Data
@Component
@ConfigurationProperties(prefix = "yuque.jwt")
public class JwtProperties {

    /**
     * JWT签名密钥
     */
    private String secretKey = "yuque-very-long-secret-key-for-jwt-2025";

    /**
     * Token过期时间（小时）
     */
    private Integer expireHours = 24;

    /**
     * Token刷新阈值（小时）
     * 当Token剩余时间少于此值时，可以刷新Token
     */
    private Integer refreshThresholdHours = 2;

    /**
     * Token前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Token请求头名称
     */
    private String headerName = "Authorization";

    /**
     * 是否启用Token自动刷新
     */
    private Boolean autoRefresh = true;
}