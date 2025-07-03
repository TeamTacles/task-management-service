package com.teamtacles.task.teamtacles_api_task.application.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.teamtacles.task.teamtacles_api_task.application.dto.response.UserResponseDTO;
import com.teamtacles.task.teamtacles_api_task.infrastructure.exception.ResourceNotFoundException;

@Service
public class UserServiceClient {

    private final RestTemplate restTemplate;

    public UserServiceClient(@Qualifier("userServiceRestTemplate") RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    // Método corrigido para aceitar e usar o token
    public UserResponseDTO getUserById(Long userId, String token){
        try{
            String url = "/api/user/" + userId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<UserResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                UserResponseDTO.class
            );
            return response.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("User with ID " + userId + " not found in the monolith.");
        }
    }
}



