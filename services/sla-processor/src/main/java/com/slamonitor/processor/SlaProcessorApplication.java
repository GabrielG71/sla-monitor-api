package com.slamonitor.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SlaProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlaProcessorApplication.class, args);
    }
}
