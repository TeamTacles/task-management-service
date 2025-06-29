package com.teamtacles.task.teamtacles_api_task.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.teamtacles.task.teamtacles_api_task.domain.model.enums.Status;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tasks")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 60, nullable = false)
    private String title;

    @Column(length = 250)
    private String description;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @ElementCollection
    @CollectionTable(name = "task_responsibles", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "responsible_user_id")
    private List<Long> responsibleUserIds;

    @Column(name = "project_id", nullable = false)
    private Long projectId;
}
