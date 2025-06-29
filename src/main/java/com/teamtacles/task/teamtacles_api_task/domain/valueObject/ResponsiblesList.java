package com.teamtacles.task.teamtacles_api_task.domain.valueObject;

import java.util.List;

public class ResponsiblesList {
    private final List<Long> ids;

    public ResponsiblesList(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Should have at least 1 responsible");
        }
        if (ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Invalid IDs");
        }
        this.ids = List.copyOf(ids);
    }

    public List<Long> getIds() {
        return ids;
    }
}
