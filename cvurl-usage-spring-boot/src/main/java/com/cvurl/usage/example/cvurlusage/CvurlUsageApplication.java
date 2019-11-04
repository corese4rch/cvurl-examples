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

        /*
        other ways you can create CVurl:
        new CVurl(Configuration.builder(httpClient)
                .genericMapper(genericMapper)
                .build());
        new CVurl(Configuration.builder()
                .executor(Executors.newFixedThreadPool(3))
                .requestTimeout(Duration.ofSeconds(5))
                .build());
        */

    }
}
