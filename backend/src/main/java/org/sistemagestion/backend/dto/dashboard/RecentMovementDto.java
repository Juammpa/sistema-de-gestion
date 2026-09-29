package org.sistemagestion.backend.dto.dashboard;

import java.time.LocalDate;

public record RecentMovementDto(
        Long id,
        LocalDate date,
        DashboardMovementKind kind,
        String counterparty,    // cliente o proveedor
        String detail,          // Corte (venta), tipo de compra o tipo de pago
        Double amount
) {
}
