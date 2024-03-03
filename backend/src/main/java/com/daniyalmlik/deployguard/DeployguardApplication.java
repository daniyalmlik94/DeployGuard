package com.daniyalmlik.deployguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeployguardApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeployguardApplication.class, args);
    }
}
