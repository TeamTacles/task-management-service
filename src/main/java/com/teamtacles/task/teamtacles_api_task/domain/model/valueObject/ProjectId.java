package com.teamtacles.task.teamtacles_api_task.domain.model.valueObject;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProjectId implements Serializable{
    private Long value; // O valor real do ID do projeto

    protected ProjectId() {}

    public ProjectId(Long value) {
        if (value == null || value <= 0) { // Exemplo de validação
            throw new IllegalArgumentException("Project ID cannot be null or non-positive.");
        }
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectId projectId = (ProjectId) o;
        return Objects.equals(value, projectId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "ProjectId{" +
               "value=" + value +
               '}';
    }

    public static ProjectId of(Long id) {
        return new ProjectId(id);
    }
}
