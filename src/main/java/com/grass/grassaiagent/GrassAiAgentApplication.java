package com.grass.grassaiagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.grass.grassaiagent.mapper")
public class GrassAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrassAiAgentApplication.class, args);
    }

}
