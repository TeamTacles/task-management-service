package com.teamtacles.task.teamtacles_api_task.domain.valueObject;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record DueDate(LocalDateTime value) {

    public DueDate {
        if (value == null) {
            throw new IllegalArgumentException("A data de vencimento não pode ser nula");
        }
        if (!value.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data de vencimento deve ser no futuro");
        }
    }
}
