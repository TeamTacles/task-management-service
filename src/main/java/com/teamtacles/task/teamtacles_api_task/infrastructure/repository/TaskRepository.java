package com.teamtacles.task.teamtacles_api_task.infrastructure.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// import com.teamtacles.task.teamtacles_api_task.domain.model.Project;
import com.teamtacles.task.teamtacles_api_task.domain.model.Task;
import com.teamtacles.task.teamtacles_api_task.domain.model.enums.Status;
import com.teamtacles.task.teamtacles_api_task.infrastructure.persistence.entity.TaskEntity;

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
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    
    Page<TaskEntity> findByStatus(Status status, Pageable pageable);

     /**
     * Finds a paginated list of tasks within a specific project where a given user
     * is listed in the task's responsibilities.
     *
     * @param projectId The ID of the Project to search tasks within.
     * @param userId The ID of the User responsible for the task.
     * @param pageable Pagination information (page number, page size, sorting).
     * @return A Page of tasks matching the project and user responsibility criteria.
     */
    @Query("SELECT t FROM TaskEntity t WHERE t.projectId = :projectId AND :userId MEMBER OF t.responsibleUserIds")
    Page<TaskEntity> findByProjectIdAndResponsibleUser(@Param("projectId") Long projectId, @Param("userId") Long userId, Pageable pageable);

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
        SELECT t FROM TaskEntity t
        WHERE (:status IS NULL OR t.status = :status)
        AND (:dueDate IS NULL OR t.dueDate <= :dueDate)
        AND (:projectId IS NULL OR t.projectId = :projectId)
        AND (t.ownerUserId = :userId OR :userId MEMBER OF t.responsibleUserIds)
    """)
    Page<TaskEntity> findTasksFilteredByUser(@Param("status") Status status, @Param("dueDate") LocalDateTime dueDate, @Param("projectId") Long projectId, @Param("userId") Long userId, Pageable pageable);

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
        SELECT t FROM TaskEntity t
        WHERE (:status IS NULL OR t.status = :status)
        AND (:dueDate IS NULL OR t.dueDate <= :dueDate)
        AND (:projectId IS NULL OR t.projectId = :projectId)
    """)
    Page<TaskEntity> findTasksFiltered(@Param("status") Status status, @Param("dueDate") LocalDateTime dueDate, @Param("projectId") Long projectId, Pageable pageable);
}