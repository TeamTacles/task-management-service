package com.teamtacles.task.teamtacles_api_task.task;
import com.teamtacles.task.teamtacles_api_task.application.dto.request.TaskRequestDTO;
import com.teamtacles.task.teamtacles_api_task.application.dto.response.PagedResponse;
import com.teamtacles.task.teamtacles_api_task.application.dto.response.ProjectResponseDTO;
import com.teamtacles.task.teamtacles_api_task.application.dto.response.ProjectResponseFilteredDTO;
import com.teamtacles.task.teamtacles_api_task.application.dto.response.TaskResponseDTO;
import com.teamtacles.task.teamtacles_api_task.application.dto.response.TaskResponseFilteredDTO;
import com.teamtacles.task.teamtacles_api_task.application.dto.response.UserResponseDTO;
import com.teamtacles.task.teamtacles_api_task.application.service.ProjectServiceClient;
import com.teamtacles.task.teamtacles_api_task.application.service.TaskService;
import com.teamtacles.task.teamtacles_api_task.application.service.UserServiceClient;
import com.teamtacles.task.teamtacles_api_task.domain.model.enums.Status;
import com.teamtacles.task.teamtacles_api_task.infrastructure.exception.ResourceNotFoundException;
import com.teamtacles.task.teamtacles_api_task.infrastructure.mapper.PagedResponseMapper;
import com.teamtacles.task.teamtacles_api_task.infrastructure.persistence.entity.TaskEntity;
import com.teamtacles.task.teamtacles_api_task.infrastructure.repository.TaskRepository;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;    

    @Mock
    UserServiceClient userServiceClient;

    @Mock
    ProjectServiceClient projectServiceClient;

    @InjectMocks
    private TaskService taskService;

    private UserResponseDTO adminUserDto;
    private UserResponseDTO normalUserDto;
    private UserResponseDTO responsibleUserDto;
    private UserResponseDTO otherUserDto;
    private ProjectResponseDTO testProjectDto;
    private TaskEntity existingTaskEntity;
    private List<String> adminRoles, userRoles;
    private String fakeToken;

    @BeforeEach 
    void setUp() {

        // Configuração dos papéis
        fakeToken = "fake-jwt-token";
        adminRoles = List.of("ROLE_ADMIN");
        userRoles = List.of("ROLE_USER");

        // Configuração do Usuário Admin
        adminUserDto = new UserResponseDTO();
        adminUserDto.setUserName("admin");
        

        // Configuração do Usuário Normal
        normalUserDto = new UserResponseDTO();
        normalUserDto.setUserName("normaluser");

        // Configuração do Usuário Responsável
        responsibleUserDto = new UserResponseDTO();
        responsibleUserDto.setUserName("responsibleuser");

        // Configuração do Outro Usuário
        otherUserDto = new UserResponseDTO();
        otherUserDto.setUserName("otheruser");

        // Configuração do Projeto de Teste

        testProjectDto = new ProjectResponseDTO();
        testProjectDto.setId(100L);
        testProjectDto.setTitle("Test Project");
        testProjectDto.setTeam(List.of(normalUserDto, responsibleUserDto, adminUserDto));

        // Configurando Tarefa existente
        existingTaskEntity = new TaskEntity();
        existingTaskEntity.setId(1L);
        existingTaskEntity.setTitle("Existing Task");
        existingTaskEntity.setProjectId(testProjectDto.getId());
        existingTaskEntity.setOwnerUserId(2L); // ID do usuário normal
        existingTaskEntity.setStatus(Status.INPROGRESS);
        existingTaskEntity.setResponsibleUserIds(List.of(3L)); // ID do responsibleUser
        existingTaskEntity.setDueDate(LocalDateTime.now().plusDays(5));
        testProjectDto.setCreator(normalUserDto); 


    }

    @Test
    @DisplayName("1.1: Should create a task successfully when data is valid")
    void createTask_shouldCreateTaskSuccessfully_whenDataIsValid() {

        // Arrange
        Long projectId = 100L; // ID do projeto de teste
        Long ownerId = 2L; // ID do usuário normal
        String fakeToken = "fake-jwt-token"; // Simulando um token JWT válido
        List<String> userRoles = List.of("ROLE_USER"); // Simulando os papéis do usuário

        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("New Task");
        requestDTO.setUsersResponsability(List.of(3L)); // o id do usuário responsável

        //simulando o que o cliente REST retornaria

        when(projectServiceClient.getProjectById(anyLong(), anyString())).thenReturn(testProjectDto);
        
        when(userServiceClient.getUserById(eq(2L), anyString())).thenReturn(normalUserDto); // Simula retorno para o dono
        when(userServiceClient.getUserById(eq(3L), anyString())).thenReturn(responsibleUserDto); // Simula retorno para o responsável

        when(modelMapper.map(any(TaskRequestDTO.class), eq(TaskEntity.class))).thenReturn(new TaskEntity());
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(existingTaskEntity); // Retorna a entidade mockada do setUp

        
        when(modelMapper.map(any(TaskEntity.class), eq(TaskResponseDTO.class))).thenReturn(new TaskResponseDTO());

        // ACT 
        TaskResponseDTO actualResponseDTO = taskService.createTask(projectId, requestDTO, ownerId, userRoles, fakeToken);

        // ASSERT 
        assertNotNull(actualResponseDTO, "The response DTO should not be null.");

        // para o dev do futuro, aqui garantimos pelo menos duas chamadas mas o taskService.createTask dispara 5 chamadas ao userServiceClient, se mudar  verifique se o teste vai fzr sentido
        verify(projectServiceClient, times(2)).getProjectById(anyLong(), anyString()); // futuramente alterar de 2 chamadas para apenas 1 
        verify(userServiceClient, atLeast(2)).getUserById   (anyLong(), eq(fakeToken));


        ArgumentCaptor<TaskEntity> taskCaptor = ArgumentCaptor.forClass(TaskEntity.class);
        verify(taskRepository).save(taskCaptor.capture());
        TaskEntity savedEntity = taskCaptor.getValue();

        assertEquals(ownerId, savedEntity.getOwnerUserId());
        assertEquals(projectId, savedEntity.getProjectId());
        assertEquals(Status.TODO, savedEntity.getStatus());
        assertTrue(savedEntity.getResponsibleUserIds().contains(3L));
    }

    @Test
    @DisplayName("1.2: Should throw ResourceNotFoundException when project does not exist")
    void createTask_shouldThrowResourceNotFoundException_whenProjectNotFound() {

        // Arrange
        Long nonexistentProjectId = 999L; // Um ID de projeto que não existe
        Long ownerId = 2L;
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Task for Nonexistent Project");
        requestDTO.setUsersResponsability(List.of(3L));

        when(projectServiceClient.getProjectById(nonexistentProjectId, fakeToken))
        .thenThrow(new ResourceNotFoundException("Project with ID " + nonexistentProjectId + " not found in the monolith."));

        // Act & Assert

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.createTask(nonexistentProjectId, requestDTO, ownerId, userRoles, fakeToken);
        });

        assertTrue(exception.getMessage().contains("Project with ID " + nonexistentProjectId + " not found"));

        verify(taskRepository, never()).save(any(TaskEntity.class));
        verify(userServiceClient, never()).getUserById(anyLong(), anyString()); // Não deve nem chegar a validar os usuários
    }

    @Test
    @DisplayName("1.3: Should throw ResourceNotFoundException_whenResponsibleUserNotFound") 
    void createTask_shouldThrowResourceNotFoundException_whenResponsibleUserNotFound() {
        // Arrange
        Long projectId = 100L;
        Long ownerId = 2L;
        Long existentResponsibleId = 3L;
        Long nonexistentResponsibleId = 999L;

        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Task with Nonexistent Responsible User");
        requestDTO.setUsersResponsability(List.of(existentResponsibleId, nonexistentResponsibleId));

        when(projectServiceClient.getProjectById(anyLong(), anyString())).thenReturn(testProjectDto);
        when(userServiceClient.getUserById(eq(ownerId), anyString())).thenReturn(normalUserDto);
        when(userServiceClient.getUserById(eq(existentResponsibleId), anyString())).thenReturn(responsibleUserDto);

        when(userServiceClient.getUserById(eq(nonexistentResponsibleId), anyString()))
            .thenThrow(new ResourceNotFoundException("User with ID " + nonexistentResponsibleId + " not found."));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.createTask(projectId, requestDTO, ownerId, userRoles, fakeToken);
        });

        assertTrue(exception.getMessage().contains("User with ID " + nonexistentResponsibleId + " not found."));
        verify(taskRepository, never()).save(any(TaskEntity.class));
            verify(projectServiceClient, times(2)).getProjectById(anyLong(), anyString()); //futuramente reparar no numero de invocacoes
        verify(userServiceClient, times(4)).getUserById(anyLong(), anyString()); // chama 4 vezes o userServiceClient

    }

    @Test
    @DisplayName("1.4: Deve lançar AccessDeniedException quando o usuário não pode visualizar o projeto")
    void createTask_shouldThrowAccessDeniedException_whenUserCannotViewProject() {
        // Arrange 
        Long projectId = 100L;
        Long unauthorizedOwnerId = 4L; // ID do otherUserDto
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Task in inaccessible project");
        requestDTO.setUsersResponsability(List.of(3L));

        when(projectServiceClient.getProjectById(anyLong(), anyString())).thenReturn(testProjectDto);
        when(userServiceClient.getUserById(eq(unauthorizedOwnerId), anyString())).thenReturn(otherUserDto);

        // ACT && ASSERT
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            taskService.createTask(projectId, requestDTO, unauthorizedOwnerId, userRoles, fakeToken);
        });

        assertEquals("You do not have permission to access this project.", exception.getMessage());

        // O projeto e o usuário foram verificados (1 vez cada).
        verify(projectServiceClient, times(1)).getProjectById(projectId, fakeToken);
        verify(userServiceClient, times(1)).getUserById(unauthorizedOwnerId, fakeToken);
        
        verify(userServiceClient, times(1)).getUserById(anyLong(), anyString()); // Apenas a chamada acima deve ocorrer
        verify(taskRepository, never()).save(any(TaskEntity.class));
    }

    @Test
    @DisplayName("2.1: Admin should get any task")
    void getTasksById_shouldReturnTask_whenUserIsAdmin() {

    Long projectId = 100L;
    Long taskId = 1L;
    Long adminId = 1L; // ID do adminUserDto
    String fakeToken = "fake-jwt-token";
    List<String> adminRoles = List.of("ROLE_ADMIN");

    
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTaskEntity));

    when(modelMapper.map(any(TaskEntity.class), eq(TaskResponseDTO.class))).thenReturn(new TaskResponseDTO());

    when(userServiceClient.getUserById(eq(2L), anyString())).thenReturn(normalUserDto);
    when(userServiceClient.getUserById(eq(3L), anyString())).thenReturn(responsibleUserDto);

    // ACT 
    TaskResponseDTO actualResponseDTO = taskService.getTasksById(projectId, taskId, adminId, adminRoles, fakeToken);

    // ASSERT 
    assertNotNull(actualResponseDTO, "The DTO should not be null for an admin.");

    verify(taskRepository, times(1)).findById(taskId);
    verify(userServiceClient, times(2)).getUserById(anyLong(), eq(fakeToken));
        
    }

    @Test
    @DisplayName("2.2: Owner should get their own task by ID successfully")
        void getTasksById_shouldReturnTask_whenUserIsOwner() {
        //  ARRANGE 
        Long projectId = 100L;
        Long taskId = 1L;
        Long ownerId = 2L; // ID do normalUserDto, que é o dono da tarefa
        String fakeToken = "fake-jwt-token";
        List<String> userRoles = List.of("ROLE_USER");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTaskEntity));

        when(modelMapper.map(any(TaskEntity.class), eq(TaskResponseDTO.class))).thenReturn(new TaskResponseDTO());
        when(userServiceClient.getUserById(eq(2L), anyString())).thenReturn(normalUserDto); // Para o dono
        when(userServiceClient.getUserById(eq(3L), anyString())).thenReturn(responsibleUserDto); // Para o responsável

        //  ACT 
        TaskResponseDTO actualResponseDTO = taskService.getTasksById(projectId, taskId, ownerId, userRoles, fakeToken);

        //  ASSERT 
        assertNotNull(actualResponseDTO, "The DTO should not be null.");

        verify(taskRepository, times(1)).findById(taskId);
        verify(userServiceClient, times(2)).getUserById(anyLong(), eq(fakeToken)); // 1 para o dono + 1 para o responsável
    }

    @Test
    @DisplayName("2.3: Responsible user should get task by ID successfully")
    void getTasksById_shouldReturnTask_whenUserIsResponsible() {
        // ARRANGE 
        Long projectId = 100L;
        Long taskId = 1L;
        Long responsibleId = 3L; // ID do responsibleUserDto

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTaskEntity));

        when(modelMapper.map(any(TaskEntity.class), eq(TaskResponseDTO.class))).thenReturn(new TaskResponseDTO());
        when(userServiceClient.getUserById(eq(2L), anyString())).thenReturn(normalUserDto);
        when(userServiceClient.getUserById(eq(3L), anyString())).thenReturn(responsibleUserDto);

        // ACT 
        TaskResponseDTO actualResponseDTO = taskService.getTasksById(projectId, taskId, responsibleId, userRoles, fakeToken);

        // ASSERT 
        assertNotNull(actualResponseDTO, "Response DTO should not be null for responsible user.");

        verify(taskRepository, times(1)).findById(taskId);

        verify(userServiceClient, times(2)).getUserById(anyLong(), eq(fakeToken));
    }

    @Test
    @DisplayName("2.4: Should throw ResourceNotFoundException when task ID does not exist")
    void getTasksById_shouldThrowResourceNotFoundException_whenTaskNotFound() {
        //  ARRANGE 
        Long nonexistentTaskId = 999L;
        Long projectId = 100L;
        Long requesterId = 2L; // ID do usuário que faz a requisição

        when(taskRepository.findById(nonexistentTaskId)).thenReturn(Optional.empty());

        //  ACT & ASSERT 
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getTasksById(projectId, nonexistentTaskId, requesterId, userRoles, fakeToken);
        });

        verify(modelMapper, never()).map(any(), any());
        verify(userServiceClient, never()).getUserById(anyLong(), anyString());
        verify(projectServiceClient, never()).getProjectById(anyLong(), anyString());
    }

    @Test
    @DisplayName("2.5: Should throw ResourceNotFoundException when task does not belong to the specified project")
    void getTasksById_shouldThrowResourceNotFoundException_whenTaskDoesNotBelongToProject() {
        //  ARRANGE 
        Long taskId = 1L;
        Long differentProjectId = 777L; 
        Long requesterId = 2L; // O usuário que faz a requisição

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTaskEntity));

        //  ACT & ASSERT 
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getTasksById(differentProjectId, taskId, requesterId, userRoles, fakeToken);
        });

        assertTrue(exception.getMessage().contains("does not belong to project with ID"));

        verify(modelMapper, never()).map(any(), any());
        verify(userServiceClient, never()).getUserById(anyLong(), anyString());
    }

    @Test
    @DisplayName("2.6: Should throw AccessDeniedException when unauthorized user tries to access task by ID")
    void getTasksById_shouldThrowAccessDeniedException_whenUnauthorizedUserIsNotAuthorized() {
        //  ARRANGE 
        Long projectId = 100L;
        Long taskId = 1L;
        Long unauthorizedUserId = 4L; // ID do otherUserDto

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTaskEntity));

        //  ACT & ASSERT 
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            taskService.getTasksById(projectId, taskId, unauthorizedUserId, userRoles, fakeToken);
        });

        assertTrue(exception.getMessage().contains("FORBIDDEN - You do not have permission to access this task."));

        verify(modelMapper, never()).map(any(), any());
        verify(userServiceClient, never()).getUserById(anyLong(), anyString());
    }


    @Test
    @DisplayName("3.1: Admin should get all tasks for a specific user in a specific project")
    void getAllTasksFromUserInProject_shouldReturnPagedTasks_whenUserIsAdmin() {
        //  ARRANGE 
        Long projectIdToSearch = 100L;
        Long userIdToSearchTasksFor = 3L; // Buscando tarefas do responsibleUser
        Long adminId = 1L; // O admin está fazendo a requisição
        Pageable pageable = PageRequest.of(0, 10);

        when(projectServiceClient.getProjectById(anyLong(), anyString())).thenReturn(testProjectDto);
        when(userServiceClient.getUserById(anyLong(), anyString())).thenReturn(adminUserDto, responsibleUserDto);

        List<TaskEntity> tasksFromRepo = List.of(existingTaskEntity);
        Page<TaskEntity> tasksPageFromRepo = new PageImpl<>(tasksFromRepo, pageable, tasksFromRepo.size());
        when(taskRepository.findByProjectIdAndResponsibleUser(projectIdToSearch, userIdToSearchTasksFor, pageable))
            .thenReturn(tasksPageFromRepo);

        
        TaskResponseDTO taskDto = new TaskResponseDTO();
        taskDto.setId(existingTaskEntity.getId());
        taskDto.setTitle(existingTaskEntity.getTitle());
        PagedResponse<TaskResponseDTO> expectedPagedResponse = new PagedResponse<>(
            List.of(taskDto), 0, 10, 1, 1, true);
        
        when(pagedResponseMapper.toPagedResponse(any(Page.class), any(Function.class))).thenReturn(expectedPagedResponse);

        // ACT 
        PagedResponse<TaskResponseDTO> actualPagedResponse = taskService.getAllTasksFromUserInProject(
            pageable, projectIdToSearch, userIdToSearchTasksFor, adminId, adminRoles, fakeToken);

        // ASSERT
        assertNotNull(actualPagedResponse, "The paged response should not be null.");
        assertEquals(1, actualPagedResponse.getTotalElements(), "Total elements should match.");
        assertFalse(actualPagedResponse.getContent().isEmpty(), "Content should not be empty.");
        assertEquals(existingTaskEntity.getTitle(), actualPagedResponse.getContent().get(0).getTitle());

        verify(projectServiceClient, times(1)).getProjectById(anyLong(), anyString());
        verify(userServiceClient, times(1)).getUserById(anyLong(), anyString());
        verify(taskRepository, times(1)).findByProjectIdAndResponsibleUser(projectIdToSearch, userIdToSearchTasksFor, pageable);
        verify(pagedResponseMapper, times(1)).toPagedResponse(any(Page.class), any(Function.class));
    }   

    @Test
    @DisplayName("3.2: Should throw AccessDeniedException when a non-admin user tries to get tasks from another user in a project")
    void getAllTasksFromUserInProject_shouldThrowAccessDeniedException_whenUserIsNotAdmin() {
        // ARRANGE
        Long projectIdToSearch = 100L;
        Long targetUserId = 3L;      // Tarefas do responsibleUser
        Long requestingUserId = 2L; // normalUser está fazendo a requisição
        Pageable pageable = PageRequest.of(0, 10);

        when(projectServiceClient.getProjectById(anyLong(), anyString())).thenReturn(testProjectDto);
        when(userServiceClient.getUserById(anyLong(), anyString())).thenReturn(normalUserDto, responsibleUserDto);
    
        // ACT & ASSERT 
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            taskService.getAllTasksFromUserInProject(pageable, projectIdToSearch, targetUserId, requestingUserId, userRoles, fakeToken);
        });

        assertEquals("FORBIDDEN - You do not have permission to access this user's tasks.", exception.getMessage());

        verify(taskRepository, never()).findByProjectIdAndResponsibleUser(anyLong(), anyLong(), any(Pageable.class));
        verify(pagedResponseMapper, never()).toPagedResponse(any(Page.class), any(java.util.function.Function.class));
    }

    @Test
    @DisplayName("3.3: Should throw ResourceNotFoundException when project is not found (admin access)")
    void getAllTasksFromUserInProject_shouldThrowResourceNotFoundException_whenProjectNotFoundForAdmin() {
        // ARRANGE 
        Long nonexistentProjectId = 888L;
        Long userIdToSearchTasksFor = 3L;
        Long adminId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        when(projectServiceClient.getProjectById(nonexistentProjectId, fakeToken))
            .thenThrow(new ResourceNotFoundException("Project not found."));

        // ACT & ASSERT 
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getAllTasksFromUserInProject(pageable, nonexistentProjectId, userIdToSearchTasksFor, adminId, adminRoles, fakeToken);
        });

        verify(userServiceClient, never()).getUserById(anyLong(), anyString());
        verify(taskRepository, never()).findByProjectIdAndResponsibleUser(anyLong(), anyLong(), any(Pageable.class));
        verify(pagedResponseMapper, never()).toPagedResponse(any(Page.class), any(java.util.function.Function.class));
    }


@Test
@DisplayName("3.4: Should throw ResourceNotFoundException when target user for task search is not found (admin access)")
void getAllTasksFromUserInProject_shouldThrowResourceNotFoundException_whenTargetUserNotFoundForAdmin() {
    // ARRANGE 
    
    Long projectIdToSearch = 100L;
    Long nonexistentUserId = 997L;
    Long adminId = 1L;
    Pageable pageable = PageRequest.of(0, 10);

   
    when(projectServiceClient.getProjectById(anyLong(), anyString())).thenReturn(testProjectDto);
    
    when(userServiceClient.getUserById(nonexistentUserId, fakeToken))
        .thenThrow(new ResourceNotFoundException("User Not Found."));

    // ACT & ASSERT 
    assertThrows(ResourceNotFoundException.class, () -> {
        taskService.getAllTasksFromUserInProject(pageable, projectIdToSearch, nonexistentUserId, adminId, adminRoles, fakeToken);
    });

    verify(taskRepository, never()).findByProjectIdAndResponsibleUser(anyLong(), anyLong(), any(Pageable.class));
    verify(pagedResponseMapper, never()).toPagedResponse(any(Page.class), any(java.util.function.Function.class));
    
    verify(projectServiceClient, times(1)).getProjectById(anyLong(), anyString());
    verify(userServiceClient, times(1)).getUserById(nonexistentUserId, fakeToken);
}


@Test
@DisplayName("3.5: Should return an empty page when target user has no tasks in the project (admin access)")
void getAllTasksFromUserInProject_shouldReturnEmptyPage_whenTargetUserHasNoTasks() {
    // ARRANGE 
    Long projectIdToSearch = 100L;
    Long userIdToSearchTasksFor = 3L;
    Long adminId = 1L;
    Pageable pageable = PageRequest.of(0, 10);

    when(projectServiceClient.getProjectById(anyLong(), anyString())).thenReturn(testProjectDto);
    when(userServiceClient.getUserById(anyLong(), anyString())).thenReturn(adminUserDto, responsibleUserDto);

    Page<TaskEntity> emptyTasksPageFromRepo = new PageImpl<>(List.of(), pageable, 0);
    when(taskRepository.findByProjectIdAndResponsibleUser(projectIdToSearch, userIdToSearchTasksFor, pageable))
        .thenReturn(emptyTasksPageFromRepo);

    PagedResponse<TaskResponseDTO> expectedEmptyPagedResponse = new PagedResponse<>(
        List.of(), 0, 10, 0, 0, true);
    when(pagedResponseMapper.toPagedResponse(any(Page.class), any(java.util.function.Function.class)))
        .thenReturn(expectedEmptyPagedResponse);

    // ACT 
    PagedResponse<TaskResponseDTO> actualPagedResponse = taskService.getAllTasksFromUserInProject(
        pageable, projectIdToSearch, userIdToSearchTasksFor, adminId, adminRoles, fakeToken);

    // ASSERT 
    assertNotNull(actualPagedResponse, "PagedResponse should not be null even if content is empty.");
    assertTrue(actualPagedResponse.getContent().isEmpty(), "Content list should be empty.");
    assertEquals(0, actualPagedResponse.getTotalElements(), "Total elements should be 0.");

    verify(projectServiceClient, times(1)).getProjectById(anyLong(), anyString());
    verify(userServiceClient, times(1)).getUserById(userIdToSearchTasksFor, fakeToken); // Apenas o usuário alvo é buscado
    verify(taskRepository, times(1)).findByProjectIdAndResponsibleUser(projectIdToSearch, userIdToSearchTasksFor, pageable);
    verify(pagedResponseMapper, times(1)).toPagedResponse(any(Page.class), any(java.util.function.Function.class));
}

@Test
@DisplayName("4.1: Admin should get all tasks (no filters) successfully")
void getAllTasksFiltered_shouldReturnAllTasks_whenAdminAndNoFilters() {
    // ARRANGE 
    Pageable pageable = PageRequest.of(0, 5);
    Long adminId = 1L; // ID do adminUserDto
    List<TaskEntity> tasksFromRepo = List.of(existingTaskEntity);
    Page<TaskEntity> taskPageFromRepo = new PageImpl<>(tasksFromRepo, pageable, tasksFromRepo.size());
    when(taskRepository.findTasksFiltered(null, null, null, pageable))
        .thenReturn(taskPageFromRepo);
    when(projectServiceClient.getProjectById(anyLong(), anyString())).thenReturn(testProjectDto);
    when(userServiceClient.getUserById(eq(2L), anyString())).thenReturn(normalUserDto); // Dono da tarefa
    when(userServiceClient.getUserById(eq(3L), anyString())).thenReturn(responsibleUserDto); // Responsável
    when(modelMapper.map(any(TaskEntity.class), eq(TaskResponseFilteredDTO.class))).thenReturn(new TaskResponseFilteredDTO());
when(modelMapper.map(any(ProjectResponseDTO.class), any())).thenReturn(new ProjectResponseFilteredDTO());

    // ACT 
    PagedResponse<TaskResponseFilteredDTO> actualPagedResponse = taskService.getAllTasksFiltered(
        null, null, null, pageable, adminId, adminRoles, fakeToken
    );

    // ASSERT 
    assertNotNull(actualPagedResponse, "PagedResponse should not be null.");
    assertEquals(1, actualPagedResponse.getTotalElements(), "Total elements should match.");
    assertFalse(actualPagedResponse.getContent().isEmpty(), "Content should not be empty.");
    verify(taskRepository, times(1)).findTasksFiltered(null, null, null, pageable);
    verify(projectServiceClient, times(1)).getProjectById(anyLong(), anyString());
    verify(userServiceClient, times(2)).getUserById(anyLong(), anyString()); 
    verify(taskRepository, never()).findTasksFilteredByUser(any(), any(), any(), anyLong(), any(Pageable.class));
}

@Test
@DisplayName("4.2: Admin should get tasks with all filters applied")
void getAllTasksFiltered_shouldReturnFilteredTasks_whenAdminAndAllFiltersApplied() {
    // ARRANGE 
    Pageable pageable = PageRequest.of(0, 5);
    Long adminId = 1L;
    String statusFilterString = "INPROGRESS";
    Status expectedStatusEnum = Status.INPROGRESS;
    LocalDateTime dueDateFilter = LocalDateTime.now().plusDays(10);
    Long projectIdFilter = 100L;
    List<TaskEntity> tasksFromRepo = List.of(existingTaskEntity);
    Page<TaskEntity> taskPageFromRepo = new PageImpl<>(tasksFromRepo, pageable, tasksFromRepo.size());
    when(taskRepository.findTasksFiltered(expectedStatusEnum, dueDateFilter, projectIdFilter, pageable))
        .thenReturn(taskPageFromRepo);

    when(projectServiceClient.getProjectById(eq(100L), anyString())).thenReturn(testProjectDto);
    when(userServiceClient.getUserById(eq(2L), anyString())).thenReturn(normalUserDto);
    when(userServiceClient.getUserById(eq(3L), anyString())).thenReturn(responsibleUserDto);
    
    when(modelMapper.map(any(TaskEntity.class), eq(TaskResponseFilteredDTO.class))).thenReturn(new TaskResponseFilteredDTO());
    when(modelMapper.map(any(ProjectResponseDTO.class), any())).thenReturn(new ProjectResponseFilteredDTO());


    // ACT 
    PagedResponse<TaskResponseFilteredDTO> actualPagedResponse = taskService.getAllTasksFiltered(
        statusFilterString, dueDateFilter, projectIdFilter, pageable, adminId, adminRoles, fakeToken
    );

    //  ASSERT
    assertNotNull(actualPagedResponse, "PagedResponse should not be null.");
    assertEquals(1, actualPagedResponse.getTotalElements());
    verify(projectServiceClient, times(2)).getProjectById(projectIdFilter, fakeToken);
    verify(taskRepository, times(1)).findTasksFiltered(expectedStatusEnum, dueDateFilter, projectIdFilter, pageable);
    verify(projectServiceClient, times(2)).getProjectById(anyLong(), anyString());
    verify(userServiceClient, times(2)).getUserById(anyLong(), anyString());
}

@Test
@DisplayName("4.3: Admin should get ResourceNotFoundException when filtering by a non-existent projectId")
void getAllTasksFiltered_shouldThrowResourceNotFoundException_whenAdminFiltersByNonExistentProjectId() {
    //  ARRANGE 
    Pageable pageable = PageRequest.of(0, 5);
    Long adminId = 1L;
    Long nonExistentProjectId = 999L;

    when(projectServiceClient.getProjectById(nonExistentProjectId, fakeToken))
        .thenThrow(new ResourceNotFoundException("Project not found."));

    // ACT & ASSERT 
    assertThrows(ResourceNotFoundException.class, () -> {
        taskService.getAllTasksFiltered(
            "TODO", null, nonExistentProjectId, pageable, adminId, adminRoles, fakeToken
        );
    });

    verify(taskRepository, never()).findTasksFiltered(any(), any(), any(), any(Pageable.class));
    verify(taskRepository, never()).findTasksFilteredByUser(any(), any(), any(), anyLong(), any(Pageable.class));
    
    verify(projectServiceClient, times(1)).getProjectById(nonExistentProjectId, fakeToken);
}

@Test
@DisplayName("4.4: Admin should get IllegalArgumentException when filtering by an invalid status string")
void getAllTasksFiltered_shouldThrowIllegalArgumentException_whenAdminFiltersByInvalidStatusString() {
    //  ARRANGE 
    Pageable pageable = PageRequest.of(0, 5);
    Long adminId = 1L;
    String invalidStatusString = "INVALID_STATUS";

    // ACT & ASSERT 
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        taskService.getAllTasksFiltered(
            invalidStatusString, null, null, pageable, adminId, adminRoles, fakeToken
        );
    });

    assertTrue(exception.getMessage().contains("Invalid status value: " + invalidStatusString));
    verify(projectServiceClient, never()).getProjectById(anyLong(), anyString());
    verify(taskRepository, never()).findTasksFiltered(any(), any(), any(), any(Pageable.class));
    verify(taskRepository, never()).findTasksFilteredByUser(any(), any(), any(), anyLong(), any(Pageable.class));
    verify(pagedResponseMapper, never()).toPagedResponse(any(Page.class), any(java.util.function.Function.class));
}

@Test
@DisplayName("4.5: Normal user should get their tasks (no filters) using getAllTasksFiltered")
void getAllTasksFiltered_shouldReturnUserTasks_whenNormalUserAndNoFilters() {
    // ARRANGE 
    Pageable pageable = PageRequest.of(0, 10);
    Long normalUserId = 2L; // O usuário normal está fazendo a requisição

    List<TaskEntity> tasksFromRepo = List.of(existingTaskEntity);
    Page<TaskEntity> taskPageFromRepo = new PageImpl<>(tasksFromRepo, pageable, tasksFromRepo.size());
    when(taskRepository.findTasksFilteredByUser(null, null, null, normalUserId, pageable))
        .thenReturn(taskPageFromRepo);

    when(projectServiceClient.getProjectById(anyLong(), anyString())).thenReturn(testProjectDto);
    when(userServiceClient.getUserById(eq(2L), anyString())).thenReturn(normalUserDto);
    when(userServiceClient.getUserById(eq(3L), anyString())).thenReturn(responsibleUserDto);

    when(modelMapper.map(any(TaskEntity.class), eq(TaskResponseFilteredDTO.class))).thenReturn(new TaskResponseFilteredDTO());
    when(modelMapper.map(any(ProjectResponseDTO.class), any())).thenReturn(new ProjectResponseFilteredDTO());

    // ACT 
    PagedResponse<TaskResponseFilteredDTO> actualPagedResponse = taskService.getAllTasksFiltered(
        null, null, null, pageable, normalUserId, userRoles, fakeToken
    );

    // ASSERT 
    assertNotNull(actualPagedResponse, "PagedResponse should not be null.");
    assertEquals(1, actualPagedResponse.getTotalElements());

    verify(projectServiceClient, times(1)).getProjectById(anyLong(), anyString()); // Chamado apenas no enriquecimento

    verify(taskRepository, times(1)).findTasksFilteredByUser(null, null, null, normalUserId, pageable);
    
    verify(taskRepository, never()).findTasksFiltered(any(), any(), any(), any(Pageable.class));
}


@Test
@DisplayName("4.6: Normal user should get their tasks with filters applied using getAllTasksFiltered")
void getAllTasksFiltered_shouldReturnUserTasksForSpecificProject_whenNormalUserFiltersByValidProjectId() {
    // ARRANGE 
    Pageable pageable = PageRequest.of(0, 10);
    Long normalUserId = 2L; // O usuário que faz a requisição
    Long projectIdFilter = 100L;

    when(projectServiceClient.getProjectById(anyLong(), anyString())).thenReturn(testProjectDto);
    when(userServiceClient.getUserById(eq(normalUserId), anyString())).thenReturn(normalUserDto);

    List<TaskEntity> tasksFromRepo = List.of(existingTaskEntity);
    Page<TaskEntity> taskPageFromRepo = new PageImpl<>(tasksFromRepo, pageable, tasksFromRepo.size());
    when(taskRepository.findTasksFilteredByUser(null, null, projectIdFilter, normalUserId, pageable))
        .thenReturn(taskPageFromRepo);

    when(userServiceClient.getUserById(eq(3L), anyString())).thenReturn(responsibleUserDto);
    when(modelMapper.map(any(TaskEntity.class), eq(TaskResponseFilteredDTO.class))).thenReturn(new TaskResponseFilteredDTO());
    when(modelMapper.map(any(ProjectResponseDTO.class), any())).thenReturn(new ProjectResponseFilteredDTO());

    // ACT 
    PagedResponse<TaskResponseFilteredDTO> actualPagedResponse = taskService.getAllTasksFiltered(
        null, null, projectIdFilter, pageable, normalUserId, userRoles, fakeToken
    );

    // ASSERT 
    assertNotNull(actualPagedResponse);
    assertFalse(actualPagedResponse.getContent().isEmpty());
    assertEquals(1, actualPagedResponse.getTotalElements());

    verify(projectServiceClient, times(2)).getProjectById(anyLong(), anyString()); // 1 na validação, 1 no enriquecimento
    
    verify(taskRepository, times(1)).findTasksFilteredByUser(null, null, projectIdFilter, normalUserId, pageable);
    
    verify(taskRepository, never()).findTasksFiltered(any(), any(), any(), any(Pageable.class));
}

@Test
@DisplayName("4.7: Should throw AccessDeniedException when normal user tries to filter tasks by project they do not belong to")
void getAllTasksFiltered_shouldThrowAccessDeniedException_whenNormalUserFiltersByProjectIdTheyDoNotBelongTo() {
    // ARRANGE 
    Pageable pageable = PageRequest.of(0, 10);
    Long unauthorizedUserId = 4L; // ID do otherUserDto
    Long projectIdFilter = 100L;

    when(projectServiceClient.getProjectById(anyLong(), anyString())).thenReturn(testProjectDto);
    when(userServiceClient.getUserById(eq(unauthorizedUserId), anyString())).thenReturn(otherUserDto);
    // ACT & ASSERT 
    AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
        taskService.getAllTasksFiltered(
            null, null, projectIdFilter, pageable, unauthorizedUserId, userRoles, fakeToken
        );
    });

    assertEquals("You do not have permission to access this project.", exception.getMessage());

    verify(taskRepository, never()).findTasksFiltered(any(), any(), any(), any(Pageable.class));
    verify(taskRepository, never()).findTasksFilteredByUser(any(), any(), any(), anyLong(), any(Pageable.class));
    
    verify(projectServiceClient, times(1)).getProjectById(projectIdFilter, fakeToken);
    verify(userServiceClient, times(1)).getUserById(unauthorizedUserId, fakeToken);
}

    // Caio chamou a responsabilidade. ???????
    // uefa cristiano caio é isso e acabo
}






   