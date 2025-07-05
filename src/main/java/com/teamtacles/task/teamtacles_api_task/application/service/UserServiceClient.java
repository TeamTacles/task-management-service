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

/**
 * A client service for interacting with the external User monolith.
 * This class uses a configured RestTemplate to make HTTP requests to fetch user data,
 * handling necessary authentication and error translation.
 *
 * @author TeamTacles 
 * @version 1.0
 * @since 2025-07-04
 */
@Service
public class UserServiceClient {

    private final RestTemplate restTemplate;

    public UserServiceClient(@Qualifier("userServiceRestTemplate") RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    /**
     * Retrieves the details of a specific user by their ID from the user service.
     * It sends a GET request with the provided JWT token in the Authorization header.
     *
     * @param userId The unique ID of the user to retrieve.
     * @param token  The JWT token of the authenticated user making the request, used for authorization.
     * @return A UserResponseDTO containing the details of the found user.
     * @throws ResourceNotFoundException if the user service returns a 404 Not Found error,
     * indicating that no user with the given ID exists.
     */    
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



