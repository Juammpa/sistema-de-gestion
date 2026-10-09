package org.sistemagestion.backend.dto.purchase;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.PurchaseType;
import org.sistemagestion.backend.validation.OnUpdate;

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
    @Max(value = 9999, message = "Las unidades no pueden superar 9.999")
    private Integer unit;

    @NotNull(message = "Los kilogramos son obligatorios.")
    @Positive(message = "Los kilogramos deben ser positivos.")
    @DecimalMax(value = "99999.99", message = "Los kilogramos no pueden superar 99.999,99")
    private Double kilograms;

    @NotNull(message = "El precio es obligatorio.")
    @Positive(message = "El precio debe ser positivo.")
    @DecimalMax(value = "9999999.99", message = "El precio no puede superar $9.999.999,99")
    private Double pricePerKilo;

    @NotNull(message = "El ID de la cuenta corriente asociada es obligatorio.")
    private Long currentAccountId;

    // Al crear se ignora (la compra se registra con la fecha de hoy);
    // Al editar es obligatoria
    @NotNull(groups = OnUpdate.class, message = "La fecha es obligatoria.")
    @PastOrPresent(message = "La fecha no puede ser futura.")
    private LocalDate date;

}
