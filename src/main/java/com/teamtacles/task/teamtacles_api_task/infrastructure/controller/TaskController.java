package com.teamtacles.task.teamtacles_api_task.infrastructure.controller;

import com.teamtacles.task.teamtacles_api_task.application.dto.request.TaskRequestDTO;
import com.teamtacles.task.teamtacles_api_task.application.dto.request.TaskRequestPatchDTO;
import com.teamtacles.task.teamtacles_api_task.application.dto.response.PagedResponse;
import com.teamtacles.task.teamtacles_api_task.application.dto.response.TaskResponseDTO;
import com.teamtacles.task.teamtacles_api_task.application.dto.response.TaskResponseFilteredDTO;
import com.teamtacles.task.teamtacles_api_task.application.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for managing task-related operations within projects in the TeamTacles application.
 * This controller provides endpoints for creating, retrieving, updating (full and partial),
 * and deleting tasks. It ensures that only authorized users can perform these actions
 * by validating permissions based on the user's JWT and the business logic in the TaskService.
 *
 * @author TeamTacles 
 * @version 1.0
 * @since 2025-07-04
 */
@RestController
@RequestMapping("/api/project")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    /**
     * Creates a new task associated with a specific project.
     * Permission to create a task is typically granted to project members or administrators.
     * The authenticated user's identity is extracted from the JWT.
     *
     * @param projectId The ID of the project where the task will be created.
     * @param taskRequestDTO The DTO containing the details for the new task. This is validated.
     * @param jwt The JWT object representing the authenticated user, injected by Spring Security.
     * @return A ResponseEntity containing the DTO of the newly created task and an HTTP status of 201 (Created).
     */
    @Operation(summary = "Create a new task in a project", description = "Creates a new task associated with a specific project. Permissions are checked based on the user's role and project membership.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully."),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid data provided for the task."),
            @ApiResponse(responseCode = "401", description = "Unauthorized: JWT token is missing or invalid."),
            @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission to create tasks in this project."),
            @ApiResponse(responseCode = "404", description = "Not Found: The specified project does not exist."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
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

    /**
     * Retrieves a specific task by its ID within a given project.
     * Access is granted if the user is a member of the project or an administrator.
     *
     * @param projectId The ID of the project containing the task.
     * @param taskId The ID of the task to retrieve.
     * @param jwt The JWT object for the authenticated user.
     * @return A ResponseEntity containing the task's details and an HTTP status of 200 (OK).
     */
    @Operation(summary = "Get a task by its ID", description = "Retrieves a specific task by its ID, scoped to a project. Requires project membership or admin rights.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task retrieved successfully."),
            @ApiResponse(responseCode = "401", description = "Unauthorized: JWT token is missing or invalid."),
            @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission to view this task."),
            @ApiResponse(responseCode = "404", description = "Not Found: The specified project or task does not exist."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
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

    /**
     * Retrieves a paginated list of all tasks for a specific user within a project.
     * This action is typically restricted to administrators or the user themselves.
     *
     * @param projectId The ID of the project to search within.
     * @param userId The ID of the user whose tasks are being requested.
     * @param pageable Pagination information (page, size, sort).
     * @param jwt The JWT object for the authenticated user making the request.
     * @return A ResponseEntity with a paginated response of tasks and an HTTP status of 200 (OK).
     */
    @Operation(summary = "Get all tasks for a user in a project", description = "Retrieves a paginated list of tasks assigned to a specific user within a project. Access is generally restricted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of tasks."),
            @ApiResponse(responseCode = "401", description = "Unauthorized: JWT token is missing or invalid."),
            @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission to view tasks for the specified user."),
            @ApiResponse(responseCode = "404", description = "Not Found: The specified project or user does not exist."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
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

    /**
     * Searches for tasks across projects based on optional filter criteria.
     * The results are filtered based on the authenticated user's permissions. Regular users
     * will only see tasks they are associated with.
     *
     * @param status Optional filter for task status (e.g., 'PENDING', 'COMPLETED').
     * @param dueDate Optional filter for tasks due on or before this date.
     * @param projectId Optional filter to scope search to a single project.
     * @param pageable Pagination information.
     * @param jwt The JWT object for the authenticated user.
     * @return A ResponseEntity with a paginated response of filtered tasks and an HTTP status of 200 (OK).
     */
    @Operation(summary = "Search and filter tasks", description = "Searches for tasks with optional filters for status, due date, and project. Results are based on user permissions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the filtered list of tasks."),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid filter parameter format."),
            @ApiResponse(responseCode = "401", description = "Unauthorized: JWT token is missing or invalid."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
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

    /**
     * Fully updates an existing task's details.
     * Requires the user to have appropriate permissions, such as being an admin or a project member.
     *
     * @param projectId The ID of the project containing the task.
     * @param taskId The ID of the task to update.
     * @param taskRequestDTO The DTO with the full set of updated task information. This is validated.
     * @param jwt The JWT object for the authenticated user.
     * @return A ResponseEntity with the updated task DTO and an HTTP status of 200 (OK).
     */
    @Operation(summary = "Update a task (full update)", description = "Performs a full update on an existing task's details. All fields in the request body are applied.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully."),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid data for the task update."),
            @ApiResponse(responseCode = "401", description = "Unauthorized: JWT token is missing or invalid."),
            @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission to update this task."),
            @ApiResponse(responseCode = "404", description = "Not Found: The specified project or task does not exist."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
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

    /**
     * Partially updates a task, specifically to change its status.
     * This is a specific operation for quick status changes.
     *
     * @param projectId The ID of the project containing the task.
     * @param taskId The ID of the task whose status will be updated.
     * @param patchDTO A DTO containing the new status value. This is validated.
     * @param jwt The JWT object for the authenticated user.
     * @return A ResponseEntity with the updated task DTO and an HTTP status of 200 (OK).
     */
    @Operation(summary = "Update a task's status (partial update)", description = "Performs a partial update on a task, specifically to change its status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task status updated successfully."),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid status value provided."),
            @ApiResponse(responseCode = "401", description = "Unauthorized: JWT token is missing or invalid."),
            @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission to update this task's status."),
            @ApiResponse(responseCode = "404", description = "Not Found: The specified project or task does not exist."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
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

    /**
     * Deletes a task from a project.
     * Requires administrative privileges or specific ownership/project permissions.
     *
     * @param projectId The ID of the project containing the task.
     * @param taskId The ID of the task to be deleted.
     * @param jwt The JWT object for the authenticated user.
     * @return A ResponseEntity with no content and an HTTP status of 204 (No Content).
     */
    @Operation(summary = "Delete a task", description = "Deletes a task from a project. This action is permanent and requires appropriate permissions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully."),
            @ApiResponse(responseCode = "401", description = "Unauthorized: JWT token is missing or invalid."),
            @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission to delete this task."),
            @ApiResponse(responseCode = "404", description = "Not Found: The specified project or task does not exist."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
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

    /**
     * Extracts the user ID from the 'userId' claim of a JWT.
     * Handles cases where the ID might be an Integer or Long.
     *
     * @param jwt The JWT to extract the claim from.
     * @return The user ID as a Long.
     * @throws IllegalArgumentException if the 'userId' claim is missing or not a number.
     */
    private Long getUserIdFromJwt(Jwt jwt) {
        Object userIdClaim = jwt.getClaim("userId");
        if (userIdClaim instanceof Long) {
            return (Long) userIdClaim;
        } else if (userIdClaim instanceof Integer) {
            return ((Integer) userIdClaim).longValue();
        }
        throw new IllegalArgumentException("User ID claim ('userId') is missing or not a number in JWT.");
    }

    /**
     * Extracts the user's roles from the 'scope' or 'roles' claim of a JWT.
     * In standard OAuth2/OIDC, roles/permissions are often in the 'scope' claim.
     *
     * @param jwt The JWT to extract the claims from.
     * @return A list of strings representing the user's roles.
     */
    private List<String> getRolesFromJwt(Jwt jwt) {
        return jwt.getClaimAsStringList("scope");
    }
}