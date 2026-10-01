package com.metas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MetasApplication {
    public static void main(String[] args) {
        SpringApplication.run(MetasApplication.class, args);
    }
}
