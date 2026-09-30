package org.sistemagestion.backend.dto.purchase;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.PurchaseType;
import org.sistemagestion.backend.model.enums.Specie;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PurchaseRequestDto {

    @NotNull(message = "El tipo de compra es obligatorio.")
    private PurchaseType purchaseType;

    @NotNull(message = "Las unidades son obligatorias.")
    @Positive(message = "Las unidades deben ser positivas.")
    private Integer unit;

    @NotNull(message = "Los kilogramos son obligatorios.")
    @Positive(message = "Los kilogramos deben ser positivos.")
    private Double kilograms;

    @NotNull(message = "El precio es obligatorio.")
    @Positive(message = "El precio debe ser positivo.")
    private Double pricePerKilo;

    @NotNull(message = "El ID de la cuenta corriente asociada es obligatorio.")
    private Long currentAccountId;

    private LocalDate date;

}
