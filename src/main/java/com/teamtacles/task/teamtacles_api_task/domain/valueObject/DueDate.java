package com.teamtacles.task.teamtacles_api_task.domain.valueObject;

import java.time.LocalDateTime;

public record DueDate(LocalDateTime value) {

    public DueDate {
        if (value == null) {
            throw new IllegalArgumentException("Due date cannot be null");
        }
        if (!value.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Due date must be in the future");
        }
    }
}
