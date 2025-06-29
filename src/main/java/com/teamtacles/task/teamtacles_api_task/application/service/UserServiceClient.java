package com.teamtacles.task.teamtacles_api_task.application.service;

import org.springframework.web.client.RestTemplate;

public class UserServiceClient {

    private final RestTemplate restTemplate;

    public UserServiceClient(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    
}