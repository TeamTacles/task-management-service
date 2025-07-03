package com.teamtacles.task.teamtacles_api_task.application.service;

import org.springframework.beans.factory.annotation.Qualifier;
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

    public ProjectResponseDTO getProjectById(Long projectId){
        try{
            return restTemplate.getForObject("/api/project/{projectId}", ProjectResponseDTO.class, projectId);
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException("Project not found.");
        }
    }
}