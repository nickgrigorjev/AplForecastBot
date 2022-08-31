package com.example.aplforecastbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AplForecastBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(AplForecastBotApplication.class, args);
    }

}
