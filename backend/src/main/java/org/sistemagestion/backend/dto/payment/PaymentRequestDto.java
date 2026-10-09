package org.sistemagestion.backend.dto.payment;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.PaymentType;
import org.sistemagestion.backend.validation.OnUpdate;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PaymentRequestDto {

    @NotNull(message = "El tipo de pago es obligatorio.")
    private PaymentType paymentType;

    @NotNull(message = "El monto es obligatorio.")
    @Positive(message = "El monto debe ser positivo.")
    @DecimalMax(value = "999999999.99", message = "El monto no puede superar $999.999.999,99")
    private Double amount;

    @NotNull(message = "El ID de la cuenta corriente asociada es obligatorio.")
    @Positive(message = "El ID de la cuenta corriente debe ser positivo.")
    private Long currentAccountId;

    // Al crear se ignora (el pago se registra con la fecha de hoy)
    // Al editar es obligatoria
    @NotNull(groups = OnUpdate.class, message = "La fecha es obligatoria.")
    @PastOrPresent(message = "La fecha no puede ser futura.")
    private LocalDate date;

    // Solo se usa en transferencia a proveedor
    @Positive(message = "El ID del proveedor debe ser positivo.")
    private Long supplierId;
}
