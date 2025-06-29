package com.teamtacles.task.teamtacles_api_task.infrastructure.dto.response;

import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskUserResponseDTO{
    @Schema(description = "The unique username of the user.", example = "jane.doe")
    private String userName;

    @Schema(description = "The unique email address of the user.", example = "jane.doe@example.com")
    private String email;
}