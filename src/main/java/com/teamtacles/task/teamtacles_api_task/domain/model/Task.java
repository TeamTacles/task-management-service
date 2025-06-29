package com.teamtacles.task.teamtacles_api_task.domain.model;

import com.teamtacles.task.teamtacles_api_task.domain.model.enums.Status;
import com.teamtacles.task.teamtacles_api_task.domain.valueObject.Description;
import com.teamtacles.task.teamtacles_api_task.domain.valueObject.DueDate;
import com.teamtacles.task.teamtacles_api_task.domain.valueObject.OwnerUserId;
import com.teamtacles.task.teamtacles_api_task.domain.valueObject.ProjectId;
import com.teamtacles.task.teamtacles_api_task.domain.valueObject.ResponsiblesList;
import com.teamtacles.task.teamtacles_api_task.domain.valueObject.TaskId;
import com.teamtacles.task.teamtacles_api_task.domain.valueObject.TaskTitle;

public class Task {
    private final TaskId id;
    private TaskTitle title;
    private Description description;
    private DueDate dueDate;
    private Status status;
    private OwnerUserId ownerUserId;
    private ResponsiblesList responsibleUserIds;
    private ProjectId projectId;

    public Task(TaskId id, TaskTitle title, Description description, DueDate dueDate, Status status,
                OwnerUserId ownerUserId, ResponsiblesList responsibleUserIds, ProjectId projectId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.ownerUserId = ownerUserId;
        this.responsibleUserIds = responsibleUserIds;
        this.projectId = projectId;
    }

    public TaskId getId() {
        return id;
    }

    public TaskTitle getTitle() {
        return title;
    }

    public Description getDescription() {
        return description;
    }

    public DueDate getDueDate() {
        return dueDate;
    }

    public Status getStatus() {
        return status;
    }

    public OwnerUserId getOwnerUserId() {
        return ownerUserId;
    }

    public ResponsiblesList getResponsibleUserIds() {
        return responsibleUserIds;
    }

    public ProjectId getProjectId() {
        return projectId;
    }
}
