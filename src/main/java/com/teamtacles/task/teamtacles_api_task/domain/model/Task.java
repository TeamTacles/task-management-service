package com.teamtacles.task.teamtacles_api_task.domain.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.teamtacles.task.teamtacles_api_task.domain.model.enums.Status;
import com.teamtacles.task.teamtacles_api_task.domain.valueObject.Description;
import com.teamtacles.task.teamtacles_api_task.domain.valueObject.DueDate;
import com.teamtacles.task.teamtacles_api_task.domain.valueObject.OwnerUserId;
import com.teamtacles.task.teamtacles_api_task.domain.valueObject.ProjectId;
import com.teamtacles.task.teamtacles_api_task.domain.valueObject.ResponsiblesList;
import com.teamtacles.task.teamtacles_api_task.domain.valueObject.TaskTitle;

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
    private final Long id;

    private TaskTitle title; 
    private Description description;

    @NotNull
    @Future(message="The due date must be in the future") 
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private DueDate dueDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private OwnerUserId ownerUserId;

    private ResponsiblesList responsibleUserIds;

    private ProjectId projectId;

}

