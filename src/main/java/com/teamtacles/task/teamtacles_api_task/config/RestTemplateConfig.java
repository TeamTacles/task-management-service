package com.teamtacles.task.teamtacles_api_task.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    
    @Value("${user-service.url}")
    private String userServiceBaseUrl;

    @Value("${project-service.url}")
    private String projectServiceBaseUrl;

    @Bean
    public RestTemplate userServiceRestTemplate(RestTemplateBuilder builder) {
        return builder
                .rootUri(userServiceBaseUrl)
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Bean
    public RestTemplate projectServiceRestTemplate(RestTemplateBuilder builder) {
        return builder
                .rootUri(projectServiceBaseUrl)
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}
