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

    public TaskResponseDTO createTask(Long projectId, TaskRequestDTO taskRequestDTO, Long ownerId, List<String> roles) {
        // Validação de acesso ao projeto
        ensureUserCanViewProject(projectId, ownerId, roles);

        // Valida o criador
        userServiceClient.getUserById(ownerId);

        List<Long> responsibleIds = new ArrayList<>(taskRequestDTO.getUsersResponsability());
        for (Long responsibleId : responsibleIds) {
            userServiceClient.getUserById(responsibleId); // Valida cada responsável
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
        return convertToDto(savedEntity);
    }

    public TaskResponseDTO getTasksById(Long projectId, Long taskId, Long userId, List<String> roles) {
        TaskEntity taskEntity = findTaskByIdAndProject(taskId, projectId);
        ensureUserCanAccessTask(taskEntity, userId, roles);
        return convertToDto(taskEntity);
    }

    public PagedResponse<TaskResponseDTO> getAllTasksFromUserInProject(Pageable pageable, Long projectId, Long targetUserId, Long requestingUserId, List<String> roles) {
        ensureUserCanViewProject(projectId, requestingUserId, roles);

        if (!isAdmin(roles) && !requestingUserId.equals(targetUserId)) {
            throw new AccessDeniedException("FORBIDDEN - You do not have permission to access this user's tasks.");
        }

        userServiceClient.getUserById(targetUserId); // Valida se o usuário alvo existe

        Page<TaskEntity> tasksPage = taskRepository.findByProjectIdAndResponsibleUser(projectId, targetUserId, pageable);

        // CORREÇÃO: Chamando o novo método sobrecarregado do mapper
        return pagedResponseMapper.toPagedResponse(tasksPage, this::convertToDto);
    }


    public PagedResponse<TaskResponseFilteredDTO> getAllTasksFiltered(String status, LocalDateTime dueDate, Long projectId, Pageable pageable, Long userId, List<String> roles) {
        Status statusEnum = transformStatusToEnum(status);

        if (projectId != null) {
            ensureUserCanViewProject(projectId, userId, roles);
        }

        Page<TaskEntity> tasksPage;
        if (isAdmin(roles)) {
            tasksPage = taskRepository.findTasksFiltered(statusEnum, dueDate, projectId, pageable);
        } else {
            tasksPage = taskRepository.findTasksFilteredByUser(statusEnum, dueDate, projectId, userId, pageable);
        }
        return pagedResponseMapper.toPagedResponse(tasksPage, TaskResponseFilteredDTO.class);
    }


    public TaskResponseDTO updateTask(Long projectId, Long taskId, TaskRequestDTO taskRequestDTO, Long userId, List<String> roles) {
        TaskEntity taskEntity = findTaskByIdAndProject(taskId, projectId);
        ensureUserCanAccessTask(taskEntity, userId, roles);

        List<Long> responsibleIds = new ArrayList<>(taskRequestDTO.getUsersResponsability());
        for (Long responsibleId : responsibleIds) {
            userServiceClient.getUserById(responsibleId);
        }

        modelMapper.map(taskRequestDTO, taskEntity);
        taskEntity.setResponsibleUserIds(responsibleIds); // Atualiza a lista de responsáveis

        TaskEntity updatedEntity = taskRepository.save(taskEntity);
        return convertToDto(updatedEntity);
    }

    public TaskResponseDTO updateStatus(Long projectId, Long taskId, TaskRequestPatchDTO patchDTO, Long userId, List<String> roles) {
        TaskEntity taskEntity = findTaskByIdAndProject(taskId, projectId);
        ensureUserCanAccessTask(taskEntity, userId, roles);

        patchDTO.getStatus().ifPresent(taskEntity::setStatus);

        TaskEntity updatedEntity = taskRepository.save(taskEntity);
        return convertToDto(updatedEntity);
    }

    public void deleteTask(Long projectId, Long taskId, Long userId, List<String> roles) {
        TaskEntity taskEntity = findTaskByIdAndProject(taskId, projectId);
        ensureUserCanAccessTask(taskEntity, userId, roles);
        taskRepository.delete(taskEntity);
    }

    // --- Métodos Privados Auxiliares ---

    private void ensureUserCanViewProject(Long projectId, Long userId, List<String> roles) {
        if (isAdmin(roles)) {
            projectServiceClient.getProjectById(projectId); // Apenas valida se o projeto existe
            return;
        }
        // Para usuários não-admin, precisamos validar se ele pertence ao time do projeto
        ProjectResponseDTO project = projectServiceClient.getProjectById(projectId);
        boolean isUserInTeam = project.getTeam().stream().anyMatch(userDto -> userDto.getUserName().equals(userServiceClient.getUserById(userId).getUserName()));
        if (!isUserInTeam) {
             throw new AccessDeniedException("You do not have permission to access this project.");
        }
    }

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

    private boolean isAdmin(List<String> roles) {
        return roles != null && roles.stream().anyMatch(role -> role.equalsIgnoreCase("ROLE_ADMIN"));
    }

    private TaskEntity findTaskByIdAndProject(Long taskId, Long projectId) {
        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task with ID " + taskId + " not found."));
        if (!taskEntity.getProjectId().equals(projectId)) {
            throw new ResourceNotFoundException("Task with ID " + taskId + " does not belong to project with ID " + projectId);
        }
        return taskEntity;
    }

    private Status transformStatusToEnum(String status){
        if(status != null && !status.isEmpty()){
            try {
                return Status.valueOf(status.toUpperCase());
            } catch(IllegalArgumentException ex){
                throw new IllegalArgumentException("Invalid status value: " + status);
            }
        } 
        return null;
    }

    private TaskResponseDTO convertToDto(TaskEntity taskEntity) {
        TaskResponseDTO dto = modelMapper.map(taskEntity, TaskResponseDTO.class);
        UserResponseDTO ownerDto = userServiceClient.getUserById(taskEntity.getOwnerUserId());
        dto.setOwner(ownerDto);

        List<UserResponseDTO> responsibleDtos = taskEntity.getResponsibleUserIds().stream()
                .map(userServiceClient::getUserById)
                .collect(Collectors.toList());
        dto.setUsersResponsability(responsibleDtos);

        return dto;
    }
}