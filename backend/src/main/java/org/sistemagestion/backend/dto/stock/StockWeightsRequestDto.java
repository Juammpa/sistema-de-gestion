package org.sistemagestion.backend.dto.stock;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

// Pe2sos de todas las piezas de una compra, en orden
public record StockWeightsRequestDto(
        @NotEmpty(message = "Los pesos de las piezas son obligatorios.")
        List<
                    @NotNull(message = "Cada pieza debe tener un peso.")
                    @Positive(message = "Cada pieza debe pesar más de 0 kg.")
                    @Digits(integer = 5, fraction = 2, message = "Cada peso admite hasta 2 decimales.")
                    BigDecimal
                    > kilograms
) {}
