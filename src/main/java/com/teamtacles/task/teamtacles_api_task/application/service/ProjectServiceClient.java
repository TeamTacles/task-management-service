package com.teamtacles.task.teamtacles_api_task.application.service;

import org.springframework.web.client.RestTemplate;

public class ProjectServiceClient {

    private final RestTemplate restTemplate;

    public ProjectServiceClient(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

}