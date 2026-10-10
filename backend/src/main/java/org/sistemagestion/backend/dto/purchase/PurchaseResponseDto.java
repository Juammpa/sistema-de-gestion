package org.sistemagestion.backend.dto.purchase;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.PurchaseType;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PurchaseResponseDto {

    private Long id;
    private Long currentAccountId;
    private LocalDate date;
    private PurchaseType purchaseType;
    private Integer unit;
    private BigDecimal kilograms;
    private BigDecimal pricePerKilo;
    private BigDecimal subtotal;
}
