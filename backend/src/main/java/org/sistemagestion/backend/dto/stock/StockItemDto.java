package org.sistemagestion.backend.dto.stock;

import org.sistemagestion.backend.model.enums.PurchaseType;
import org.sistemagestion.backend.model.enums.Specie;

import java.time.LocalDate;

// Una pieza de stock: una unidad de compra
public record StockItemDto(
        Long purchaseId,
        int unitNumber,     // 1, 2, 3, ...
        int totalUnits,     // unidades de la compra (para mostrar "2 de 4")
        PurchaseType purchaseType,
        Specie specie,
        double kilograms,   // kilos de la compra / unidades
        LocalDate date,
        Long supplierId,
        String supplierName,
        boolean editable,   // la compra tiene mas de una unidad, se puede repartir el peso
        boolean adjusted    // el peso fue ajustado a mano
) {}
