package com.teamtacles.task.teamtacles_api_task.infrastructure.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.teamtacles.task.teamtacles_api_task.domain.model.enums.Status;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponseFilteredDTO{
    @Schema(description = "The unique identifier of the task.", example = "101")
    private Long id;

    @Schema(description = "The title of the task.", example = "Implement User Authentication")
    private String title;

    @Schema(description = "A detailed description of the task.", example = "Develop and integrate the JWT-based authentication system.")
    private String description;

    @Schema(description = "The due date and time for the task. Format: 'yyyy-MM-ddTHH:mm:ss' (ISO 8601).", example = "2024-12-31T23:59:59", type = "string", format = "date-time")
    private LocalDateTime dueDate;

    @Schema(description = "The current status of the task.", example = "IN_PROGRESS")
    private Status status;

    @Schema(description = "The user assigned as the primary owner/creator of the task.")
    private UserResponseDTO owner;

    @Schema(description = "A list of users who are responsible for completing this task.", type = "array")
    private List<UserResponseDTO> usersResponsability;
    
    @Schema(description = "The project details associated with this task, potentially filtered.")
    private TaskProjectResponseFilteredDTO project;
}  