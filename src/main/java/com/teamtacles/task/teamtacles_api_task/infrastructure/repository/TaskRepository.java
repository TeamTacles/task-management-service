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
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing TaskEntity entities.
 * Extends JpaRepository to provide standard CRUD and pagination capabilities.
 * 
 * This interface defines custom query methods to retrieve tasks based on various criteria,
 * including project and user relationships, and dynamic filtering.
 *
 * @author TeamTacles
 * @version 1.1
 * @since 2025-07-04
 */
@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    
    Page<TaskEntity> findByStatus(Status status, Pageable pageable);

     /**
     * Retrieves a paginated list of tasks that match a specific status.
     *
     * @param status The status to filter tasks by.
     * @param pageable Pagination and sorting information.
     * @return A Page of tasks matching the given status.
     */
    @Query("SELECT t FROM TaskEntity t WHERE t.projectId = :projectId AND :userId MEMBER OF t.responsibleUserIds")
    Page<TaskEntity> findByProjectIdAndResponsibleUser(@Param("projectId") Long projectId, @Param("userId") Long userId, Pageable pageable);

    /**
     * Finds a paginated list of tasks using multiple optional filters, including a user-specific filter.
     *
     * The query filters by status, due date, project, and checks if the given user is either the
     * *owner* or a *member of the responsible users list*.
     *
     * @param status The task status to filter by. Can be null to ignore.
     * @param dueDate The latest due date. The query will find tasks due on or before this date. Can be null to ignore.
     * @param projectId The ID of the project to filter by. Can be null to ignore.
     * @param userId The ID of the user to filter by (as owner or responsible).
     * @param pageable Pagination and sorting information.
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
     * Finds a paginated list of tasks using multiple optional filters, without user-specific criteria.
     *
     * This query allows filtering by task status, due date, and the project it belongs to.
     *
     * @param status The task status to filter by. Can be null to ignore.
     * @param dueDate The latest due date. The query will find tasks due on or before this date. Can be null to ignore.
     * @param projectId The ID of the project to filter by. Can be null to ignore.
     * @param pageable Pagination and sorting information.
     * @return A Page of tasks matching the specified filters.
     */
    @Query("""
        SELECT t FROM TaskEntity t
        WHERE (:status IS NULL OR t.status = :status)
        AND (:dueDate IS NULL OR t.dueDate <= :dueDate)
        AND (:projectId IS NULL OR t.projectId = :projectId)
    """)
    Page<TaskEntity> findTasksFiltered(@Param("status") Status status, @Param("dueDate") LocalDateTime dueDate, @Param("projectId") Long projectId, Pageable pageable);
    
    /**
     * Finds all tasks associated with a specific project ID.
     * This query method is automatically implemented by Spring Data JPA based on its name.
     *
     * @param projectId The unique ID of the project for which to find tasks.
     * @return A List of TaskEntity objects belonging to the specified project. 
     * Returns an empty list if no tasks are found.
     */
    List<TaskEntity> findTasksByProjectId(Long projectId);
}