package org.sistemagestion.backend.dto.stock;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

// Cortes de una pieza. Una lista vacia deja la pieza sin cortes
public record StockCutsRequestDto(
        @NotNull(message = "Los cortes son obligatorios.")
        List<@Valid Cut> cuts
) {

    public record Cut(
        // Solo viaja en las unidades que ya existen; sin id es una unidad nueva
        Long id,

        @NotNull(message = "El corte es obligatorio.")
        Long cutMeatId,

        @NotNull(message = "El peso del corte es obligatorio.")
        @Positive(message = "Cada corte debe pesar más de 0 kg.")
        @Digits(integer = 5, fraction = 2, message = "Cada peso admite hasta 2 decimales.")
        BigDecimal kilograms
    ){}

}
