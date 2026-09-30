package org.sistemagestion.backend.dto.sales;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SaleRequestDto {

    @NotNull(message = "El ID del cliente es obligatorio.")
    private Long clientId;

    @NotNull(message = "El ID de la sucursal es obligatorio.")
    private Long butcherShopId;

    @NotNull(message = "El ID del corte de carne es obligatorio.")
    private Long cutMeatId;

    @NotNull(message = "Los kilogramos son obligatorios.")
    @Positive(message = "Los kilogramos deben ser positivos.")
    private Double kilograms;

    @NotNull(message = "Las unidades son obligatorias.")
    @Positive(message = "Las unidades deben ser positivas.")
    private Integer unit;

    private LocalDate date;

}
