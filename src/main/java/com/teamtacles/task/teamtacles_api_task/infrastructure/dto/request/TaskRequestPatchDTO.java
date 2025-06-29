package com.teamtacles.task.teamtacles_api_task.infrastructure.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//import com.teamtacles.teamtacles_api.infrastructure.dto.response.ProjectResponseDTO;
//import com.teamtacles.teamtacles_api.dto.response.UserResponseDTO;
import com.teamtacles.task.teamtacles_api_task.domain.model.enums.Status;
import com.teamtacles.task.teamtacles_api_task.infrastructure.dto.response.UserResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequestPatchDTO {
    @Schema(description = "The new title for the task. If provided, the existing title will be updated.", example = "Refine Project Scope")
    private Optional<String> title = Optional.empty();

    @Schema(description = "The new description for the task. If provided, the existing description will be updated.", example = "Review and finalize the project's requirements and deliverables.")
    private Optional<String> description = Optional.empty();

    @Schema(description = "The new due date and time for the task. Must be in the future. Format: 'yyyy-MM-ddTHH:mm:ss'.", example = "2025-12-31T17:00:00", type = "string", format = "date-time")
    private Optional<LocalDateTime> dueDate = Optional.empty();

    @Schema(description = "The new status for the task. If provided, the existing status will be updated. Allowed values are based on the Task Status enum.", example = "IN_PROGRESS") 
    private Optional<Status> status = Optional.empty();

    private Optional<UserResponseDTO> owner = Optional.empty();

    // private Optional<ProjectResponseDTO> project = Optional.empty();

    @Schema(description = "A new list of user IDs (Long) to replace the current task responsibilities. If provided, the existing list will be fully replaced.", example = "[789, 987]", type = "array")
    private Optional<List<Long>> userId = Optional.empty();
}
