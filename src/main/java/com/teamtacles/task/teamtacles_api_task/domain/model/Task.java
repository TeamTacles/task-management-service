package com.teamtacles.task.teamtacles_api_task.domain.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.teamtacles.task.teamtacles_api_task.domain.model.enums.Status;
import com.teamtacles.task.teamtacles_api_task.domain.model.valueObject.ProjectId;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
import jakarta.persistence.Table;
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
@Table(name = "tasks") 
public class Task {
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

    private Long ownerUserId;

    private List<Long> responsibleUserIds;

     // NOVO: Projeto associado como ProjectId Value Object
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "project_id", nullable = false)) // Mapeia o 'value' do ProjectId para a coluna 'project_id'
    private ProjectId projectId;

    

    /* 
    antes:

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
    */
}
