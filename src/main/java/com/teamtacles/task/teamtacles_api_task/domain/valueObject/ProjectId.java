package com.teamtacles.task.teamtacles_api_task.domain.valueObject;

public record ProjectId(Long value) {

    public ProjectId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Invalid project ID");
        }
    }
}