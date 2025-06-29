package com.teamtacles.task.teamtacles_api_task.infrastructure.mapper;

import com.teamtacles.task.teamtacles_api_task.domain.model.Task;
import com.teamtacles.task.teamtacles_api_task.domain.valueObject.*;
import com.teamtacles.task.teamtacles_api_task.infrastructure.persistence.entity.TaskEntity;

import java.util.stream.Collectors;

public class TaskMapper {

    public static Task toDomain(TaskEntity entity) {
        if (entity == null) return null;

        return new Task(
            new TaskId(entity.getId()),
            new TaskTitle(entity.getTitle()),
            new Description(entity.getDescription()),
            new DueDate(entity.getDueDate()),
            entity.getStatus(),
            new OwnerUserId(entity.getOwnerUserId()),
            new ResponsiblesList(entity.getResponsibleUserIds()),
            new ProjectId(entity.getProjectId())
        );
    }

    public static TaskEntity toEntity(Task domain) {
        if (domain == null) return null;

        return new TaskEntity(
            domain.getId().value(),
            domain.getTitle().value(),
            domain.getDescription() != null ? domain.getDescription().value() : null,
            domain.getDueDate().value(),
            domain.getStatus(),
            domain.getOwnerUserId().value(),
            domain.getResponsibleUserIds().getIds(),
            domain.getProjectId().value()
        );
    }
}
