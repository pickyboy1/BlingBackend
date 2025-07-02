package com.pickyboy.yuquebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.pickyboy.yuquebackend.mapper")
@EnableAsync
public class YuQueBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuQueBackendApplication.class, args);
    }

}
