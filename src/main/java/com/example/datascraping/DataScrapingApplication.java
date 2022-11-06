package com.example.datascraping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DataScrapingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataScrapingApplication.class, args);
    }

}
