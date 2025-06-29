package com.teamtacles.task.teamtacles_api_task.domain.valueObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record TaskTitle(String value) {
        public TaskTitle {
        if (value == null || value.isBlank() || value.length() > 50) {
            throw new IllegalArgumentException("Título inválido: deve ter entre 1 e 50 caracteres.");
        }
    }
}
