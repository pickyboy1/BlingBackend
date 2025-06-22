package com.pickyboy.yuquebackend.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus自动填充处理器
 * 用于自动设置created_at和updated_at字段
 *
 * @author pickyboy
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充... 表名: {}", metaObject.getOriginalObject().getClass().getSimpleName());

        // 插入时自动填充创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();

        // 检查字段是否存在
        if (metaObject.hasGetter("createdAt")) {
            this.setFieldValByName("createdAt", now, metaObject);
            log.info("设置 createdAt: {}", now);
        }

        if (metaObject.hasGetter("updatedAt")) {
            this.setFieldValByName("updatedAt", now, metaObject);
            log.info("设置 updatedAt: {}", now);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充... 表名: {}", metaObject.getOriginalObject().getClass().getSimpleName());

        LocalDateTime now = LocalDateTime.now();

        // 检查字段是否存在
        if (metaObject.hasGetter("updatedAt")) {
            // 使用非严格模式，强制设置字段值
            this.setFieldValByName("updatedAt", now, metaObject);
            log.info("设置 updatedAt: {}", now);
        } else {
            log.warn("updatedAt 字段不存在于 {}", metaObject.getOriginalObject().getClass().getSimpleName());
        }
    }
}