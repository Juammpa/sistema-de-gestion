package org.sistemagestion.backend.dto.dashboard;

import java.math.BigDecimal;

// Un cliente o proveedor con su saldo pendiente
public record DebtRankingDto(
        Long id,
        String name,
        BigDecimal balance
) {
}
