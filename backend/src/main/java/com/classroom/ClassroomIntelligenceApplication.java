package com.classroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClassroomIntelligenceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClassroomIntelligenceApplication.class, args);
    }
}
