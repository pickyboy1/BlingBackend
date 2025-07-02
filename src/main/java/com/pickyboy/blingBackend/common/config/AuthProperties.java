package com.pickyboy.blingBackend.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 认证过滤器配置属性
 *
 * @author pickyboy
 */
@Data
@Component
@ConfigurationProperties(prefix = "pickyboy.auth")
public class AuthProperties {

    /**
     * 排除路径列表
     */
    private List<String> excludePaths;
}