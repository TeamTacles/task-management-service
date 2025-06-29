package com.teamtacles.task.teamtacles_api_task.infrastructure.controller;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

import com.teamtacles.task.teamtacles_api_task.application.dto.request.TaskRequestDTO;
import com.teamtacles.task.teamtacles_api_task.application.dto.request.TaskRequestPatchDTO;
import com.teamtacles.task.teamtacles_api_task.application.dto.response.PagedResponse;
import com.teamtacles.task.teamtacles_api_task.application.dto.response.TaskResponseDTO;
import com.teamtacles.task.teamtacles_api_task.application.dto.response.TaskResponseFilteredDTO;
import com.teamtacles.task.teamtacles_api_task.application.service.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/project")
public class TaskController {
        private final TaskService taskService;

    public TaskController(TaskService taskService){
        this.taskService = taskService;
    }

    /**
     * Creates a new task associated with a specific project.
     * Only users who are the project creator, a team member of the project, or an administrator
     * have permission to create tasks within that project.
     *
     * @param id_project The unique identifier (ID) of the project to which the task will be added.
     * @param taskRequestDTO The TaskRequestDTO containing the details for the new task (e.g., title, description, due date).
     * This object is validated.
     * @param authenticatedUser The UserAuthenticated object representing the currently authenticated user,
     * injected automatically by Spring Security. This parameter is hidden from Swagger documentation.
     * @return A ResponseEntity containing the TaskResponseDTO of the newly created task
     * and an HTTP status of 201 (Created) upon successful creation.
     */
    @Operation(summary = "Create a new task for a project", description = "Creates a new task associated with a specific project. Only users with appropriate permissions for the project (project creator, team member, or administrator) can create tasks.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Task created successfully, returns the new task details."),
        @ApiResponse(responseCode = "400", description = "Bad Request: Invalid task data provided (missing fields, invalid format)."),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required or invalid token."),
        @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission to create tasks in this project."),
        @ApiResponse(responseCode = "404", description = "Not Found: Project with the specified ID was not found."),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: An unexpected error occurred.")
    })
    @PostMapping("/{id_project}/task")
    public ResponseEntity<TaskResponseDTO> createTask(@PathVariable("id_project") @Parameter(description = "Project ID") Long id_project, 
        @Valid @RequestBody @Parameter(description = "Details of the task to be created (title, description, due date).") TaskRequestDTO taskRequestDTO, 
        @Parameter(hidden = true) @AuthenticationPrincipal Jwt authenticationToken
    ){
        Long userIdFromToken = authenticationToken.getClaim("userId");
        TaskResponseDTO taskResponseDTO = taskService.createTask(id_project, taskRequestDTO, userIdFromToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskResponseDTO);
    }

    /**
     * Retrieves a specific task within a project by its task ID.
     * Users can only view tasks that belong to projects they have access to (as a team member or administrator).
     *
     * @param id_project The unique ID of the project the task belongs to.
     * @param id_task The unique ID of the task to retrieve.
     * @param authenticatedUser The UserAuthenticated object representing the currently authenticated user.
     * @return A ResponseEntity containing the TaskResponseDTO of the found task
     * and an HTTP status of 200 (OK).
     */
    @Operation(summary = "Get a task by ID", description = "Retrieves a specific task associated with a project by its ID. Users can only view tasks within projects they have access to.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the task details."),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required or invalid token."),
        @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission to view tasks in this project."),
        @ApiResponse(responseCode = "404", description = "Not Found: Project or Task with the specified IDs was not found."),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: An unexpected error occurred.")
    })
    @GetMapping("/{id_project}/task/{id_task}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable("id_project") @Parameter(description = "Project ID") Long id_project, 
        @PathVariable("id_task") @Parameter(description = "Project Task") Long id_task,
        @Parameter(hidden = true) @AuthenticationPrincipal Jwt authenticationToken
    ){
        Long userIdFromToken = authenticationToken.getClaim("userId");
        return ResponseEntity.ok(taskService.getTasksById(id_project, id_task, userIdFromToken));
    }  

    /**
     * Retrieves a paginated list of tasks assigned to a specific user within a given project.
     * This endpoint is typically restricted to administrators or the assigned user themselves.
     *
     * @param projectId The unique ID of the project.
     * @param userId The unique ID of the user whose assigned tasks are to be retrieved.
     * @param pageable Pageable object containing pagination parameters (page number, size, sort).
     * @param userFromToken The UserAuthenticated object representing the currently authenticated user.
     * @return A ResponseEntity containing a PagedResponse of { TaskResponseDTO objects,
     * representing the paginated list of tasks, and an HTTP status of 200 (OK).
     */
    @Operation(summary = "Get tasks assigned to a specific user within a project", description = "Retrieves a paginated list of tasks assigned to a specific user within a given project. Access is typically restricted to administrators.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the paginated list of tasks assigned to the user."),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required or invalid token."),
        @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission to view tasks in this project or tasks assigned to this specific user."),
        @ApiResponse(responseCode = "404", description = "Not Found: Project or User with the specified IDs was not found."),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: An unexpected error occurred.")
    })
    @GetMapping("/{projectId}/tasks/user/{userId}")
    public ResponseEntity<PagedResponse<TaskResponseDTO>> getTasksByUserInProject(@PathVariable @Parameter(description = "Project ID") Long projectId, 
        @PathVariable @Parameter(description = "User ID") Long userId, 
        @Parameter(description = "Pagination parameters (page, size, sort).") Pageable pageable,
        @Parameter(hidden = true) @AuthenticationPrincipal Jwt authenticationToken
    ){
        Long userIdFromToken = authenticationToken.getClaim("userId");
        PagedResponse<TaskResponseDTO> response = taskService.getAllTasksFromUserInProject(pageable, projectId, userId, userIdFromToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Searches and filters tasks based on various criteria such as status, due date, and project ID.
     * Users can only search within projects they have access to, or administrators can search across all projects.
     *
     * @param status Optional. Filters tasks by their status.
     * @param dueDate Optional. Filters tasks by their due date. Uses DateTimeFormat.ISO.DATE_TIME for parsing.
     * @param projectId Optional. Filters tasks belonging to a specific project.
     * @param pageable Pageable object for pagination (page number, size, sort).
     * @param authenticatedUser The UserAuthenticated object representing the currently authenticated user.
     * @return A ResponseEntity containing a PagedResponse of TaskResponseFilteredDTO objects,
     * representing the filtered and paginated list of tasks, and an HTTP status of 200 (OK).
     */
    @Operation(summary = "Search and filter tasks", description = "Retrieves a paginated and filtered list of tasks based on provided criteria (status, due date, project ID). Users can only search within projects they have access to, or administrators")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the filtered list of tasks."),
        @ApiResponse(responseCode = "400", description = "Bad Request: Invalid query parameters provided (malformed date, invalid status)."),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required or invalid token."),
        @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission to view tasks in the specified project(s)."),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: An unexpected error occurred.")
    })
    @GetMapping("/task/search")
    public ResponseEntity<PagedResponse<TaskResponseFilteredDTO>> getAllTasksFiltered(@RequestParam(value = "status", required = false) 
        @Parameter(description = "Filter tasks by status") String status,
        @RequestParam(value = "dueDate", required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
        @Parameter(description = "Filter tasks by dueDate") LocalDateTime dueDate,
        @RequestParam(value = "projectId", required = false) @Parameter(description = "Filter tasks by Project ID") Long projectId,
        @Parameter(description = "Pagination parameters (page, size, sort).") Pageable pageable, 
        @Parameter(hidden = true) @AuthenticationPrincipal Jwt authenticationToken
    ){
        Long userIdFromToken = authenticationToken.getClaim("userId");
        PagedResponse tasksPage = taskService.getAllTasksFiltered(status, dueDate, projectId, pageable, userIdFromToken);
        return ResponseEntity.status(HttpStatus.OK).body(tasksPage);
    }  

    /**
     * Updates the status of a specific task within a project.
     * This operation allows task assignees or administrators to change a task's status.
     *
     * @param id_project The unique ID of the project the task belongs to.
     * @param id_task The unique ID of the task whose status is to be updated.
     * @param taskRequestPatchDTO The TaskRequestPatchDTO containing the new status for the task.
     * This object is validated.
     * @param authenticatedUser The UserAuthenticated object representing the currently authenticated user.
     * @return A ResponseEntity containing the TaskResponseDTO of the updated task
     * and an HTTP status of 200 (OK) upon successful update.
     */
    @Operation(summary = "Update task status", description = "Updates the status of a specific task within a project. Only users with appropriate permissions (e.g., project members, task assignees, or administrators) can change a task's status.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task status updated successfully, returns the updated task details."),
        @ApiResponse(responseCode = "400", description = "Bad Request: Invalid status provided or other invalid data for update."),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required or invalid token."),
        @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission to update the status of this task or in this project."),
        @ApiResponse(responseCode = "404", description = "Not Found: Project or Task with the specified IDs was not found."),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: An unexpected error occurred.")
    })
    @PatchMapping("/{id_project}/task/{id_task}/updateStatus")
    public ResponseEntity<TaskResponseDTO> updateStatus(@PathVariable("id_project") @Parameter(description = "Project ID") Long id_project, 
        @PathVariable("id_task") @Parameter(description = "Task ID")Long id_task, 
        @Valid @RequestBody @Parameter(description = "Request body containing the new status for the task or other informations.") TaskRequestPatchDTO taskRequestPatchDTO, 
        @Parameter(hidden = true) @AuthenticationPrincipal Jwt authenticationToken
    ){
        Long userIdFromToken = authenticationToken.getClaim("userId");
        TaskResponseDTO taskResponseDTO = taskService.updateStatus(id_project, id_task, taskRequestPatchDTO, userIdFromToken);
        return ResponseEntity.status(HttpStatus.OK).body(taskResponseDTO);
    }
    
    /**
     * Updates all modifiable details of a specific task within a project.
     * This operation allows task assignees  or administrators to update a task.
     *
     * @param id_project The unique ID of the project the task belongs to.
     * @param id_task The unique ID of the task to update.
     * @param taskRequestDTO The TaskRequestDTO containing the complete updated details for the task.
     * This object is validated.
     * @param authenticatedUser The UserAuthenticated object representing the currently authenticated user.
     * @return A ResponseEntity containing the TaskResponseDTO of the updated task
     * and an HTTP status of 200 (OK) upon successful update.
     */
    @Operation(summary = "Update an existing task", description = "Updates all modifiable details of a specific task within a project, identified by its project ID and task ID. Only users with appropriate permissions (project members, task assignees, or administrators) can update a task.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task updated successfully, returns the updated task details."),
        @ApiResponse(responseCode = "400", description = "Bad Request: Invalid task data provided (missing required fields, invalid format)."),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required or invalid token."),
        @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission to update this task or in this project."),
        @ApiResponse(responseCode = "404", description = "Not Found: Project or Task with the specified IDs was not found."),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: An unexpected error occurred.")
    })
    @PutMapping("/{id_project}/task/{id_task}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable("id_project") @Parameter(description = "Project ID") Long id_project, 
        @PathVariable("id_task") @Parameter(description = "Task ID") Long id_task, 
        @Valid @RequestBody @Parameter(description = "Complete updated details for the task.") TaskRequestDTO taskRequestDTO, 
        @Parameter(hidden = true) @AuthenticationPrincipal Jwt authenticationToken
    ){
        Long userIdFromToken = authenticationToken.getClaim("userId");
        TaskResponseDTO taskResponseDTO = taskService.updateTask(id_project, id_task, taskRequestDTO, userIdFromToken);
        return ResponseEntity.status(HttpStatus.OK).body(taskResponseDTO);
    }

    /**
     * Deletes a specific task from a project.
     * This operation allows task assignees or administrators to delete a task.
     *
     * @param id_project The unique ID of the project the task belongs to.
     * @param id_task The unique ID of the task to delete.
     * @param authenticatedUser The UserAuthenticated object representing the currently authenticated user.
     * @return A ResponseEntity with no content (HTTP status 204 No Content) upon successful deletion.
     */
    @Operation(summary = "Delete a task", description = "Deletes a specific task identified by its project ID and task ID. Only users with appropriate permissions (project members, task assignees, or administrators) can delete a task.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task deleted successfully (No Content)."),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required or invalid token."),
        @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission to delete this task or in this project."),
        @ApiResponse(responseCode = "404", description = "Not Found: Project or Task with the specified IDs was not found."),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: An unexpected error occurred.")
    })
    @DeleteMapping("/{id_project}/task/{id_task}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id_project") @Parameter(description = "Project ID") Long id_project, 
        @PathVariable("id_task") @Parameter(description = "Task ID") Long id_task, 
        @Parameter(hidden = true) @AuthenticationPrincipal Jwt authenticationToken
    ){
        Long userIdFromToken = authenticationToken.getClaim("userId");
        taskService.deleteTask(id_project, id_task, userIdFromToken);
        return ResponseEntity.noContent().build();
    }
}
