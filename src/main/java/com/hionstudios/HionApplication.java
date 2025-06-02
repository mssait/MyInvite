package com.hionstudios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HionApplication {

    public static void main(String[] args) {
        SpringApplication.run(HionApplication.class, args);
    }

}
