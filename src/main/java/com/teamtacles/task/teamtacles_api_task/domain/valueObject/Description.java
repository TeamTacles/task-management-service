package com.teamtacles.task.teamtacles_api_task.domain.valueObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Description(String value) {

    public Description {
        if (value != null && value.length() > 250) {
            throw new IllegalArgumentException("Descrição deve ter no máximo 250 caracteres");
        }
    }
}
