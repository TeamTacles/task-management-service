package com.teamtacles.task.teamtacles_api_task.application.service;

import com.teamtacles.task.teamtacles_api_task.application.dto.request.TaskRequestDTO;
import com.teamtacles.task.teamtacles_api_task.application.dto.request.TaskRequestPatchDTO;
import com.teamtacles.task.teamtacles_api_task.application.dto.response.*;
import com.teamtacles.task.teamtacles_api_task.domain.model.enums.Status;
import com.teamtacles.task.teamtacles_api_task.infrastructure.exception.ResourceNotFoundException;
import com.teamtacles.task.teamtacles_api_task.infrastructure.mapper.PagedResponseMapper;
import com.teamtacles.task.teamtacles_api_task.infrastructure.persistence.entity.TaskEntity;
import com.teamtacles.task.teamtacles_api_task.infrastructure.repository.TaskRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Function;

/**
 * Service class for handling business logic related to tasks.
 * This class orchestrates operations between the TaskRepository and external services
 * like UserServiceClient and ProjectServiceClient to manage tasks, enforce security,
 * and structure responses.
 *
 * @author TeamTacles
 * @version 1.0
 * @since 2025-07-04
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserServiceClient userServiceClient;
    private final ProjectServiceClient projectServiceClient;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public TaskService(TaskRepository taskRepository, UserServiceClient userServiceClient, ProjectServiceClient projectServiceClient, ModelMapper modelMapper, PagedResponseMapper pagedResponseMapper) {
        this.taskRepository = taskRepository;
        this.userServiceClient = userServiceClient;
        this.projectServiceClient = projectServiceClient;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    /**
     * Creates a new task for a given project.
     * It verifies that the owner and all responsible users exist and that the owner
     * has permission to view the project. The task owner is automatically added
     * to the list of responsible users if not already present.
     *
     * @param projectId The ID of the project to associate the task with.
     * @param taskRequestDTO DTO containing the details for the new task.
     * @param ownerId The ID of the user creating the task.
     * @param roles The roles of the user creating the task.
     * @param token The JWT token for authenticating with other services.
     * @return A DTO representing the newly created task.
     * @throws ResourceNotFoundException if the project or any specified user does not exist.
     * @throws AccessDeniedException if the user does not have permission to view the project.
     */
    public TaskResponseDTO createTask(Long projectId, TaskRequestDTO taskRequestDTO, Long ownerId, List<String> roles, String token) {
        ensureUserCanViewProject(projectId, ownerId, roles, token);
        userServiceClient.getUserById(ownerId, token);

        List<Long> responsibleIds = new ArrayList<>(taskRequestDTO.getUsersResponsability());
        for (Long responsibleId : responsibleIds) {
            userServiceClient.getUserById(responsibleId, token);
        }

        if (!responsibleIds.contains(ownerId)) {
            responsibleIds.add(ownerId);
        }

        TaskEntity taskEntity = modelMapper.map(taskRequestDTO, TaskEntity.class);
        taskEntity.setProjectId(projectId);
        taskEntity.setOwnerUserId(ownerId);
        taskEntity.setStatus(Status.TODO);
        taskEntity.setResponsibleUserIds(responsibleIds);

        TaskEntity savedEntity = taskRepository.save(taskEntity);
        return convertToDto(savedEntity, token);
    }

    /**
     * Retrieves a single task by its ID and project ID.
     * It ensures the task exists and belongs to the specified project, and that the
     * requesting user has permission to access it.
     *
     * @param projectId The ID of the project the task belongs to.
     * @param taskId The ID of the task to retrieve.
     * @param userId The ID of the user making the request.
     * @param roles The roles of the user making the request.
     * @param token The JWT token for service-to-service communication.
     * @return A DTO representing the found task.
     * @throws ResourceNotFoundException if the task is not found or does not belong to the project.
     * @throws AccessDeniedException if the user is not an admin, owner, or responsible user.
     */
    public TaskResponseDTO getTasksById(Long projectId, Long taskId, Long userId, List<String> roles, String token) {
        TaskEntity taskEntity = findTaskByIdAndProject(taskId, projectId);
        ensureUserCanAccessTask(taskEntity, userId, roles);
        return convertToDto(taskEntity, token);
    }

    /**
     * Retrieves a paginated list of tasks for a specific user within a specific project.
     * The requesting user must be an admin or the same user whose tasks are being requested.
     *
     * @param pageable Pagination information.
     * @param projectId The ID of the project to search within.
     * @param targetUserId The ID of the user whose tasks are to be retrieved.
     * @param requestingUserId The ID of the user making the request.
     * @param roles The roles of the user making the request.
     * @param token The JWT token for service-to-service communication.
     * @return A paginated response of task DTOs.
     * @throws AccessDeniedException if the requesting user is not authorized to view the target user's tasks.
     * @throws ResourceNotFoundException if the project or target user is not found.
     */
    public PagedResponse<TaskResponseDTO> getAllTasksFromUserInProject(Pageable pageable, Long projectId, Long targetUserId, Long requestingUserId, List<String> roles, String token) {
        ensureUserCanViewProject(projectId, requestingUserId, roles, token);

        if (!isAdmin(roles) && !requestingUserId.equals(targetUserId)) {
            throw new AccessDeniedException("FORBIDDEN - You do not have permission to access this user's tasks.");
        }

        userServiceClient.getUserById(targetUserId, token);

        Page<TaskEntity> tasksPage = taskRepository.findByProjectIdAndResponsibleUser(projectId, targetUserId, pageable);
        Function<TaskEntity, TaskResponseDTO> converter = entity -> convertToDto(entity, token);

        return pagedResponseMapper.toPagedResponse(tasksPage, converter);
    }

    /**
     * Retrieves a paginated list of tasks based on optional filters.
     * If the user is an admin, the search is performed without user restrictions.
     * Otherwise, the search is scoped to tasks where the user is the owner or a responsible user.
     *
     * @param status Optional status to filter by.
     * @param dueDate Optional due date to filter by (tasks due on or before this date).
     * @param projectId Optional project ID to filter by.
     * @param pageable Pagination information.
     * @param userId The ID of the user making the request.
     * @param roles The roles of the user making the request.
     * @param token The JWT token for service-to-service communication.
     * @return A paginated response of filtered task DTOs.
     * @throws IllegalArgumentException if the status string is invalid.
     */
    public PagedResponse<TaskResponseFilteredDTO> getAllTasksFiltered(String status, LocalDateTime dueDate, Long projectId, Pageable pageable, Long userId, List<String> roles, String token) {
        Status statusEnum = transformStatusToEnum(status);

        if (projectId != null) {
            ensureUserCanViewProject(projectId, userId, roles, token);
        }

        Page<TaskEntity> tasksPage;
        if (isAdmin(roles)) {
            tasksPage = taskRepository.findTasksFiltered(statusEnum, dueDate, projectId, pageable);
        } else {
            tasksPage = taskRepository.findTasksFilteredByUser(statusEnum, dueDate, projectId, userId, pageable);
        }

        // Para o filtro, a conversão para DTO também precisa do token
        Page<TaskResponseFilteredDTO> dtoPage = tasksPage.map(entity -> {
            TaskResponseFilteredDTO dto = modelMapper.map(entity, TaskResponseFilteredDTO.class);
            ProjectResponseDTO projectDto = projectServiceClient.getProjectById(entity.getProjectId(), token);
            dto.setProject(modelMapper.map(projectDto, ProjectResponseFilteredDTO.class));
            UserResponseDTO ownerDto = userServiceClient.getUserById(entity.getOwnerUserId(), token);
            dto.setOwner(ownerDto);
            List<UserResponseDTO> responsibleDtos = entity.getResponsibleUserIds().stream()
                    .map(id -> userServiceClient.getUserById(id, token))
                    .collect(Collectors.toList());
            dto.setUsersResponsability(responsibleDtos);
            return dto;
        });

        return new PagedResponse<>(dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(), dtoPage.getTotalElements(), dtoPage.getTotalPages(), dtoPage.isLast());
    }

    /**
     * Updates an existing task with new details.
     * It ensures the user has permission to access the task before applying changes.
     *
     * @param projectId The ID of the project the task belongs to.
     * @param taskId The ID of the task to update.
     * @param taskRequestDTO DTO containing the new details for the task.
     * @param userId The ID of the user making the request.
     * @param roles The roles of the user making the request.
     * @param token The JWT token for service-to-service communication.
     * @return A DTO representing the updated task.
     * @throws ResourceNotFoundException if the task or any responsible user is not found.
     * @throws AccessDeniedException if the user does not have permission to access the task.
     */
    public TaskResponseDTO updateTask(Long projectId, Long taskId, TaskRequestDTO taskRequestDTO, Long userId, List<String> roles, String token) {
        TaskEntity taskEntity = findTaskByIdAndProject(taskId, projectId);
        ensureUserCanAccessTask(taskEntity, userId, roles);

        List<Long> responsibleIds = new ArrayList<>(taskRequestDTO.getUsersResponsability());
        for (Long responsibleId : responsibleIds) {
            userServiceClient.getUserById(responsibleId, token);
        }

        modelMapper.map(taskRequestDTO, taskEntity);
        taskEntity.setResponsibleUserIds(responsibleIds);

        TaskEntity updatedEntity = taskRepository.save(taskEntity);
        return convertToDto(updatedEntity, token);
    }

     /**
     * Partially updates a task, specifically its status.
     * It ensures the user has permission to access the task before applying the change.
     *
     * @param projectId The ID of the project the task belongs to.
     * @param taskId The ID of the task to update.
     * @param patchDTO DTO containing the new status.
     * @param userId The ID of the user making the request.
     * @param roles The roles of the user making the request.
     * @param token The JWT token for service-to-service communication.
     * @return A DTO representing the updated task.
     * @throws ResourceNotFoundException if the task is not found.
     * @throws AccessDeniedException if the user does not have permission to access the task.
     */
    public TaskResponseDTO updateStatus(Long projectId, Long taskId, TaskRequestPatchDTO patchDTO, Long userId, List<String> roles, String token) {
        TaskEntity taskEntity = findTaskByIdAndProject(taskId, projectId);
        ensureUserCanAccessTask(taskEntity, userId, roles);

        patchDTO.getStatus().ifPresent(taskEntity::setStatus);

        TaskEntity updatedEntity = taskRepository.save(taskEntity);
        return convertToDto(updatedEntity, token);
    }

    /**
     * Deletes a task.
     * It ensures the user has permission to access the task before deletion.
     *
     * @param projectId The ID of the project the task belongs to.
     * @param taskId The ID of the task to delete.
     * @param userId The ID of the user making the request.
     * @param roles The roles of the user making the request.
     * @throws ResourceNotFoundException if the task is not found.
     * @throws AccessDeniedException if the user does not have permission to access the task.
     */
    public void deleteTask(Long projectId, Long taskId, Long userId, List<String> roles) {
        TaskEntity taskEntity = findTaskByIdAndProject(taskId, projectId);
        ensureUserCanAccessTask(taskEntity, userId, roles);
        taskRepository.delete(taskEntity);
    }

    /**
     * Checks if a user has permission to view a project's contents.
     * Access is granted if the user is an admin or a member of the project's team.
     *
     * @param projectId The ID of the project to check.
     * @param userId The ID of the user to check.
     * @param roles The roles of the user.
     * @param token The JWT token for service communication.
     * @throws AccessDeniedException if access is denied.
     * @throws ResourceNotFoundException if the project or user is not found.
     */
    private void ensureUserCanViewProject(Long projectId, Long userId, List<String> roles, String token) {
        if (isAdmin(roles)) {
            projectServiceClient.getProjectById(projectId, token);
            return;
        }
        
        ProjectResponseDTO project = projectServiceClient.getProjectById(projectId, token);
        UserResponseDTO requestingUser = userServiceClient.getUserById(userId, token);

        boolean isUserInTeam = project.getTeam().stream()
                .anyMatch(userDto -> userDto.getUserName().equals(requestingUser.getUserName()));

        if (!isUserInTeam) {
            throw new AccessDeniedException("You do not have permission to access this project.");
        }
    }

    /**
     * Checks if a user has permission to access/modify a specific task.
     * Access is granted if the user is an admin, the task owner, or listed as a responsible user.
     *
     * @param task The task entity to check against.
     * @param userId The ID of the user to check.
     * @param roles The roles of the user.
     * @throws AccessDeniedException if access is denied.
     */
    private void ensureUserCanAccessTask(TaskEntity task, Long userId, List<String> roles) {
        if (isAdmin(roles)) {
            return;
        }
        boolean isOwner = task.getOwnerUserId().equals(userId);
        boolean isResponsible = task.getResponsibleUserIds().contains(userId);
        if (!isOwner && !isResponsible) {
            throw new AccessDeniedException("FORBIDDEN - You do not have permission to access this task.");
        }
    }

    /**
     * Checks if the list of roles contains an admin role.
     *
     * @param roles The list of roles for a user.
     * @return True if an admin role is present, false otherwise.
     */
    private boolean isAdmin(List<String> roles) {
        return roles != null && roles.stream().anyMatch(role -> role.equalsIgnoreCase("ROLE_ADMIN"));
    }

    /**
     * Finds a task by its ID and verifies it belongs to the specified project.
     *
     * @param taskId The ID of the task to find.
     * @param projectId The ID of the project it must belong to.
     * @return The found TaskEntity.
     * @throws ResourceNotFoundException if the task is not found or does not belong to the project.
     */
    private TaskEntity findTaskByIdAndProject(Long taskId, Long projectId) {
        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task with ID " + taskId + " not found."));
        if (!taskEntity.getProjectId().equals(projectId)) {
            throw new ResourceNotFoundException("Task with ID " + taskId + " does not belong to project with ID " + projectId);
        }
        return taskEntity;
    }

    /**
     * Converts a string representation of a status to the Status enum.
     *
     * @param status The status string (e.g., "TODO").
     * @return The corresponding Status enum, or null if the input is null or empty.
     * @throws IllegalArgumentException if the status string is not a valid enum constant.
     */
    private Status transformStatusToEnum(String status) {
        if (status != null && !status.isEmpty()) {
            try {
                return Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid status value: " + status);
            }
        }
        return null;
    }

    /**
     * Converts a TaskEntity to a TaskResponseDTO.
     * This method enriches the DTO with full user objects for the owner and responsible users
     * by making calls to the UserServiceClient.
     *
     * @param taskEntity The entity to convert.
     * @param token The JWT token for service communication.
     * @return The fully populated TaskResponseDTO.
     */
    private TaskResponseDTO convertToDto(TaskEntity taskEntity, String token) {
        TaskResponseDTO dto = modelMapper.map(taskEntity, TaskResponseDTO.class);
        UserResponseDTO ownerDto = userServiceClient.getUserById(taskEntity.getOwnerUserId(), token);
        dto.setOwner(ownerDto);

        List<UserResponseDTO> responsibleDtos = taskEntity.getResponsibleUserIds().stream()
                .map(id -> userServiceClient.getUserById(id, token))
                .collect(Collectors.toList());
        dto.setUsersResponsability(responsibleDtos);

        return dto;
    }
}