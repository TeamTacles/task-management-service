package com.teamtacles.task.teamtacles_api_task.infrastructure.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

// import com.teamtacles.task.teamtacles_api_task.domain.model.Project;
import com.teamtacles.task.teamtacles_api_task.domain.model.Task;
import com.teamtacles.task.teamtacles_api_task.domain.model.enums.Status;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Interface de repositório para gerenciar entidades {@link Task} na aplicação TeamTacles.
 * Estende {@link JpaRepository} para fornecer operações CRUD padrão e capacidades de paginação.
 *
 * Esta interface define métodos de consulta personalizados para recuperar tarefas com base em vários critérios,
 * incluindo relacionamentos com projetos e usuários, e buscas filtradas.
 *
 * @author Equipe de Desenvolvimento TeamTacles
 * @version 1.0
 * @since 2025-05-026
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Page<Task> findByProject(Project project, Pageable Pageable);
    Page<Task> findByStatus(Status status, Pageable pageable);

     /**
     * Finds a paginated list of tasks within a specific project where a given user
     * is listed in the task's responsibilities.
     *
     * @param projectId The ID of the Project to search tasks within.
     * @param userId The ID of the User responsible for the task.
     * @param pageable Pagination information (page number, page size, sorting).
     * @return A Page of tasks matching the project and user responsibility criteria.
     */
    @Query("SELECT t FROM Task t JOIN t.usersResponsability u WHERE t.project.id = :projectId AND u.id = :userId")
    Page<Task> findByProjectIdAndUsersResponsabilityId(Long projectId, Long userId, Pageable pageable);

     /**
     * Finds a paginated list of tasks based on multiple optional filtering criteria.
     * This query allows filtering by task status, due date (tasks due on or before this date),
     * the project it belongs to, and whether a specific user is the owner or among the responsible users.
     *
     * @param statusEnum The Status to filter by (optional: {null} to ignore).
     * @param dueDate The maximum due date to filter by (optional: { null} to ignore).
     * @param projectId The ID of the Project to filter by (optional: {null} to ignore).
     * @param userId The ID of the User (owner or responsible) to filter by (optional: {null} to ignore).
     * @param pageable Pagination information (page number, page size, sorting).
     * @return A Page of tasks matching the specified filters.
     */
     @Query("""
        SELECT DISTINCT t FROM Task t
        LEFT JOIN t.usersResponsability ur
        WHERE COALESCE(:statusEnum, t.status) = t.status
        AND t.dueDate <= COALESCE(:dueDate, t.dueDate)
        AND COALESCE(:projectId, t.project.id) = t.project.id
        AND (t.owner.id = :userId OR ur.id = :userId)
    """)
    Page<Task> findTasksFilteredByUser(Status statusEnum, LocalDateTime dueDate, Long projectId, Long userId, Pageable pageable);

    /**
     * Finds a paginated list of tasks based on multiple optional filtering criteria.
     * This query allows filtering by task status, due date (tasks due on or before this date),
     * and the project it belongs to. This version does not filter by user responsibility.
     *
     * @param statusEnum The Status to filter by (optional: {null} to ignore).
     * @param dueDate The maximum due date to filter by (optional: {null} to ignore).
     * @param projectId The ID of the Project to filter by (optional: {null} to ignore).
     * @param pageable Pagination information (page number, page size, sorting).
     * @return A Page of tasks matching the specified filters.
     */
    @Query("""
        SELECT DISTINCT t FROM Task t
        WHERE COALESCE(:statusEnum, t.status) = t.status
        AND t.dueDate <= COALESCE(:dueDate, t.dueDate)
        AND COALESCE(:projectId, t.project.id) = t.project.id
    """)
    Page<Task> findTasksFiltered(Status statusEnum, LocalDateTime dueDate, Long projectId, Pageable pageable);  
}