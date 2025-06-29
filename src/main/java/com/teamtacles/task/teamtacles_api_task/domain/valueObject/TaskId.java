package com.teamtacles.task.teamtacles_api_task.domain.valueObject;

public record TaskId(Long value) {
    public TaskId {
        if (value == null || value <= 0)
            throw new IllegalArgumentException("Invalid Task ID!");
    }
}
