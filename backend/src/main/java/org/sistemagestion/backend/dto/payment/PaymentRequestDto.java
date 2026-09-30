package org.sistemagestion.backend.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.PaymentType;

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
    private Double amount;

    @NotNull(message = "El ID de la cuenta corriente asociada es obligatorio.")
    private Long currentAccountId;

    private LocalDate date;
    private Long supplierId;
}
