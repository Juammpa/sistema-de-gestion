package org.sistemagestion.backend.dto.movement;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.MovementType;
import org.sistemagestion.backend.model.enums.PaymentType;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MovementResponseDto {

    private Long id;
    private MovementType type;
    private LocalDate date;
    private BigDecimal amount;
    private BigDecimal balance;

    // Propios de Sale/Purchase (null si type === PAYMENT)
    private String cutMeatName;
    private Integer unit;
    private BigDecimal kilograms;
    private BigDecimal pricePerKilo;

    // Propio de Payment (null si type == SALE)
    private PaymentType paymentType;

    // Solo en pagos CUSTOM
    private String customPaymentMethodName;

    // Propio de Purchase (null salvo que type == PURCHASE)
    private Long purchaseTypeId;
    private String purchaseTypeName;

}
