package com.clawdash;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.clawdash.mapper")
@EnableScheduling
public class ClawDashApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClawDashApplication.class, args);
    }
}
