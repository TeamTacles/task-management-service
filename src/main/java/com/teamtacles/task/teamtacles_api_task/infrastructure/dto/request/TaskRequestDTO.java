package com.teamtacles.task.teamtacles_api_task.infrastructure.dto.request;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequestDTO {
    @Schema(description = "The title of the task.", example = "Complete API Documentation", maxLength = 50, required = true)
    @Size(max = 50, message = "The title must not exceed 50 characters")
    @NotBlank(message = "The title must not be blank")
    private String title; 

    @Schema(description = "A detailed description of the task.", example = "Document all endpoints, DTOs, and error responses using Swagger/OpenAPI annotations.", maxLength = 250)
    @Size(max = 250, message = "The description must not exceed 250 characters")
    private String description;

    @Schema(description = "The due date and time for the task. Must be in the future and adhere to 'dd/MM/yyyy HH:mm' format.", example = "31/12/2025 23:59", type = "string", format = "date-time", required = true)
    @Future(message = "The due date cannot be in the past")
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime dueDate;

    @Schema(description = "A list of user IDs responsible for this task.", example = "[101, 105]", type = "array")
    private List<Long> usersResponsability = new ArrayList<>();
}