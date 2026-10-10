package org.sistemagestion.backend.dto.sales;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SaleResponseDto {

    private Long id;
    private Long clientId;
    private LocalDate date;
    private Long cutMeatId;
    private String cutMeatName;
    private Integer unit;
    private BigDecimal kilograms;
    private BigDecimal pricePerKilo;
    private BigDecimal subtotal;

}
