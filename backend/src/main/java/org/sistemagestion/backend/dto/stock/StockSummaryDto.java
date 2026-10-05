package org.sistemagestion.backend.dto.stock;

import org.sistemagestion.backend.model.enums.PurchaseType;

import java.util.List;

// Totales del period (siempre del rango completo, sin los filtros de pantalla)
public record StockSummaryDto(
    double totalKilograms,
    int totalUnits,
    SpecieTotals bovine,
    SpecieTotals porcine,
    List<TypeTotals> byType
) {
    public record SpecieTotals(double kilograms, int units) {}

    public record TypeTotals(PurchaseType purchaseType, double kilograms, int units) {}
}
