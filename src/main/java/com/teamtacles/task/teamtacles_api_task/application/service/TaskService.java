package com.teamtacles.task.teamtacles_api_task.application.service;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.teamtacles.task.teamtacles_api_task.domain.model.TaskUser;
import com.teamtacles.task.teamtacles_api_task.domain.model.TaskProject;
import com.teamtacles.task.teamtacles_api_task.domain.model.enums.Status;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a task within a project in the TeamTacles application.
 * Each task has a unique identifier, a title, an optional description,
 * a due date, a current status, an assigned owner, a list of responsible users,
 * and is associated with a specific project.
 *
 * @author TeamTacles 
 * @version 1.0
 * @since 2025-05-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TaskService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 50)
	@NotBlank(message="The title cannot be blank!")
    private String title; 

    @Size(max = 250)
    private String description;

    @NotNull
    @Future(message="The due date must be in the future") 
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    // owner das tasks 
    @NotNull
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    @JsonBackReference(value = "user-task")
    private TaskUser owner;

    // lista de responsabilidades
    @ManyToMany
    @JoinTable(name = "users_responsability", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "userId"))
    private List<TaskUser> usersResponsability;

    // projetos que a task está associada
    @NotNull
    @ManyToOne(optional = false) // composição - temq pertencer a algum projeto
    @JoinColumn(name = "project_id", nullable = false)
    @JsonBackReference(value = "project-task")
    private TaskProject project;
}
