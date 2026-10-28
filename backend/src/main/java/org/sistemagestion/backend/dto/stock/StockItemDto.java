package org.sistemagestion.backend.dto.stock;

import java.math.BigDecimal;

import org.sistemagestion.backend.model.enums.Specie;

import java.time.LocalDate;
import java.util.List;

// Una pieza de stock: una unidad de compra
public record StockItemDto(
        Long purchaseId,
        int unitNumber,     // 1, 2, 3, ...
        int totalUnits,     // unidades de la compra (para mostrar "2 de 4")
        Long purchaseTypeId,
        String purchaseTypeName,
        Specie specie,
        BigDecimal kilograms,   // kilos de la compra / unidades
        LocalDate date,
        Long supplierId,
        String supplierName,
        boolean editable,   // la compra tiene mas de una unidad, se puede repartir el peso
        boolean adjusted,    // el peso fue ajustado a mano
        List<Cut> cuts      // cortes sacadose de esta pieza (vacio si no tiene)
) {

    // Una unidad de corte de la pieza (vendidad = ya pertenece a una venta y no se puede tocar)
    public record Cut(Long id, Long cutMeatId, String cutMeatName, BigDecimal kilograms, boolean sold) {}

}
