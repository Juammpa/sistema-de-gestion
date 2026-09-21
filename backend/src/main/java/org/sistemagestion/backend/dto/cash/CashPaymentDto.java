package org.sistemagestion.backend.dto.cash;

import org.sistemagestion.backend.model.enums.PaymentType;

import java.time.LocalDate;

public record CashPaymentDto(
        Long id,
        LocalDate date,
        PaymentType paymentType,
        Double amount,
        String clientName,
        String butcherShopName,
        String supplierName // Solo en SUPPLIER_TRANSFER
) {}


