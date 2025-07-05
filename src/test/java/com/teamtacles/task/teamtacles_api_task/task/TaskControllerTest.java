package com.teamtacles.task.teamtacles_api_task.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamtacles.task.teamtacles_api_task.application.dto.request.TaskRequestDTO;
import com.teamtacles.task.teamtacles_api_task.infrastructure.repository.TaskRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 8081)
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUpEnvironment() {
        taskRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Should create a task and return 201 CREATED")
    void testCreateTask_ShouldReturn201() throws Exception {
        long validUserId = 123L;
        long validProjectId = 456L;

        stubFor(get(String.format("/api/users/" + validUserId))
                .willReturn(aResponse()
                        .withStatus(200) // conseguiu buscar o usuário
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": " + validUserId + ", \"name\": \"Test User\"}")));
        
        stubFor(get(String.format("/api/projects/" + validProjectId))
                .willReturn(aResponse()
                        .withStatus(200) // conseguiu buscar o projeto
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": " + validProjectId + ", \"name\": \"Test Project\"}")));

        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle("Review project documentation");
        dto.setUsersResponsability(List.of(validUserId));

        mockMvc.perform(post("/api/project") 
            .header("Authorization", "Bearer some-valid-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Review project documentation"))
            .andExpect(jsonPath("$.userId").value(validUserId)) 
            .andExpect(jsonPath("$.projectId").value(validProjectId));
            
        // Verificamos se o serviço de Task realmente CHAMOU as dependências
        verify(getRequestedFor(urlEqualTo("/api/users/" + validUserId)));
        verify(getRequestedFor(urlEqualTo("/api/projects/" + validProjectId)));

        var tasks = taskRepository.findAll();
        assertFalse(tasks.isEmpty());
        assertEquals(validUserId, tasks.get(0).getOwnerUserId());
    }

}
