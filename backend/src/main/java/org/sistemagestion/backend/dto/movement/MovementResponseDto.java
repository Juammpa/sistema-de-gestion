package org.sistemagestion.backend.dto.movement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.CutMeatType;
import org.sistemagestion.backend.model.enums.MovementType;
import org.sistemagestion.backend.model.enums.PaymentType;
import org.sistemagestion.backend.model.enums.PurchaseType;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MovementResponseDto {

    private Long id;
    private MovementType type;
    private LocalDate date;
    private Double amount;
    private Double balance;

    // Propios de Sale/Purchase (null si type === PAYMENT)
    private CutMeatType cutMeatType;
    private Integer unit;
    private Double kilograms;
    private Double pricePerKilo;

    // Propio de Payment (null si type == SALE)
    private PaymentType paymentType;

    // Propio de Purchase (null salvo que tyep = PURCHASE)
    private PurchaseType purchaseType;

}
