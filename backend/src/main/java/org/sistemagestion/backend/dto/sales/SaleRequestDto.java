package org.sistemagestion.backend.dto.sales;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.validation.OnUpdate;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SaleRequestDto {

    @NotNull(message = "El ID del cliente es obligatorio.")
    @Positive(message = "El ID del cliente debe ser positivo.")
    private Long clientId;

    @NotNull(message = "El ID de la sucursal es obligatorio.")
    @Positive(message = "El ID de la sucursal debe ser positivo.")
    private Long butcherShopId;

    @NotNull(message = "El ID del corte de carne es obligatorio.")
    @Positive(message = "El ID del corte de carne debe ser positivo.")
    private Long cutMeatId;

    @NotNull(message = "Los kilogramos son obligatorios.")
    @Positive(message = "Los kilogramos deben ser positivos.")
    @DecimalMax(value = "9999.999", message = "Los kilogramos no pueden superar 9.999,999")
    @Digits(integer = 4, fraction = 3, message = "Los kilogramos admiten hasta 3 decimales.")
    private BigDecimal kilograms;

    @NotNull(message = "Las unidades son obligatorias.")
    @Positive(message = "Las unidades deben ser positivas.")
    @Max(value = 9999, message = "Las unidades no pueden superar 9.999")
    private Integer unit;

    // Al crear se ignora (la venta se registra con la fecha de hoy)
    // Al editar es obligatoria.
    @NotNull(groups = OnUpdate.class, message = "La fecha es obligatoria.")
    @PastOrPresent(message = "La fecha no puede ser futura.")
    private LocalDate date;

}
