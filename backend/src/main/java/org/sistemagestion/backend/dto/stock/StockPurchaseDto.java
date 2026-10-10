package org.sistemagestion.backend.dto.stock;

import java.math.BigDecimal;

import org.sistemagestion.backend.model.enums.PurchaseType;

import java.time.LocalDate;

// Una compra tal como sale de la BD, antes de partirla en piezas
public record StockPurchaseDto(
        Long id,
        LocalDate date,
        PurchaseType purchaseType,
        Integer unit,
        BigDecimal kilograms,
        Long supplierId,
        String supplierName
) {}
