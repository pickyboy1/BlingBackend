package com.pickyboy.yuquebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pickyboy.yuquebackend.mapper")
public class YuQueBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuQueBackendApplication.class, args);
    }

}
