package com.teamtacles.task.teamtacles_api_task.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.teamtacles.task.teamtacles_api_task.infrastructure.dto.request.TaskRequestDTO;
import com.teamtacles.task.teamtacles_api_task.infrastructure.dto.response.TaskResponseDTO;
import com.teamtacles.task.teamtacles_api_task.infrastructure.dto.response.TaskResponseFilteredDTO;
import com.teamtacles.task.teamtacles_api_task.infrastructure.mapper.PagedResponseMapper;
import com.teamtacles.task.teamtacles_api_task.infrastructure.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.modelmapper.ModelMapper;

/**
 * Service class responsible for managing business logic related to Task entities
 * in the TeamTacles application. This includes creating, retrieving, updating, and deleting tasks,
 * along with handling access control based on user roles, task ownership, and project membership.
 *
 * @author TeamTacles Development Team
 * @version 1.0
 * @since 2025-05-26
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserServiceClient userServiceClient;
    private final ProjectServiceClient projectServiceClient;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public TaskService(TaskRepository taskRepository, UserServiceClient userServiceClient, ProjectServiceClient projectServiceClient, ModelMapper modelMapper, PagedResponseMapper pagedResponseMapper){
        this.taskRepository = taskRepository;
        this.userServiceClient = userServiceClient;
        this.projectServiceClient = projectServiceClient;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    /**
     * Creates a new task within a specified project.
     * The authenticated user creating the task is set as the task owner.
     * Responsible users for the task are looked up by their IDs. If the owner is not
     * explicitly listed as responsible, they are added to the list.
     * The task's initial status is set to Status.TODO.
     *
     * @param id_project The ID of the Project to which the task belongs.
     * @param taskRequestDTO The TaskRequestDTO containing the details for the new task.
     * @param userFromToken The authenticated User who is creating the task.
     * @return A TaskResponseDTO representing the newly created task.
     */
    public TaskResponseDTO createTask(Long id_project, TaskRequestDTO taskRequestDTO, Long userId) {        
        Project project = findprojects(id_project);
        User creatorUser = findUsers(userFromToken.getUserId());

        projectService.ensureUserCanViewProject(project, creatorUser);

        List<User> usersResponsability = new ArrayList<>();
        
        for (Long userId : taskRequestDTO.getUsersResponsability()) {
            usersResponsability.add(findUsers(userId));
        }

        if (!usersResponsability.contains(creatorUser)) {
            usersResponsability.add(creatorUser);
        }
        
        Task convertedTask = modelMapper.map(taskRequestDTO, Task.class);
        convertedTask.setProject(project);
        convertedTask.setOwner(creatorUser);
        convertedTask.setStatus(Status.TODO);
        convertedTask.setUsersResponsability(usersResponsability);

        Task createdTask = taskRepository.save(convertedTask);
        return modelMapper.map(createdTask, TaskResponseDTO.class);
	}

    /**
     * Retrieves a single task by its ID within a specific project, ensuring the authenticated user has permission to view it.
     * The task must belong to the specified project.
     * Only the task owner, a responsible user, or an administrator can view a task.
     *
     * @param id_project The ID of the Project to which the task is expected to belong.
     * @param id_task The unique ID of the task to retrieve.
     * @param userFromToken The authenticated User attempting to view the task.
     * @return A TaskResponseDTO representing the found task.
     */
    public TaskResponseDTO getTasksById(Long id_project, Long id_task, Long userId){
        Task task = taskRepository.findById(id_task) 
                .orElseThrow(() -> new ResourceNotFoundException("Task Not Found."));

        ensureProjectMatchesTask(task, id_project);

        // Chama o método que verifica se o usuário é dono da tarefa ou se é um administrador
        ensureUserCanAccessTask(task, userFromToken);
        
        return modelMapper.map(task, TaskResponseDTO.class);
    }
    
    /**
     * Retrieves a paginated list of tasks assigned to a specific user within a given project.
     * An administrator can retrieve tasks for any user in any project.
     * A non-admin user can only retrieve their own tasks within a project.
     *
     * @param pageable Pagination information (page number, page size, sorting).
     * @param projectId The ID of the Project to filter tasks by.
     * @param userId The ID of the User whose tasks are to be retrieved.
     * @param userFromToken The authenticated User making the request.
     * @return A PagedResponse containing a page of TaskResponseDTO objects.
    */
    public PagedResponse<TaskResponseDTO> getAllTasksFromUserInProject(Pageable pageable, Long projectId, Long userId, Long userIdFromToken) {
        // Verifica se o projeto existe
        projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found."));

        // Verifica se o usuário existe
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Page<Task> tasksPage;

        if (isADM(userFromToken)) {
            tasksPage = taskRepository.findByProjectIdAndUsersResponsabilityId(projectId, userId, pageable);
        } else if (!userFromToken.getUserId().equals(userId)) {
            // Não ADM tentando acessar dados de outro user
            throw new AccessDeniedException("FORBIDDEN - You do not have permission to access this user's tasks.");
        } else {
            // Normal user: pode buscar suas tasks no projeto
            tasksPage = taskRepository.findByProjectIdAndUsersResponsabilityId(projectId, userId, pageable);
        }
        return pagedResponseMapper.toPagedResponse(tasksPage, TaskResponseDTO.class);
    }

    /**
     * Retrieves a paginated and filtered list of tasks based on various criteria such as status, due date, and project.
     * If the authenticated user is an administrator, all tasks matching the filters are returned.
     * Otherwise, only tasks where the user is an owner or a responsible party, and which match the filters, are returned.
     *
     * @param status The status of the task as a String ("TODO", "IN_PROGRESS", "DONE"). Can be null.
     * @param dueDate The due date to filter tasks by (tasks due on or before this date). Can be null.
     * @param projectId The ID of the Project to filter tasks by. Can be null.
     * @param pageable Pagination information (page number, page size, sorting).
     * @param userFromToken The authenticated User making the request.
     * @return A PagedResponse containing a page of TaskResponseFilteredDTO objects.
     */
    public PagedResponse<TaskResponseFilteredDTO> getAllTasksFiltered(String status, LocalDateTime dueDate, Long projectId, Pageable pageable, Long userId){        
        Status statusEnum = transformStatusToEnum(status);

        if(projectId != null){
            Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));

            projectService.ensureUserCanViewProject(project, userFromToken);
        }

        Page<Task> tasksList;
        if(isADM(userFromToken)){
            tasksList = taskRepository.findTasksFiltered(statusEnum, dueDate, projectId, pageable);
        }
        else{
            tasksList = taskRepository.findTasksFilteredByUser(statusEnum, dueDate, projectId, userFromToken.getUserId(), pageable);
        }
        return pagedResponseMapper.toPagedResponse(tasksList, TaskResponseFilteredDTO.class);
    }
    
     /**
     * Updates an existing task within a specified project.
     * The task must belong to the specified project.
     * Only the task owner, a responsible user, or an administrator can update a task.
     *
     * @param id_project The ID of the Project to which the task is expected to belong.
     * @param id_task The unique ID of the task to update.
     * @param taskRequestDTO The TaskRequestDTO containing the updated task details.
     * @param userFromToken The authenticated User attempting to update the task.
     * @return A TaskResponseDTO representing the updated task.
     */
    public TaskResponseDTO updateTask(Long id_project, Long id_task, TaskRequestDTO taskRequestDTO, Long userId) {
        Task task = taskRepository.findById(id_task)
            .orElseThrow(() -> new ResourceNotFoundException("Task Not Found."));
        // Chama o método que verifica se o usuário é dono da tarefa ou se é um administrador
        ensureProjectMatchesTask(task, id_project);
        ensureUserCanAccessTask(task, userFromToken);

        List<User> usersResponsability = new ArrayList<>();
        
        for (Long userId : taskRequestDTO.getUsersResponsability()) {
            usersResponsability.add(findUsers(userId));
        }

        modelMapper.map(taskRequestDTO, task);
        task.setId(id_task);
        task.setProject(task.getProject());
        task.setOwner(task.getOwner());
        task.setUsersResponsability(usersResponsability);
        Task updated = taskRepository.save(task);
        
        return modelMapper.map(updated, TaskResponseDTO.class);
    }

    /**
     * Partially updates an existing task, primarily used for updating the task's Status.
     * The task must belong to the specified project.
     * Only the task owner, a responsible user, or an administrator can update a task.
     *
     * @param id_project The ID of the Project to which the task is expected to belong.
     * @param id_task The unique ID of the task to partially update.
     * @param taskRequestPatchDTO The TaskRequestPatchDTO containing the status to be updated.
     * @param userFromToken The authenticated User attempting to update the task's status.
     * @return A TaskResponseDTO representing the partially updated task.
     */    
    public TaskResponseDTO updateStatus(Long id_project, Long id_task, TaskRequestPatchDTO taskRequestPatchDTO, Long userId){
        Task task = taskRepository.findById(id_task)
            .orElseThrow(() -> new ResourceNotFoundException("Task Not Found."));
        
        ensureProjectMatchesTask(task, id_project);
        ensureUserCanAccessTask(task, userFromToken);

        taskRequestPatchDTO.getStatus().ifPresent(task::setStatus);
        task.setId(id_task);
        task.setOwner(task.getOwner());
        
        Task taskUpdated = taskRepository.save(task);
        return modelMapper.map(taskUpdated, TaskResponseDTO.class);
    }

    /**
     * Deletes a task from the system.
     * The task must belong to the specified project.
     * Only the task owner, a responsible user, or an administrator can delete a task.
     *
     * @param id_project The ID of the Project to which the task is expected to belong.
     * @param id_task The unique ID of the task to delete.
     * @param userFromToken The authenticated User attempting to delete the task.
     */    
    public void deleteTask(Long id_project, Long id_task, Long userId) {
        Task task = taskRepository.findById(id_task)
            .orElseThrow(() -> new ResourceNotFoundException("Task Not Found."));
            // Chama o método que verifica se o usuário é dono da tarefa ou se é um administrador
        ensureProjectMatchesTask(task, id_project);
        ensureUserCanAccessTask(task, userFromToken);

        taskRepository.delete(task);
    }
    
    private User findUsers(Long id_task){
        User user = userRepository.findById(id_task).orElseThrow(() -> new ResourceNotFoundException("User Not Found."));
        return user;
    }

    private Project findprojects(Long id_project){
        Project project = projectRepository.findById(id_project).orElseThrow(() -> new ResourceNotFoundException("Project Not Found."));        
        return project;
    }

    // Verificando se o usuário é admin
    private boolean isADM(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName().equals(ERole.ADMIN));
    }

    // Validando se o usuário é dono do projeto, se ele não for adm/responsavel, ele não consegue criar, editar ou deletar tarefas de outros usuários
    private void ensureUserCanAccessTask(Task task, User user) {
        boolean isResposible = task.getUsersResponsability().stream() 
            .anyMatch(resposible -> resposible.getUserId().equals(user.getUserId())); //verifica se o usuário é responsável pela tarefa

        if(!isADM(user) && !task.getOwner().getUserId().equals(user.getUserId()) && !isResposible) {
            throw new AccessDeniedException (" FORBIDDEN - You do not have permission to modify this task."); 
        }
    }
    
    // Verifica se o projeto da tarefa corresponde ao projeto fornecido
    private void ensureProjectMatchesTask(Task task, Long id_project){
        if(!(task.getProject().getId() == id_project)){
            throw new ResourceNotFoundException("Task does not belong to the specified project.");
        }
    }

    private Status transformStatusToEnum(String status){
        if(status != null && !status.isEmpty()){
            try{
                return Status.valueOf(status.toUpperCase());
            } catch(IllegalArgumentException ex){
                throw new IllegalArgumentException("Invalid status value: " + status);
            }
        } 
        return null;
    }
}