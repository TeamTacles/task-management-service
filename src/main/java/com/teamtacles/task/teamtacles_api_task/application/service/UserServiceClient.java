package com.teamtacles.task.teamtacles_api_task.application.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.teamtacles.task.teamtacles_api_task.application.dto.response.UserResponseDTO;
import com.teamtacles.task.teamtacles_api_task.infrastructure.exception.ResourceNotFoundException;
import com.teamtacles.task.teamtacles_api_task.infrastructure.exception.ServiceUnavailableException;

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
     * @throws ResourceNotFoundException if the user service returns a 404 Not Found error.
     * @throws AccessDeniedException if the user service returns a 403 Forbidden error.
     * @throws IllegalArgumentException if the request sent to the user service is malformed (400 Bad Request).
     * @throws ServiceUnavailableException if the user service is temporarily unavailable (503 Service Unavailable).
     * @throws RuntimeException for internal errors in the user service (500) or other network communication issues.
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
        } catch (HttpClientErrorException.Forbidden ex) {
            throw new AccessDeniedException("You do not have permission to access the user with ID " + userId);
        } catch (HttpClientErrorException.BadRequest ex) {
            throw new IllegalArgumentException("Invalid request sent to the project service.");
        } catch (HttpServerErrorException.ServiceUnavailable ex) {
            throw new ServiceUnavailableException("The project service is temporarily unavailable. Please try again later.");
        } catch (HttpServerErrorException.InternalServerError ex) {
            throw new RuntimeException("An internal error occurred in the project service.");
        } catch (RestClientException ex) {
            throw new RuntimeException("A network communication error occurred.");
        }
    }
}



