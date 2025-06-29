package com.teamtacles.task.teamtacles_api_task.infrastructure.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponseDTO {
    @Schema(description = "The unique identifier of the project.", example = "1")
    private Long id;

    @Schema(description = "The title of the project.", example = "New Feature Development")
    private String title;

    @Schema(description = "A brief description of the project.", example = "Developing and integrating the user authentication module.")
    private String description;

    @Schema(description = "A list of tasks associated with this project.", type = "array")
    private List<TaskResponseDTO> tasks;

    @Schema(description = "The user who created this project.")
    private UserResponseDTO creator;

    @Schema(description = "A list of users who are part of this project's team.", type = "array")
    private List<UserResponseDTO> team;
}