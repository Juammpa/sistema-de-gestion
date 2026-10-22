package org.sistemagestion.backend.dto.stock;

import java.math.BigDecimal;

import org.sistemagestion.backend.model.enums.Specie;

import java.time.LocalDate;

// Una compra tal como sale de la BD, antes de partirla en piezas
public record StockPurchaseDto(
        Long id,
        LocalDate date,
        Long purchaseTypeId,
        String purchaseTypeName,
        Specie specie,
        Integer unit,
        BigDecimal kilograms,
        Long supplierId,
        String supplierName
) {}
