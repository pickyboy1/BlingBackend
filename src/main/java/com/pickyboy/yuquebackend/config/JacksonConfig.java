package com.pickyboy.yuquebackend.config; // 建议将该文件放在config包下

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Spring MVC JSON序列化配置
 * <p>
 * 该配置类解决了JavaScript中处理长整型（Long）丢失精度的问题，
 * 通过将所有返回给前端的Long类型自动转换为String类型。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // 创建一个模块，用于注册自定义的序列化器
        SimpleModule simpleModule = new SimpleModule();

        // 添加针对 Long 和 long 类型的序列化器
        // ToStringSerializer 是Jackson自带的，能将任意对象转换为其toString()形式
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance); // Long.TYPE 代表基本类型 long

        objectMapper.registerModule(simpleModule);
        return objectMapper;
    }
}
