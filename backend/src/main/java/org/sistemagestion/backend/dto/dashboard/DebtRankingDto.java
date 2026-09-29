package org.sistemagestion.backend.dto.dashboard;

// Un cliente o proveedor con su saldo pendiente
public record DebtRankingDto(
        Long id,
        String name,
        Double balance
) {
}
