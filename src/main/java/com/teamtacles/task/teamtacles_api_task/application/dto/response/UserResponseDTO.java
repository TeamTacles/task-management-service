package com.teamtacles.task.teamtacles_api_task.application.dto.response;

import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserResponseDTO{
    @Schema(description = "The unique identifier of the User.", example = "1")
    private Long userId;

    @Schema(description = "The unique username of the user.", example = "jane.doe")
    private String userName;

    @Schema(description = "The unique email address of the user.", example = "jane.doe@example.com")
    private String email;
}