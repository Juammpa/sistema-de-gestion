package org.sistemagestion.backend.dto.cash;

import java.time.LocalDate;

// Un pago en efectivo hecho a un proveedor (resta de la caja)
public record CashWithdrawalDto(
        Long id,
        LocalDate date,
        Double amount,
        String supplierName
) {
}
