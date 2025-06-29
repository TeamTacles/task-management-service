package com.teamtacles.task.teamtacles_api_task.domain.valueObject;

public record Description(String value) {

    public Description {
        if (value != null && value.length() > 250) {
            throw new IllegalArgumentException("Description must be at most 250 characters long");
        }
    }
}
