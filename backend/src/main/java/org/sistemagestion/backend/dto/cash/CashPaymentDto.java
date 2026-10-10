package org.sistemagestion.backend.dto.cash;

import java.math.BigDecimal;

import org.sistemagestion.backend.model.enums.PaymentType;

import java.time.LocalDate;

// Un pago recibido de un cliente, listo para mostrar en caja
public record CashPaymentDto(
        Long id,
        LocalDate date,
        PaymentType paymentType,
        BigDecimal amount,
        String clientName,
        String butcherShopName,
        String supplierName // Solo en SUPPLIER_TRANSFER
) {}


