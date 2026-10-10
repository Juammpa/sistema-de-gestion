package org.sistemagestion.backend.dto.stock;

import java.math.BigDecimal;

import org.sistemagestion.backend.model.enums.PurchaseType;

import java.util.List;

// Totales del period (siempre del rango completo, sin los filtros de pantalla)
public record StockSummaryDto(
    BigDecimal totalKilograms,
    int totalUnits,
    SpecieTotals bovine,
    SpecieTotals porcine,
    List<TypeTotals> byType
) {
    public record SpecieTotals(BigDecimal kilograms, int units) {}

    public record TypeTotals(PurchaseType purchaseType, BigDecimal kilograms, int units) {}
}
