package com.teamtacles.task.teamtacles_api_task.infrastructure.controller;

import com.teamtacles.task.teamtacles_api_task.application.dto.request.TaskRequestDTO;
import com.teamtacles.task.teamtacles_api_task.application.dto.request.TaskRequestPatchDTO;
import com.teamtacles.task.teamtacles_api_task.application.dto.response.PagedResponse;
import com.teamtacles.task.teamtacles_api_task.application.dto.response.TaskResponseDTO;
import com.teamtacles.task.teamtacles_api_task.application.dto.response.TaskResponseFilteredDTO;
import com.teamtacles.task.teamtacles_api_task.application.service.TaskService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/project")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/{projectId}/task")
    public ResponseEntity<TaskResponseDTO> createTask(@PathVariable Long projectId,
                                                      @Valid @RequestBody TaskRequestDTO taskRequestDTO,
                                                      @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        Long userId = getUserIdFromJwt(jwt);
        List<String> roles = getRolesFromJwt(jwt);
        String token = jwt.getTokenValue();
        TaskResponseDTO response = taskService.createTask(projectId, taskRequestDTO, userId, roles, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{projectId}/task/{taskId}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long projectId,
                                                       @PathVariable Long taskId,
                                                       @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        Long userId = getUserIdFromJwt(jwt);
        List<String> roles = getRolesFromJwt(jwt);
        String token = jwt.getTokenValue();
        TaskResponseDTO response = taskService.getTasksById(projectId, taskId, userId, roles, token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{projectId}/tasks/user/{userId}")
    public ResponseEntity<PagedResponse<TaskResponseDTO>> getTasksByUserInProject(@PathVariable Long projectId,
                                                                                  @PathVariable Long userId,
                                                                                  Pageable pageable,
                                                                                  @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        Long requestingUserId = getUserIdFromJwt(jwt);
        List<String> roles = getRolesFromJwt(jwt);
        String token = jwt.getTokenValue();
        PagedResponse<TaskResponseDTO> response = taskService.getAllTasksFromUserInProject(pageable, projectId, userId, requestingUserId, roles, token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/task/search")
    public ResponseEntity<PagedResponse<TaskResponseFilteredDTO>> getAllTasksFiltered(@RequestParam(required = false) String status,
                                                                                      @RequestParam(required = false) LocalDateTime dueDate,
                                                                                      @RequestParam(required = false) Long projectId,
                                                                                      Pageable pageable,
                                                                                      @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        Long userId = getUserIdFromJwt(jwt);
        List<String> roles = getRolesFromJwt(jwt);
        String token = jwt.getTokenValue();
        PagedResponse<TaskResponseFilteredDTO> response = taskService.getAllTasksFiltered(status, dueDate, projectId, pageable, userId, roles, token);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{projectId}/task/{taskId}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long projectId,
                                                      @PathVariable Long taskId,
                                                      @Valid @RequestBody TaskRequestDTO taskRequestDTO,
                                                      @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        Long userId = getUserIdFromJwt(jwt);
        List<String> roles = getRolesFromJwt(jwt);
        String token = jwt.getTokenValue();
        TaskResponseDTO response = taskService.updateTask(projectId, taskId, taskRequestDTO, userId, roles, token);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{projectId}/task/{taskId}/updateStatus")
    public ResponseEntity<TaskResponseDTO> updateTaskStatus(@PathVariable Long projectId,
                                                            @PathVariable Long taskId,
                                                            @Valid @RequestBody TaskRequestPatchDTO patchDTO,
                                                            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        Long userId = getUserIdFromJwt(jwt);
        List<String> roles = getRolesFromJwt(jwt);
        String token = jwt.getTokenValue();
        TaskResponseDTO response = taskService.updateStatus(projectId, taskId, patchDTO, userId, roles, token);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{projectId}/task/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long projectId,
                                           @PathVariable Long taskId,
                                           @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        Long userId = getUserIdFromJwt(jwt);
        List<String> roles = getRolesFromJwt(jwt);
        String token = jwt.getTokenValue();
        taskService.deleteTask(projectId, taskId, userId, roles);
        return ResponseEntity.noContent().build();
    }

    // --- Métodos Auxiliares ---

    private Long getUserIdFromJwt(Jwt jwt) {
        Object userIdClaim = jwt.getClaim("userId");
        if (userIdClaim instanceof Long) {
            return (Long) userIdClaim;
        } else if (userIdClaim instanceof Integer) {
            return ((Integer) userIdClaim).longValue();
        }
        throw new IllegalArgumentException("User ID claim ('userId') is missing or not a number in JWT.");
    }

    private List<String> getRolesFromJwt(Jwt jwt) {
        // No padrão OAuth2, os papéis/permissões são frequentemente passados na claim 'scope'
        return jwt.getClaimAsStringList("scope");
    }
}