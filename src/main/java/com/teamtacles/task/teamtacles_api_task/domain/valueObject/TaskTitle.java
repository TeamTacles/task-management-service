package com.teamtacles.task.teamtacles_api_task.domain.valueObject;

public record TaskTitle(String value) {
        public TaskTitle {
        if (value == null || value.isBlank() || value.length() > 50) {
            throw new IllegalArgumentException("Invalid title: must be between 1 and 50 characters long.");
        }
    }
}
