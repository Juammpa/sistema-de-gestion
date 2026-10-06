package org.sistemagestion.backend.dto.stock;

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
                    Double
                    > kilograms
) {}
