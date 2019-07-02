package com.cvurl.usage.example.cvurlusage;

import coresearch.cvurl.io.request.CVurl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CvurlUsageApplication {

    public static void main(String[] args) {
        SpringApplication.run(CvurlUsageApplication.class, args);
    }

    @Bean
    public CVurl cVurl() {
        return new CVurl();
    }
}
