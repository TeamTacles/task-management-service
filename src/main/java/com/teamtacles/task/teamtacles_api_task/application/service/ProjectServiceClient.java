package com.teamtacles.task.teamtacles_api_task.application.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.teamtacles.task.teamtacles_api_task.application.dto.response.ProjectResponseDTO;
import com.teamtacles.task.teamtacles_api_task.infrastructure.exception.ResourceNotFoundException;

/**
 * A client service for interacting with the external Project monolith.
 * This class uses a dedicated RestTemplate to make authenticated HTTP requests
 * to fetch project data and handles relevant exceptions.
 *
 * @author TeamTacles
 * @version 1.0
 * @since 2025-07-04
 */
@Service
public class ProjectServiceClient {

    private final RestTemplate restTemplate;

    public ProjectServiceClient(@Qualifier("projectServiceRestTemplate") RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    /**
     * Retrieves the details of a specific project by its ID from the project service.
     * It sends an authenticated GET request using the provided JWT token.
     *
     * @param projectId The unique ID of the project to retrieve.
     * @param token     The JWT token of the authenticated user, used for authorization.
     * @return A ProjectResponseDTO containing the details of the found project.
     * @throws ResourceNotFoundException if the project service returns a 404 Not Found error,
     * indicating that no project with the given ID exists.
     */    
    public ProjectResponseDTO getProjectById(Long projectId, String token) {
        try {
            String url = "/api/project/" + projectId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<ProjectResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                ProjectResponseDTO.class
            );
            return response.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Project with ID " + projectId + " not found in the monolith.");
        }
    }
}