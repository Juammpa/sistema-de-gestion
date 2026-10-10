package org.sistemagestion.backend.dto.cash;

import java.math.BigDecimal;

import java.time.LocalDate;

// Un pago en efectivo hecho a un proveedor (resta de la caja)
public record CashWithdrawalDto(
        Long id,
        LocalDate date,
        BigDecimal amount,
        String supplierName
) {
}
