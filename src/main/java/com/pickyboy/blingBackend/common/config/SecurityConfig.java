package com.pickyboy.blingBackend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security配置类
 * 提供密码加密器等Security相关的Bean
 *
 * @author pickyboy
 */
@Configuration
public class SecurityConfig {

    /**
     * 密码加密器Bean
     * 使用BCrypt加密算法
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}