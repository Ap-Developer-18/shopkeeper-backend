package com.shopkeeper.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // needed for udhaar khata due-date reminder job
public class ShopkeeperApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopkeeperApplication.class, args);
    }
}
