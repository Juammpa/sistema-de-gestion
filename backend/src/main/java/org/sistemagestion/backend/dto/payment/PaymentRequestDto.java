package org.sistemagestion.backend.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.PaymentType;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PaymentRequestDto {

    @NotNull(message = "The paymentType is mandatory")
    private PaymentType paymentType;

    @NotNull(message = "The amount is mandatory")
    @Positive(message = "The amount must be positive")
    private Double amount;

    @NotNull(message = "The butcherShopId is mandatory")
    private Long butcherShopId;

    @NotNull(message = "The clientId is mandatory")
    private Long clientId;
}
