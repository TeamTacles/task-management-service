package com.teamtacles.task.teamtacles_api_task.application.service;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.teamtacles.task.teamtacles_api_task.infrastructure.dto.response.UserResponseDTO;
import com.teamtacles.task.teamtacles_api_task.infrastructure.exception.ResourceNotFoundException;

public class UserServiceClient {

    private final RestTemplate restTemplate;

    public UserServiceClient(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    public UserResponseDTO getUserById(Long userId){
        try{
            return restTemplate.getForObject("api/user/{userId}", UserResponseDTO.class, userId);
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException("user not found.");
        }
    }
}



