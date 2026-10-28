package org.sistemagestion.backend.dto.stock;

import java.math.BigDecimal;
import java.time.LocalDate;

// Una unidad de corte sin vender: se puede elegir al armar una venta
public record AvailableCutDto(
        Long id,
        Long cutMeatId,
        String cutMeatName,
        BigDecimal kilograms,
        Long purchaseId,
        Integer pieceNumber,
        Integer purchaseUnits,    // unidades de la compra (para mostrar "pieza 2 de 4")
        LocalDate purchaseDate,
        String purchaseTypeName,
        String supplierName
) {}
