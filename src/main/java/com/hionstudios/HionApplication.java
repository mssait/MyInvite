package com.hionstudios;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.hionstudios.time.TimeUtil;

@SpringBootApplication
@ComponentScan
@EnableScheduling
public class HionApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeUtil.TIMEZONE);
        SpringApplication.run(HionApplication.class, args);
    }

}
