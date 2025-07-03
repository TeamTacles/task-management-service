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

@Service
public class ProjectServiceClient {

    private final RestTemplate restTemplate;

    public ProjectServiceClient(@Qualifier("projectServiceRestTemplate") RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    // Método corrigido para aceitar e usar o token
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