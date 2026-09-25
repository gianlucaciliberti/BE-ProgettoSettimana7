package com.epicode.salone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SaloneAutoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaloneAutoApplication.class, args);
    }

}
