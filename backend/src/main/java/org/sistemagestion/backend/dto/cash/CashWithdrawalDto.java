package org.sistemagestion.backend.dto.cash;

import java.time.LocalDate;

public record CashWithdrawalDto(
        Long id,
        LocalDate date,
        Double amount,
        String supplierName
) {
}
