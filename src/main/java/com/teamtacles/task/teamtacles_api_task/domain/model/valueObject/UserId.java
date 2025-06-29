package com.teamtacles.task.teamtacles_api_task.domain.model.valueObject;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Representa o identificador único de um usuário.
 * Este é um Value Object, o que significa que é definido pelo seu valor (o ID do usuário)
 * e é imutável. Ele não possui um ciclo de vida próprio ou comportamentos complexos de domínio.
 */
@Embeddable // Indica que este VO pode ser embutido como parte de uma entidade JPA
public class UserId implements Serializable {
    private Long userId; // O nome do campo interno do Value Object

    // Construtor protegido (ou privado) sem argumentos para uso do JPA/Hibernate.
    protected UserId() {}

    /**
     * Construtor para criar uma nova instância de UserId.
     * Realiza validação para garantir que o ID é válido.
     * @param userId O valor Long que representa o ID do usuário.
     * @throws IllegalArgumentException se o valor for nulo ou inválido.
     */
    public UserId(Long userId) {
        if (userId == null || userId <= 0) { // Exemplo de validação: ID não pode ser nulo ou não positivo
            throw new IllegalArgumentException("User ID cannot be null or non-positive.");
        }
        this.userId = userId;
    }

    /**
     * Retorna o valor Long do ID do usuário.
     * @return O ID do usuário.
     */
    public Long getUserId() {
        return userId;
    }

    // Métodos equals() e hashCode() são cruciais para Value Objects.
    // Eles garantem que dois VOs são considerados iguais se seus *valores* são iguais.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId otherUserId = (UserId) o; // Use um nome diferente para a variável cast
        return Objects.equals(this.userId, otherUserId.userId); // COMPARAÇÃO CORRIGIDA AQUI
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId); // Hash baseado no valor do campo 'userId'
    }

    @Override
    public String toString() {
        return "UserId{" +
               "value=" + userId + // Para ser consistente com 'value' na string
               '}';
    }
}