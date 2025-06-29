package com.teamtacles.task.teamtacles_api_task.domain.valueObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record OwnerUserId(Long value) {

    public OwnerUserId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ID do usuário dono inválido");
        }
    }
}