package com.teamtacles.task.teamtacles_api_task.domain.valueObject;

public record OwnerUserId(Long value) {

    public OwnerUserId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Invalid owner user ID");
        }
    }
}